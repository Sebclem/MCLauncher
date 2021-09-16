module McLauncher {
    requires java.desktop;
    requires jdk.crypto.ec;
    requires org.apache.logging.log4j;
    requires org.apache.commons.io;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires javafx.fxml;
    requires transitive javafx.controls;
    requires com.google.gson;
    exports McLauncher;
    exports McLauncher.Json to com.google.gson;
    opens McLauncher to javafx.fxml;
}
