package com.kuzu.csvparser;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CSVProcessor {
    public static void processCSV(String csvFilePath, double price, String name, String uptime, String validity, String speed, int vouchersPerPage) throws IOException {
        File csvFile = new File(csvFilePath);

        // Use the builder pattern to configure CSVFormat
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setSkipHeaderRecord(true) // Skip the header record when parsing
                .build();

        CSVParser parser = CSVParser.parse(new FileReader(csvFile), format);

        // Create a PDF document
        PDDocument document = new PDDocument();
        List<String> voucherData = new ArrayList<>();

        // Collect voucher data
        for (CSVRecord record : parser) {
            String voucher = String.format("Price: %.2f | Name: %s | Uptime: %s | Validity: %s | Speed: %s",
                    price, name, uptime, validity, speed);
            voucherData.add(voucher);
        }

        // Create a thread pool with 313 threads
        ExecutorService executor = Executors.newFixedThreadPool(313);

        // Divide the workload into 313 chunks
        int totalVouchers = voucherData.size();
        int chunkSize = 32; // Fixed chunk size

        for (int startIndex = 0; startIndex < totalVouchers; startIndex += chunkSize) {
            int endIndex = Math.min(startIndex + chunkSize, totalVouchers);
            List<String> sublist = voucherData.subList(startIndex, endIndex);
            executor.submit(() -> generateVoucherPage(document, sublist));
        }

        // Shutdown the executor and wait for all threads to finish
        executor.shutdown();
        while (!executor.isTerminated()) {
            // Wait for all threads to finish
        }

        // Save the PDF
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
                contentStream.showText(voucher);
                contentStream.newLineAtOffset(0, -15); // Move to the next line
            }

            contentStream.endText();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
