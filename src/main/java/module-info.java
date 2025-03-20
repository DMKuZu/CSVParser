module com.kuzu.csvparser {
    requires javafx.controls;
    requires javafx.fxml;
    requires commons.csv;
    requires org.apache.pdfbox;
    requires flying.saucer.pdf;


    opens com.kuzu.csvparser to javafx.fxml;
    exports com.kuzu.csvparser;
}