package com.kuzu.csvparser;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

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
    public Slider sliderScale;

    private File selectedFile;

    @FXML
    public void initialize() {

        // Add a label to show percentage
        sliderScale.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double value) {
                return String.format("%.0f%%", value);
            }

            @Override
            public Double fromString(String string) {
                try {
                    return Double.parseDouble(string.replace("%", "")) / 100.0;
                } catch (NumberFormatException ignored) {
                    return 1.5;
                }
            }
        });

        // Update FXML TextArea with current percentage when slider moves
       /* sliderScale.valueProperty().addListener((obs, oldVal, newVal) -> {
            int percentage = (int) (newVal.doubleValue() * 100);
            taCSVfile.setText(String.format("Scale: %d%%", percentage));
        });*/

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
                double voucherScale = sliderScale.getValue() / 100;

                // Check if a file is selected
                if (selectedFile == null) {
                    taCSVfile.setText("Please select a CSV file.");
                    return;
                }

                // Process the CSV file and generate the PDF
                CSVProcessor.processCSV(selectedFile.getAbsolutePath(), price, name, uptime, validity, speed, voucherScale);
                taCSVfile.setText("Vouchers located at: " + selectedFile.getParent());
                System.out.println(price + " " + name + " " + uptime + " " + validity + " " + speed + " " + voucherScale);
            } catch (NumberFormatException e) {
                taCSVfile.setText("Invalid input. Please check your values.");
            } catch (IOException e) {
                taCSVfile.setText("Error generating vouchers: " + e.getMessage());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

}