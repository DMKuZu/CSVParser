package com.kuzu.csvparser;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.awt.Desktop;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// Import for OpenHtmlToPdf library
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.util.XRLog;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.w3c.dom.Document;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CSVProcessor {

    public static void processCSV(String csvFilePath, double price, String name, String uptime,
                                  String validity, String speed) throws IOException {

        File csvFile = new File(csvFilePath);

        // Configure CSVFormat
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setSkipHeaderRecord(true)
                .build();

        // Parse CSV file
        List<CSVRecord> records;
        try (CSVParser parser = CSVParser.parse(new FileReader(csvFile), format)) {
            records = new ArrayList<>(parser.getRecords());
        }

        int totalVouchers = records.size();
        if (totalVouchers == 0) {
//        System.out.println("No vouchers to process.");
            return;
        }

        // Generate unique output path for HTML file
        String basePath = csvFilePath.replace(".csv", "_vouchers");
        String htmlOutputPath = generateUniqueOutputPath(basePath, "html");
        String pdfOutputPath = generateUniqueOutputPath(basePath, "pdf");

        // Load HTML template from the resources folder
        String templatePath = findTemplatePath("root_template.html");
        String mainTemplate = loadTemplate(templatePath);

        // Load voucher template from the resources folder
        String voucherTemplatePath = findTemplatePath("voucher_template.html");
        String voucherTemplate = loadTemplate(voucherTemplatePath);

        if (mainTemplate == null || voucherTemplate == null) {
//        System.err.println("Template file not found: " + templatePath + " or " + voucherTemplatePath);
            return;
        }

        // Get the background image path as a resource
        String bgImagePath = CSVProcessor.class.getResource("/template/bg_voucher.jpg").toString();

        // Update the image path in the voucher template
        voucherTemplate = updateImagePathInTemplate(voucherTemplate, bgImagePath);

        // Generate voucher HTML content
        StringBuilder vouchersContent = new StringBuilder();
        for (CSVRecord record : records) {
            // Get the raw record value for the code
            String parsedCode = "";
            if (record.size() > 0) {
                String code = record.get(0); // base code
                parsedCode = code.substring(code.indexOf(";\"") + 2, code.indexOf("\";"));
            }

            // Use the template with placeholders
            String voucherHtml = voucherTemplate
                    .replace("${price}", String.format("%.2f", price))
                    .replace("${name}", name)
                    .replace("${uptime}", uptime)
                    .replace("${validity}", validity)
                    .replace("${speed}", speed)
                    .replace("${code}", parsedCode);

            vouchersContent.append(voucherHtml);
        }

        // Replace vouchers placeholder in template
        String finalHtml = mainTemplate.replace("${VOUCHERS}", vouchersContent.toString());

        // Write to HTML output file
        try (FileWriter writer = new FileWriter(htmlOutputPath, StandardCharsets.UTF_8)) {
            writer.write(finalHtml);
        }

        // Convert HTML to PDF with proper formatting
        boolean pdfSuccess = convertHtmlToPdf(htmlOutputPath, pdfOutputPath);

        if (pdfSuccess) {
//        System.out.println("PDF file with vouchers generated at: " + pdfOutputPath);
            openPdfInViewer(pdfOutputPath);
        }
    }

    private static String updateImagePathInTemplate(String template, String newImagePath) {
        // Use regex to replace the image source path
        Pattern pattern = Pattern.compile("src=\"[^\"]+\"");
        Matcher matcher = pattern.matcher(template);
        if (matcher.find()) {
            return template.replace(matcher.group(0), "src=\"" + newImagePath + "\"");
        }
        return template;
    }

    /**
     * Convert HTML file to PDF using OpenHtmlToPdf
     * This library provides better CSS support and layout preservation
     */
    private static boolean convertHtmlToPdf(String htmlFilePath, String pdfFilePath) {
        try {
            // Reduce logging noise
            XRLog.setLoggingEnabled(false);

            // Read the HTML file content
            String htmlContent = Files.readString(Path.of(htmlFilePath), StandardCharsets.UTF_8);

            // Fix for rotated text - use a different approach that's better supported
            htmlContent = htmlContent.replace("transform: rotate(-90deg)",
                    "writing-mode: vertical-rl; text-orientation: mixed; transform: rotate(-90deg)");

            // Parse HTML to DOM using jsoup
            org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(htmlContent);
            jsoupDoc.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);

            // Convert to W3C Document
            Document w3cDoc = new W3CDom().fromJsoup(jsoupDoc);

            // Create PDF output stream
            try (OutputStream os = new FileOutputStream(pdfFilePath)) {
                PdfRendererBuilder builder = new PdfRendererBuilder();

                // Set base URI to resolve resources
                // This uses the URI format which most versions support
                String baseUri = new File(htmlFilePath).getParentFile().toURI().toString();
                builder.withW3cDocument(w3cDoc, baseUri);

                builder.toStream(os);

                // Set A4 paper size with small margins
                builder.useDefaultPageSize(210, 297, PdfRendererBuilder.PageSizeUnits.MM);

                // Build and run renderer
                builder.run();

                return true;
            }
        } catch (Exception e) {
//            System.err.println("Error converting HTML to PDF: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static String findTemplatePath(String templateFileName) {
        // The template file is in the resources folder, so we return the resource path
        return "/template/" + templateFileName;
    }

    private static String loadTemplate(String templatePath) {
        try (InputStream inputStream = CSVProcessor.class.getResourceAsStream(templatePath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        } catch (IOException e) {
//        System.err.println("Error reading template file: " + e.getMessage());
            return null;
        }
    }

    private static String generateUniqueOutputPath(String basePath, String extension) {
        String outputPath = basePath + "." + extension;
        File file = new File(outputPath);

        int counter = 1;
        while (file.exists()) {
            outputPath = basePath + "(" + counter + ")." + extension;
            file = new File(outputPath);
            counter++;
        }

        return outputPath;
    }

    /**
     * Opens the generated PDF file in the default PDF viewer.
     *
     * @param pdfFilePath Path to the PDF file to open.
     */
    private static void openPdfInViewer(String pdfFilePath) {
        try {
            File pdfFile = new File(pdfFilePath);
            if (pdfFile.exists()) {
                Desktop.getDesktop().open(pdfFile); // Open the PDF file
            } /*else {
                System.err.println("PDF file does not exist: " + pdfFilePath);
            }*/
        } catch (IOException e) {
//            System.err.println("Error opening PDF file in viewer: " + e.getMessage());
        }
    }

    private static void openHtmlInBrowser(String htmlFilePath) {
        try {
            File htmlFile = new File(htmlFilePath);
            if (htmlFile.exists()) {
                Desktop.getDesktop().browse(htmlFile.toURI());
            } /*else {
                System.err.println("HTML file does not exist: " + htmlFilePath);
            }*/
        } catch (IOException e) {
//            System.err.println("Error opening HTML file in browser: " + e.getMessage());
        }
    }
}