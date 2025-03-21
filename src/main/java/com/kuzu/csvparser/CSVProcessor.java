package com.kuzu.csvparser;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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

        // Generate unique output path for HTML file
        String basePath = csvFilePath.replace(".csv", "_vouchers");
        String outputPath = generateUniqueOutputPath(basePath, "html");

        // Create HTML file
        File htmlFile = new File(outputPath);

        try (FileWriter writer = new FileWriter(htmlFile, StandardCharsets.UTF_8)) {
            // Write HTML header
            writer.write("<!DOCTYPE html>\n");
            writer.write("<html lang=\"en\">\n");
            writer.write("<head>\n");
            writer.write("    <meta charset=\"UTF-8\">\n");
            writer.write("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
            writer.write("    <title>Vouchers</title>\n");
            writer.write("    <style>\n");
            writer.write("        body { font-family: Arial, sans-serif; margin: 20px; }\n");
            writer.write("        .voucher-container { display: flex; flex-wrap: wrap; justify-content: space-around; }\n");
            writer.write("        .voucher {\n");
            writer.write("            border: 1px solid black;\n");
            writer.write("            border-radius: 5px;\n");
            writer.write("            padding: 15px;\n");
            writer.write("            margin: 10px;\n");
            writer.write("            width: 300px;\n");
            writer.write(String.format("            font-size: %.2fpx;\n", voucherScale * 12));
            writer.write("            box-shadow: 0 2px 5px rgba(0,0,0,0.1);\n");
            writer.write("        }\n");
            writer.write("        .voucher h1 { margin-top: 0; text-align: center; }\n");
            writer.write("        .voucher p { margin: 8px 0; }\n");
            writer.write("        @media print {\n");
            writer.write("            .voucher { page-break-inside: avoid; }\n");
            writer.write("            @page { margin: 0.5cm; }\n");
            writer.write("        }\n");
            writer.write("    </style>\n");
            writer.write("</head>\n");
            writer.write("<body>\n");
            writer.write("    <h1>Generated Vouchers</h1>\n");
            writer.write("    <div class=\"voucher-container\">\n");

            // Process each voucher
            for (CSVRecord record : records) {
                // Extract the code from the second column (index 1)
                String code = record.get(1); // Assuming the codes are in the second column

                writer.write("        <div class=\"voucher\">\n");
                writer.write("            <h1>Voucher</h1>\n");
                writer.write(String.format("            <p><strong>Code:</strong> %s</p>\n", code)); // Include the code
                writer.write(String.format("            <p><strong>Price:</strong> %.2f</p>\n", price));
                writer.write(String.format("            <p><strong>Name:</strong> %s</p>\n", name));
                writer.write(String.format("            <p><strong>Uptime:</strong> %s</p>\n", uptime));
                writer.write(String.format("            <p><strong>Validity:</strong> %s</p>\n", validity));
                writer.write(String.format("            <p><strong>Speed:</strong> %s</p>\n", speed));
                writer.write("        </div>\n");
            }

            // Close HTML tags
            writer.write("    </div>\n");
            writer.write("</body>\n");
            writer.write("</html>");
        }

        System.out.println("HTML file with vouchers generated at: " + outputPath);
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