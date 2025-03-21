package com.kuzu.csvparser;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class CSVProcessor {

    public static void processCSV(String csvFilePath, double price, String name, String uptime,
                                  String validity, String speed, double voucherScale) throws IOException, InterruptedException {

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

        // Generate unique output path
        String basePath = csvFilePath.replace(".csv", "_vouchers");
        String outputPath = generateUniqueOutputPath(basePath);

        // Use a reasonable thread pool size based on available processors
        int threadPoolSize = Math.min(Runtime.getRuntime().availableProcessors(), 4);
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

        // Calculate chunk size for each thread
        int chunkSize = (int) Math.ceil((double) totalVouchers / threadPoolSize);

        // Create temporary files for each chunk
        List<File> tempFiles = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(threadPoolSize);
        AtomicInteger processedChunks = new AtomicInteger(0);

        for (int i = 0; i < threadPoolSize; i++) {
            int startIndex = i * chunkSize;
            int endIndex = Math.min(startIndex + chunkSize, totalVouchers);

            // Skip if this chunk would be empty
            if (startIndex >= totalVouchers) {
                latch.countDown();
                continue;
            }

            // Create a temporary file for this chunk
            File tempFile = File.createTempFile("voucher_chunk_" + i, ".pdf");
            tempFile.deleteOnExit(); // Ensure cleanup on JVM exit
            tempFiles.add(tempFile);

            List<CSVRecord> chunkRecords = records.subList(startIndex, endIndex);

            executor.submit(() -> {
                try {
                    generatePDFChunk(tempFile, chunkRecords, price, name, uptime, validity, speed, voucherScale);
                    int completed = processedChunks.incrementAndGet();
                    System.out.println("Processed chunk " + completed + " of " + threadPoolSize);
                } catch (Exception e) {
                    System.err.println("Error processing chunk: " + e.getMessage());
                    e.printStackTrace();

                    // Delete corrupted file if it exists
                    if (tempFile.exists()) {
                        tempFile.delete();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all tasks to complete
        latch.await();
        executor.shutdown();

        // Filter out any missing or corrupted files
        List<File> validFiles = new ArrayList<>();
        for (File file : tempFiles) {
            if (file.exists() && file.length() > 0) {
                validFiles.add(file);
            } else {
                System.err.println("Skipping invalid file: " + file.getAbsolutePath());
            }
        }

        // Merge valid PDF files
        if (!validFiles.isEmpty()) {
            mergePDFs(validFiles, outputPath);
            System.out.println("Final PDF saved to: " + outputPath);
        } else {
            System.err.println("No valid PDF chunks were generated.");
        }
    }

    private static void generatePDFChunk(File outputFile, List<CSVRecord> records,
                                         double price, String name, String uptime, String validity,
                                         String speed, double voucherScale) throws IOException {

        try (PDDocument document = new PDDocument()) {
            // Create a page for every 4 vouchers (or fewer for the last page)
            int vouchersPerPage = 4;
            int totalVouchers = records.size();

            for (int i = 0; i < totalVouchers; i += vouchersPerPage) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    // Start at the top of the page
                    float startY = page.getMediaBox().getHeight() - 50;
                    float startX = 50;

                    // Calculate font sizes based on scale
                    float titleFontSize = (float) (voucherScale * 14);
                    float bodyFontSize = (float) (voucherScale * 10);

                    for (int j = 0; j < vouchersPerPage && (i + j) < totalVouchers; j++) {
                        // Calculate position for this voucher
                        float y = startY - (j * 180);

                        // Draw border for voucher
                        contentStream.setLineWidth(1f);
                        contentStream.addRect(startX, y - 150, 500, 160);
                        contentStream.stroke();

                        // Add voucher title
                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.HELVETICA_BOLD, titleFontSize);
                        contentStream.newLineAtOffset(startX + 10, y);
                        contentStream.showText("Voucher");
                        contentStream.endText();

                        // Add voucher details
                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.HELVETICA, bodyFontSize);
                        contentStream.newLineAtOffset(startX + 10, y - 30);
                        contentStream.showText("Price: " + price);
                        contentStream.newLineAtOffset(0, -20);
                        contentStream.showText("Name: " + name);
                        contentStream.newLineAtOffset(0, -20);
                        contentStream.showText("Uptime: " + uptime);
                        contentStream.newLineAtOffset(0, -20);
                        contentStream.showText("Validity: " + validity);
                        contentStream.newLineAtOffset(0, -20);
                        contentStream.showText("Speed: " + speed);
                        contentStream.endText();
                    }
                }
            }

            // Save the document to the temporary file
            document.save(outputFile);
        }
    }

    private static String generateUniqueOutputPath(String basePath) {
        String outputPath = basePath + ".pdf";
        File file = new File(outputPath);

        int counter = 1;
        while (file.exists()) {
            outputPath = basePath + "(" + counter + ").pdf";
            file = new File(outputPath);
            counter++;
        }

        return outputPath;
    }

    private static void mergePDFs(List<File> pdfFiles, String outputPath) throws IOException {
        PDFMergerUtility pdfMerger = new PDFMergerUtility();
        pdfMerger.setDestinationFileName(outputPath);

        // Add source files to merge
        for (File file : pdfFiles) {
            pdfMerger.addSource(file);
        }

        // Merge the PDFs
        pdfMerger.mergeDocuments(null);

        // Clean up temporary files
        for (File file : pdfFiles) {
            file.delete();
        }
    }
}