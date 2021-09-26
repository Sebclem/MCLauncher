module McLauncher {

    requires java.net.http;
    requires jdk.crypto.ec;


    requires java.base;
    requires java.desktop;

    requires org.apache.logging.log4j;
    requires org.apache.commons.io;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.web;
    requires transitive javafx.controls;
    requires com.google.gson;
    requires org.apache.logging.log4j.core;
    requires org.apache.commons.compress;
    exports McLauncher;
    exports McLauncher.Json to com.google.gson;
    exports McLauncher.Json.Auth.Msa to com.google.gson;
    opens McLauncher.Auth to javafx.fxml;
    opens McLauncher to javafx.fxml;


}
