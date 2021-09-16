module McLauncher {
    requires java.desktop;
    requires jdk.crypto.ec;
    requires org.apache.logging.log4j;
    requires org.apache.commons.io;
    requires javafx.fxml;
    requires transitive javafx.controls;
    requires com.google.gson;
    exports McLauncher;
    opens McLauncher to javafx.fxml;
}
