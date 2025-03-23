module com.kuzu.csvparser {
    requires javafx.controls;
    requires javafx.fxml;
    requires commons.csv;
    requires java.desktop;
    requires openhtmltopdf.pdfbox;
    requires openhtmltopdf.core;
    requires java.logging;
    requires org.jsoup;
    requires jdk.compiler;


    opens com.kuzu.csvparser to javafx.fxml;
    exports com.kuzu.csvparser;
}