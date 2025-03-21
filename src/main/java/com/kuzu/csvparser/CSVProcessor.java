package com.kuzu.csvparser;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CSVProcessor {

    public static void processCSV(String csvFilePath, double price, String name, String uptime,
                                  String validity, String speed, double voucherScale) throws IOException {

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
            System.out.println("No vouchers to process.");
            return;
        }

        // Generate unique output path for HTML file
        String basePath = csvFilePath.replace(".csv", "_vouchers");
        String outputPath = generateUniqueOutputPath(basePath, "html");

        // Load HTML template from the template folder within the application
        String templatePath = findTemplatePath("root_template.html");
        String mainTemplate = loadTemplate(templatePath);

        // Load voucher template from the template folder within the application
        String voucherTemplatePath = findTemplatePath("voucher_template.html");
        String voucherTemplate = loadTemplate(voucherTemplatePath);

        if (mainTemplate == null || voucherTemplate == null) {
            System.err.println("Template file not found: " + templatePath + " or " + voucherTemplatePath);
            return;
        }

        // Replace font size placeholder in template
        mainTemplate = mainTemplate.replace("${FONT_SIZE}", String.format("%.2fpx", voucherScale * 12));

        // Generate voucher HTML content
        StringBuilder vouchersContent = new StringBuilder();
        for (CSVRecord record : records) {
            // Get the raw record value for the code
            String parsedCode = "";
            if (record.size() > 0) {
                String code = record.get(0); // base code
                parsedCode = code.substring(code.indexOf(";\"")+2, code.indexOf("\";"));
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

        // Write to output file
        try (FileWriter writer = new FileWriter(outputPath, StandardCharsets.UTF_8)) {
            writer.write(finalHtml);
        }

        System.out.println("HTML file with vouchers generated at: " + outputPath);
    }

    /**
     * Find the template path by looking in multiple possible locations
     */
    private static String findTemplatePath(String templateFileName) {
        // List of potential template locations to check
        List<String> potentialPaths = new ArrayList<>();

        // 1. Check template folder relative to application directory
        String appDir = System.getProperty("user.dir");
        potentialPaths.add(Paths.get(appDir, "template", templateFileName).toString());

        // 2. Check template folder relative to JAR location
        try {
            String jarPath = CSVProcessor.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            File jarFile = new File(jarPath);
            String jarDir = jarFile.getParentFile().getPath();
            potentialPaths.add(Paths.get(jarDir, "template", templateFileName).toString());
        } catch (Exception e) {
            System.err.println("Warning: Could not determine JAR location: " + e.getMessage());
        }

        // 3. Check relative to the class path
        potentialPaths.add(Paths.get("template", templateFileName).toString());

        // Try each path
        for (String path : potentialPaths) {
            if (Files.exists(Path.of(path))) {
                System.out.println("Found template at: " + path);
                return path;
            }
        }

        // If not found, return the default path for error reporting
        System.err.println("Template not found in any of the following locations:");
        potentialPaths.forEach(path -> System.err.println("- " + path));
        return potentialPaths.get(0);
    }

    private static String loadTemplate(String templatePath) {
        try {
            return Files.readString(Path.of(templatePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error reading template file: " + e.getMessage());
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
}