package com.kuzu.csvparser;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class MainController {
    public Button btnGenerate;
    public TextField tfPrice;
    public TextField tfName;
    public TextField tfUptime;
    public TextField tfValidity;
    public TextField tfSpeed;
    public Button btnChooseFile;
    public TextArea taCSVfile;
    public ComboBox<Integer> cbVouchersPerPage;

    private File selectedFile;

    @FXML
    public void initialize() {
        // Populate the ComboBox with values
        cbVouchersPerPage.getItems().addAll(9, 12, 24, 48, 96);
        cbVouchersPerPage.getSelectionModel().selectFirst(); // Select the first value by default

        // Set up the file chooser button
        btnChooseFile.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choose CSV File");

            // Set file extension filter for CSV files
            FileChooser.ExtensionFilter csvFilter = new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv");
            fileChooser.getExtensionFilters().add(csvFilter);

            // Show the file chooser dialog
            Stage stage = (Stage) btnChooseFile.getScene().getWindow();
            selectedFile = fileChooser.showOpenDialog(stage);

            // Update the TextArea with the selected file's name
            if (selectedFile != null) {
                taCSVfile.setText(selectedFile.getName());
            } else {
                taCSVfile.setText("No file selected");
            }
        });

        // Set up the generate button
        btnGenerate.setOnAction(event -> {
            try {
                // Get values from the text fields
                double price = Double.parseDouble(tfPrice.getText());
                String name = tfName.getText();
                String uptime = tfUptime.getText();
                String validity = tfValidity.getText();
                String speed = tfSpeed.getText();
                int vouchersPerPage = cbVouchersPerPage.getValue();

                // Check if a file is selected
                if (selectedFile == null) {
                    taCSVfile.setText("Please select a CSV file.");
                    return;
                }

                // Process the CSV file and generate the PDF
                CSVProcessor.processCSV(selectedFile.getAbsolutePath(), price, name, uptime, validity, speed, vouchersPerPage);
                taCSVfile.setText("Vouchers generated successfully!");
            } catch (NumberFormatException e) {
                taCSVfile.setText("Invalid input. Please check your values.");
            } catch (IOException e) {
                taCSVfile.setText("Error generating vouchers: " + e.getMessage());
            }
        });
    }

}