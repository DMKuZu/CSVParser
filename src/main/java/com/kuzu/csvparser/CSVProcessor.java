package com.kuzu.csvparser;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CSVProcessor {
    /*public static void processCSV(String csvFilePath, double price, String name, String uptime, String validity, String speed, double voucherScale) throws IOException {
        File csvFile = new File(csvFilePath);

        // Use the builder pattern to configure CSVFormat
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setSkipHeaderRecord(true) // Skip the header record when parsing
                .build();

        CSVParser parser = CSVParser.parse(new FileReader(csvFile), format);

        // Create a PDF document
        PDDocument document = new PDDocument();

        // Collect voucher data
        List<String> voucherData = new ArrayList<>();
        for (CSVRecord record : parser) {
            String voucher = String.format(
                    "<div style='font-size: %.2fpx; border: 1px solid black; padding: 10px; margin: 10px;'>" +
                            "<h1>Voucher</h1>" +
                            "<p><strong>Price:</strong> %.2f</p>" +
                            "<p><strong>Name:</strong> %s</p>" +
                            "<p><strong>Uptime:</strong> %s</p>" +
                            "<p><strong>Validity:</strong> %s</p>" +
                            "<p><strong>Speed:</strong> %s</p>" +
                            "</div>",
                    voucherScale * 12, // Scale font size
                    price, name, uptime, validity, speed
            );
            voucherData.add(voucher);
        }

        // Create a thread pool with 313 threads
        ExecutorService executor = Executors.newFixedThreadPool(313);

        // Divide the workload into chunks
        int totalVouchers = voucherData.size();
        int chunkSize = (int) Math.ceil((double) totalVouchers / 313); // Size of each chunk
        int startIndex = 0;

        for (int i = 0; i < 313; i++) {
            int endIndex = Math.min(startIndex + chunkSize, totalVouchers);
            List<String> sublist = voucherData.subList(startIndex, endIndex);
            executor.submit(() -> generateVoucherPage(document, sublist));
            startIndex = endIndex;
        }

        // Shutdown the executor and wait for all threads to finish
        executor.shutdown();
        while (!executor.isTerminated()) {
            // Wait for all threads to finish
        }

        // Save the PDF to the filepath of the csv file
        String outputPath = csvFilePath.replace(".csv", "_vouchers.pdf");
        document.save(outputPath);
        document.close();
        parser.close();
    }

    private static void generateVoucherPage(PDDocument document, List<String> voucherData) {
        PDPage page = new PDPage();
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.setFont(PDType1Font.COURIER, 12);
            contentStream.beginText();
            contentStream.newLineAtOffset(50, 700); // Set starting position

            for (String voucher : voucherData) {
                // Simulate HTML formatting by writing plain text
                contentStream.showText(voucher);
                contentStream.newLineAtOffset(0, -15); // Move to the next line
            }

            contentStream.endText();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
    public static void processCSV(String csvFilePath, double price, String name, String uptime, String validity, String speed, double voucherScale) throws IOException {
        File csvFile = new File(csvFilePath);

        // Use the builder pattern to configure CSVFormat
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setSkipHeaderRecord(true) // Skip the header record when parsing
                .build();

        CSVParser parser = CSVParser.parse(new FileReader(csvFile), format);

        // Collect voucher data
        List<String> voucherData = new ArrayList<>();
        for (CSVRecord record : parser) {
            String voucher = String.format(
                    """
                        <div style='font-size: %.2fpx; border: 1px solid black; padding: 10px; margin: 10px;'>
                            <h1>Voucher</h1>
                            <p><strong>Price:</strong> %.2f</p>
                            <p><strong>Name:</strong> %s</p>
                            <p><strong>Uptime:</strong> %s</p>
                            <p><strong>Validity:</strong> %s</p>
                            <p><strong>Speed:</strong> %s</p>
                        </div>
                    """,
                    voucherScale * 12, // Scale font size
                    price, name, uptime, validity, speed
            );
            voucherData.add(voucher);
        }

        // Create a thread pool with 313 threads
        ExecutorService executor = Executors.newFixedThreadPool(313);

        // Divide the workload into chunks
        int totalVouchers = voucherData.size();
        int chunkSize = (int) Math.ceil((double) totalVouchers / 313); // Size of each chunk
        int startIndex = 0;

        // Generate a unique output path
        String basePath = csvFilePath.replace(".csv", "_vouchers");
        String outputPath = generateUniqueOutputPath(basePath);

        // Create a PDF document
        ITextRenderer renderer = new ITextRenderer();

        for (int i = 0; i < 313; i++) {
            int endIndex = Math.min(startIndex + chunkSize, totalVouchers);
            List<String> sublist = voucherData.subList(startIndex, endIndex);
            executor.submit(() -> generateVoucherPage(renderer, sublist));
            startIndex = endIndex;
        }

        // Shutdown the executor and wait for all threads to finish
        executor.shutdown();
        while (!executor.isTerminated()) {
            // Wait for all threads to finish
        }

        // Save the PDF
        try (OutputStream outputStream = Files.newOutputStream(Paths.get(outputPath))) {
            renderer.createPDF(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            parser.close();
        }
    }

    private static void generateVoucherPage(ITextRenderer renderer, List<String> voucherData) {
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<html><head></head><body>");
        for (String voucher : voucherData) {
            htmlContent.append(voucher);
        }
        htmlContent.append("</body></html>");

        System.out.println("HTML Content:\n" + htmlContent);

        renderer.setDocumentFromString(htmlContent.toString());
        renderer.layout();
    }

    private static String generateUniqueOutputPath(String basePath) {
        String outputPath = basePath + ".pdf";
        File file = new File(outputPath);

        // Append a number if the file already exists
        int counter = 1;
        while (file.exists()) {
            outputPath = basePath + "(" + counter + ").pdf";
            file = new File(outputPath);
            counter++;
        }

        return outputPath;
    }
}