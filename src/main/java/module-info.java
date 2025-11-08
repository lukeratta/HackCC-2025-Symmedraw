module com.lukeratta.hackcc2025 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.lukeratta.hackcc2025 to javafx.fxml;
    exports com.lukeratta.hackcc2025;
}