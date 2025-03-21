module com.kuzu.csvparser {
    requires javafx.controls;
    requires javafx.fxml;
    requires commons.csv;
    requires org.apache.pdfbox;
    requires java.desktop;


    opens com.kuzu.csvparser to javafx.fxml;
    exports com.kuzu.csvparser;
}