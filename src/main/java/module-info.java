module com.kuzu.csvparser {
    requires javafx.controls;
    requires javafx.fxml;
    requires commons.csv;
    requires org.apache.pdfbox;
    requires java.desktop;
    requires openhtmltopdf.pdfbox;
    requires openhtmltopdf.core;
    requires java.logging;
    requires org.jsoup;


    opens com.kuzu.csvparser to javafx.fxml;
    exports com.kuzu.csvparser;
}