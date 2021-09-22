package McLauncher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import McLauncher.Utils.LauncherUpdateChecker;
import McLauncher.Utils.OsIdentifer;
import McLauncher.Utils.SaveUtils;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class App extends Application {

    public static String gamePath = OsIdentifer.getInstallPath();


    private static SaveUtils saveUtils;
    private static Stage primaryStageS;
    private static Stage logStage;
    public static Controller controller;
    public static int screenCorecter;
    static Logger logger = LogManager.getLogger();

    @Override
    public void start(Stage primaryStage) throws Exception {
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        Appender appender = LogGuiAppender.createAppender("FxLog", null);
        appender.start();
        config.addAppender(appender);
        AppenderRef ref = AppenderRef.createAppenderRef("FxLog", Level.DEBUG, null);
        config.getLoggerConfig("").addAppender(appender, Level.DEBUG, null);
        ctx.updateLoggers();
//        TODO Change rolling file dir ?

        screenCorecter = 10;
        String os = System.getProperty("os.name");

        if (OsIdentifer.isMac() || OsIdentifer.isLinux())
            screenCorecter = 10;
        saveUtils = SaveUtils.getINSTANCE(gamePath + "launcher.properties");

        if(saveUtils.get("logViewer") != null && saveUtils.get("logViewer").equals("true"))
            openLogs();

        logger.info("/*****************************************************************/");
        logger.info("/**************************Launcher Logs**************************/");
        logger.info("/*****************************************************************/");
        logger.info("OS: " + os);
        logger.info("Install path: " + gamePath);
        logger.info("Java Home: " + System.getProperty("java.home"));
        logger.info("Java Version: " + System.getProperty("java.version"));
        logger.info("Launcher Version: " + LauncherUpdateChecker.getVersion());
        org.apache.logging.log4j.core.Logger loggerImpl = (org.apache.logging.log4j.core.Logger) logger;
        RollingFileAppender Rappender = (RollingFileAppender) loggerImpl.getAppenders().get("RollingFile");
        logger.info("Log File: " + Rappender.getFileName());


        FXMLLoader logLoader = new FXMLLoader();
        logLoader.setLocation(getClass().getResource("/mainPage.fxml"));
        logLoader.setResources(ResourceBundle.getBundle("bundles.MyBundle", Locale.getDefault()));
        Parent root = logLoader.load();

        controller = logLoader.getController();
        primaryStage.setTitle("Mc Launcher SC");
        primaryStage.setScene(new Scene(root, 1014, 605 ));
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(e -> {
            if(logStage != null)
                logStage.close();
            Platform.exit();
        });
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        primaryStageS = primaryStage;


        primaryStage.show();

    }

    public static Stage getPrimaryStage() {
        return primaryStageS;
    }


    public static Stage getLogStage() {
        return logStage;
    }

    public static void setLogStage(Stage logStage){
        App.logStage = logStage;
    }

    public static void main(String[] args) {
        System.setProperty("user.dir", gamePath);
        launch(args);
    }

    public static void openLogs() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(App.class.getResource("/LogViewer.fxml"));
        Parent popup = loader.load();
        logStage = new Stage();
        logStage.initModality(Modality.NONE);
        logStage.initOwner(App.getPrimaryStage());
        logStage.setTitle("Logs");
        logStage.getIcons().add(new Image(App.class.getResourceAsStream("/icon.png")));
        Scene logScene = new Scene(popup, 1090, 600);
        logStage.setScene(logScene);
        logStage.setResizable(true);
        logStage.setOnCloseRequest(event -> {
            logStage = null;
        });
        logStage.show();
    }
}
