package McLauncher;

import McLauncher.Auth.AbstractLogin;
import McLauncher.Auth.Account;
import McLauncher.Auth.MojanLogin;
import McLauncher.Auth.MsaLogin;
import McLauncher.Json.LauncherUpdateResponse;
import McLauncher.Utils.*;
import McLauncher.Utils.Event.Observer;
import McLauncher.Utils.Exception.DownloadFailException;
import McLauncher.Utils.Exception.RefreshProfileFailException;
import McLauncher.Utils.Exception.TokenRefreshException;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.text.DecimalFormat;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class Controller implements Initializable {

    public static Scene dialogScene;
    @FXML
    private GridPane grid;
    @FXML
    private Label userLabel;
    @FXML
    private Label passwordLabel;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button optionButton;
    @FXML
    private Button playButton;
    @FXML
    private TextField userText;
    @FXML
    private Button logMsaBtn;
    @FXML
    private Button disconectButton;
    @FXML
    private GridPane gridLogged;
    @FXML
    private Label pseudoLabel;
    @FXML
    private ImageView faceImg;
    @FXML
    private TitledPane updateNotification;
    @FXML
    private Label oldVersion;
    @FXML
    private Label newVersion;
    @FXML
    private Button updateBtn;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label labelBar;
    @FXML
    private Label leftLabelBar;
    @FXML
    private Label dlSpeed;
    @FXML
    private Label rightLabelBar;

    private GameProfileLoader gameProfileLoader;
    private final Logger logger = LogManager.getLogger();
    private SaveUtils saveUtils;
    private ResourceBundle bundle;
    private AbstractLogin logManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        bundle = resources;
        saveUtils = SaveUtils.getINSTANCE();
        SaveUtils.getINSTANCE().checkConfig();
        gameProfileLoader = new GameProfileLoader();

        userText.setText(saveUtils.get("username"));
        if (!userText.textProperty().isEmpty().get() && !passwordField.textProperty().isEmpty().get()) {
            playButton.setDisable(false);
        }
        userText.textProperty().addListener((observable, oldValue, newValue) -> playButton.setDisable(newValue.trim().isEmpty() || passwordField.textProperty().isEmpty().get()));
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> playButton.setDisable(newValue.trim().isEmpty() || userText.textProperty().isEmpty().get()));
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                new LaunchThread().start();
            }
        });

        playButton.setOnMouseClicked(event -> {
            if(gameProfileLoader.isLogged())
                new LaunchThread().start();
            else{
                Platform.runLater(() -> {
                    grid.setDisable(true);
                    disconectButton.setDisable(true);
                    labelBar.setText(bundle.getString("auth") + "...");
                    progressBar.setProgress(-1);
                });
                logManager.login(userText.getText(), passwordField.getText());
            }
        });

        optionButton.setOnMouseClicked(new EventHandler<>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("/option.fxml"));
                    loader.setResources(bundle);
                    Parent popup = loader.load();
                    final Stage dialog = new Stage();
                    dialog.initModality(Modality.APPLICATION_MODAL);
                    dialog.initOwner(App.getPrimaryStage());
                    dialog.setTitle("Option");
                    dialog.getIcons().add(new Image(getClass().getResourceAsStream("/settingsIcon.png")));
                    dialogScene = new Scene(popup, 400, 257);
                    dialog.setScene(dialogScene);
                    dialog.setResizable(false);
                    dialog.show();
                } catch (IOException e) {
                    logger.catching(e);
                }
            }
        });
        disconectButton.setOnMouseClicked(event -> disconnect());
        logMsaBtn.setOnMouseClicked(event -> {
            progressBar.setProgress(-1);
            labelBar.setText(bundle.getString("auth"));
            logManager.login(null, null);
        });
        new Thread(() -> {
            String currentVersion = LauncherUpdateChecker.getVersion();
            try {
                LauncherUpdateResponse lastVersion = LauncherUpdateChecker.getLastVersion();
                if (!currentVersion.equals(lastVersion.tag_name)) {
                    showUpdateNotification(currentVersion, lastVersion.tag_name, lastVersion.html_url);
                }
            } catch (IOException e) {
                logger.warn("Fail to check for launcher update !");
                logger.catching(e);
            }
        }).start();

        updateVisibility();
        updateLogManger();


    }

    protected void disconnect() {
        Platform.runLater(() -> {

            saveUtils.save(new Account("", "", "", "", null, "", ""));
            gameProfileLoader.setLogged(false);
            updateVisibility();
        });
    }

    protected void settingsChanged(){
        Platform.runLater(this::updateVisibility);
    }
    private void updateLogManger(){
        if (SaveUtils.getINSTANCE().get("authType").equals("1"))
            logManager = new MsaLogin();
        else
            logManager = new MojanLogin();
        setupLoginEventListeners();
    }

    private void logStateChanged(){
        Platform.runLater(this::updateVisibility);
        updateLogManger();
    }
    void updateVisibility(){
        if(!gameProfileLoader.isLogged()){
            if (SaveUtils.getINSTANCE().get("authType").equals("1")) {
                passwordField.setVisible(false);
                passwordLabel.setVisible(false);
                userText.setVisible(false);
                userLabel.setVisible(false);
                logMsaBtn.setVisible(true);
            }
            else{
                passwordField.setVisible(true);
                passwordLabel.setVisible(true);
                userText.setVisible(true);
                userLabel.setVisible(true);
                logMsaBtn.setVisible(false);
                playButton.setDisable(passwordField.textProperty().isEmpty().get());

            }
            playButton.setDisable(true);
            disconectButton.setVisible(false);
            gridLogged.setVisible(false);
        }else{
            passwordField.setVisible(false);
            passwordLabel.setVisible(false);
            userText.setVisible(false);
            userLabel.setVisible(false);
            logMsaBtn.setVisible(false);
            playButton.setDisable(false);
            disconectButton.setVisible(true);
            passwordLabel.setVisible(false);
            gridLogged.setVisible(true);
            pseudoLabel.setText(gameProfileLoader.getAccount().getDisplayName());
            try {
                String httpsURL;
                httpsURL = "https://mc-heads.net/head/" + gameProfileLoader.getAccount().getUUID() + "/98.png";
                URL myurl = new URL(httpsURL);
                HttpURLConnection con = (HttpURLConnection) myurl.openConnection();
                con.setRequestProperty("User-Agent",
                        "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
                InputStream ins = con.getInputStream();
                Image image = new Image(ins);
                faceImg.setImage(image);
                ins.close();
            } catch (IOException e) {
                logger.catching(e);
            }
        }
    }

    private void setupLoginEventListeners(){
        logManager.setOnLoginCancel(loginProscesor -> Platform.runLater(() -> {
            progressBar.setProgress(0);
            labelBar.setText("");
            logMsaBtn.setDisable(false);
        }));
        logManager.setOnBadCredentials(loginProscesor -> {
            logger.warn("Authentication Fail : Wrong User or Password!");
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setHeaderText(bundle.getString("authFail") + " !");
                Label label = new Label(bundle.getString("authFail") + " : \n" + loginProscesor.getException().getMessage());
                label.setWrapText(true);
                alert.getDialogPane().setContent(label);
                alert.setTitle(bundle.getString("error"));
                alert.getDialogPane().getStylesheets().add("alert.css");
                progressBar.setProgress(0);
                labelBar.setText(bundle.getString("authFail") + " !");
                logMsaBtn.setDisable(false);
                alert.showAndWait();
                disconnect();
            });
        });
        logManager.setOnConnectionError(loginProscesor -> {
            logger.catching(loginProscesor.getException());
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setHeaderText(loginProscesor.getException().getClass().toString());
                alert.setContentText(bundle.getString("error") + " : " + loginProscesor.getException().getMessage());
                alert.setTitle(bundle.getString("error"));
                alert.getDialogPane().getStylesheets().add("alert.css");
                progressBar.setProgress(0);
                logMsaBtn.setDisable(false);
                labelBar.setText(bundle.getString("error") + "!");
                alert.showAndWait();
            });
        });
        logManager.setOnLoginSuccess(loginProscesor -> {
            gameProfileLoader.setAccount(loginProscesor.getAccount());
            gameProfileLoader.setLogged(true);
            gameProfileLoader.updateCanOffline();
            Platform.runLater(()->{
                progressBar.setProgress(0);
                labelBar.setText("");
                logMsaBtn.setDisable(false);
            });
            logStateChanged();
            new LaunchThread().start();
        });
    }

    private void launchGame() {

        String cassPath = new ClassPathBuilder(App.gamePath).build();

        GameProfile gameProfile = new GameProfile(gameProfileLoader.getAccount(), saveUtils.get("ramMax"),
                saveUtils.get("assetId"), App.gamePath, gameProfileLoader.getVersion(), cassPath,
                gameProfileLoader.getMainClass(), saveUtils.get("logConfigPath"));

        Platform.runLater(() -> {
            progressBar.setProgress(-1);
            leftLabelBar.setText("");
            rightLabelBar.setText("");
            leftLabelBar.setText("");
            dlSpeed.setText("");
            labelBar.setText(bundle.getString("launch") + "...");

        });

        Thread threadLaunch = new Thread(() -> {
            try {
                gameProfile.launch();
            } catch (IOException | InterruptedException e) {
                logger.error(e.getMessage());
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText("Echec de lancement!");
                    alert.setContentText("Echec lors du lancement du jeu:\n" + e.getMessage());
                    alert.setTitle("Erreur");
                    alert.getDialogPane().getStylesheets().add("alert.css");
                    progressBar.setProgress(0);
                    labelBar.setText("Echec de lancement du jeu!");
                    alert.showAndWait();
                    grid.setDisable(false);
                });
            }
        });
        threadLaunch.start();

        try {
            Thread.sleep(10000);

        } catch (InterruptedException e) {
            logger.catching(e);
        }
        Platform.runLater(() -> labelBar.getScene().getWindow().hide());
    }

    private void showUpdateNotification(String currentVersion, String newVersionStr, String url) {
        Platform.runLater(() -> {
            oldVersion.setText("Current Version: " + currentVersion);
            newVersion.setText("New Version: " + newVersionStr);
        });

        updateBtn.setOnMouseClicked((event) -> {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                logger.catching(e);
            }
        });
        FadeTransition ft = new FadeTransition(Duration.millis(500), updateNotification);
        ft.setFromValue(0);
        ft.setToValue(1.0);
        updateNotification.setVisible(true);
        ft.play();

    }



    class LaunchThread extends Thread {
        @Override
        public void run() {
            Platform.runLater(() -> {
                grid.setDisable(true);
                disconectButton.setDisable(true);
                labelBar.setText(bundle.getString("dlProfile") + "...");
                progressBar.setProgress(-1);
            });

            try {
                gameProfileLoader.refreshProfile();

                Platform.runLater(() -> {
                    grid.setDisable(true);
                    disconectButton.setDisable(true);
                    labelBar.setText(bundle.getString("tokenRefresh") + "...");
                    progressBar.setProgress(-1);
                });
                gameProfileLoader.setAccount(logManager.refreshToken(gameProfileLoader.getAccount()));
                if (gameProfileLoader.getAccount() == null) {
                    throw new TokenRefreshException();
                }

                Platform.runLater(() -> {
                    progressBar.setProgress(-1);
                    leftLabelBar.setText("");
                    rightLabelBar.setText("");
                    leftLabelBar.setText("");
                    dlSpeed.setText("");
                    labelBar.setText(bundle.getString("checkFiles") + "...");

                });

                FullGameInstaller gameInstaller = new FullGameInstaller();
                if (gameProfileLoader.needWipe()) {
                    final FutureTask<Boolean> wipeQuestion = new FutureTask<>(() -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, bundle.getString("needWipe"));
                        alert.getDialogPane().getStylesheets().add("alert.css");
                        Optional<ButtonType> result = alert.showAndWait();
                        return result.filter(buttonType -> buttonType == ButtonType.OK).isPresent();
                    });
                    Platform.runLater(wipeQuestion);
                    Boolean result = wipeQuestion.get();
                    if (result) {
                        Platform.runLater(() -> labelBar.setText(bundle.getString("wipe") + "..."));
                        gameInstaller.wipper(App.gamePath, gameProfileLoader);
                    } else {
                        Platform.runLater(() -> {
                            progressBar.setProgress(0);
                            labelBar.setText("");
                            disconectButton.setDisable(false);
                            grid.setDisable(false);

                        });
                        gameProfileLoader.resetPackUUID();
                        return;
                    }

                }
                gameInstaller.init(App.gamePath, gameProfileLoader.getVersion());
                gameInstaller.addObserver(new DlListenner());
                gameInstaller.download(App.gamePath, gameProfileLoader.getVersion());
                launchGame();

            } catch (UnknownHostException e) {
                logger.catching(e);
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText(bundle.getString("error") + " !");
                    alert.setContentText(bundle.getString("servFail") + " ! \n(" + e.getMessage() + ")");
                    alert.setTitle(bundle.getString("error"));
                    alert.getDialogPane().getStylesheets().add("alert.css");
                    progressBar.setProgress(0);
                    labelBar.setText(bundle.getString("error") + " !");
                    alert.showAndWait();
                    disconnect();

                });
                grid.setDisable(false);
            } catch (IOException | InterruptedException e) {
                logger.catching(e);
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText("Error !");
                    alert.setContentText(bundle.getString("error") + " : " + e.getClass().toString());
                    alert.setTitle(bundle.getString("error"));
                    alert.getDialogPane().getStylesheets().add("alert.css");
                    progressBar.setProgress(0);
                    labelBar.setText(bundle.getString("error") + "!");
                    alert.showAndWait();

                });
                grid.setDisable(false);
            } catch (DownloadFailException e) {
                logger.catching(e);
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText(bundle.getString("dlFail"));
                    alert.setContentText(bundle.getString("dlFailLong"));
                    alert.setTitle(bundle.getString("error"));
                    alert.getDialogPane().getStylesheets().add("alert.css");
                    progressBar.setProgress(0);
                    labelBar.setText(bundle.getString("dlFail"));
                    alert.showAndWait();

                });
                grid.setDisable(false);
            } catch (TokenRefreshException e) {
                logger.info("Refresh token fail. Please re-login.");
                Platform.runLater(() -> {
                    progressBar.setProgress(0);
                    labelBar.setText(bundle.getString("authFail") + " !");
                    grid.setDisable(false);
                    userLabel.setVisible(true);
                    passwordLabel.setVisible(true);
                    userText.setVisible(true);
                    passwordField.setVisible(true);
                    disconectButton.setDisable(false);
                    gridLogged.setVisible(false);
                    disconnect();
                });
            } catch (RefreshProfileFailException e) {
                logger.catching(e);
                final FutureTask<Boolean> offlineQuestion = new FutureTask<>(() -> {
                    ButtonType okBtn = new ButtonType("Ok", ButtonBar.ButtonData.CANCEL_CLOSE);
                    ButtonType offlineBtn = new ButtonType("Play Offline", ButtonBar.ButtonData.OK_DONE);
                    Alert alert = new Alert(Alert.AlertType.ERROR, "", offlineBtn, okBtn);
                    alert.setHeaderText(bundle.getString("dlFail"));
                    if (gameProfileLoader.canOffline())
                        alert.setContentText(bundle.getString("profileErrorOffline"));
                    else {
                        alert.setContentText(bundle.getString("profileErrorLong"));
                        alert.getDialogPane().lookupButton(offlineBtn).setDisable(true);
                    }

                    alert.setTitle(bundle.getString("error"));
                    alert.getDialogPane().getStylesheets().add("alert.css");
                    Optional<ButtonType> result = alert.showAndWait();
                    return result.filter(buttonType -> buttonType == okBtn).isPresent();
                });
                Platform.runLater(offlineQuestion);
                boolean result;
                try {
                    result = offlineQuestion.get();
                    if (result) {
                        Platform.runLater(() -> {
                            progressBar.setProgress(0);
                            labelBar.setText(bundle.getString("profileError"));
                            disconectButton.setDisable(false);
                            grid.setDisable(false);

                        });
                    } else {
                        launchGame();
                    }
                } catch (InterruptedException | ExecutionException e1) {
                    Platform.runLater(() -> {
                        progressBar.setProgress(0);
                        labelBar.setText(bundle.getString("profileError"));
                        disconectButton.setDisable(false);
                        grid.setDisable(false);

                    });
                }

            } catch (Exception e) {
                logger.catching(e);
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText("Error !");
                    alert.setContentText(bundle.getString("error") + " : " + e.getClass().toString());
                    alert.setTitle(bundle.getString("error"));
                    alert.getDialogPane().getStylesheets().add("alert.css");
                    progressBar.setProgress(0);
                    labelBar.setText(bundle.getString("error") + "!");
                    disconectButton.setDisable(false);
                    grid.setDisable(false);
                    alert.showAndWait();

                });
            }

        }
    }

    class DlListenner implements Observer {
        private long old = 0;

        @Override
        public void update(Object subObject) {
            FullGameInstaller installer = (FullGameInstaller) subObject;

            DecimalFormat myFormatter = new DecimalFormat("##0.00");

            if (old != installer.downloaded / 1000) {
                old = installer.downloaded / 1000;

                double pour = ((old * 1.0) / (installer.totalSize / 1000.0)) * 100;

                // logger.debug(old /1000+ "M/" + installer.totalSize / 1000000.0 + "M -> " +
                // myFormatter.format(pour));
                Platform.runLater(() -> {
                    progressBar.setProgress(pour / 100);
                    leftLabelBar.setText(old / 1000 + "MB / " + installer.totalSize / 1000000 + "MB");
                    if (pour > 100)
                        labelBar.setText(bundle.getString("download") + ": 100%");
                    else
                        labelBar.setText(bundle.getString("download") + ": " + myFormatter.format(pour) + "%");

                });
            }

        }
    }
}
