
package McLauncher;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import McLauncher.Utils.Account;
import McLauncher.Utils.ClassPathBuilder;
import McLauncher.Utils.FullGameInstaller;
import McLauncher.Utils.GameProfile;
import McLauncher.Utils.GameProfileLoader;
import McLauncher.Utils.MojanLogin;
import McLauncher.Utils.SaveUtils;
import McLauncher.Utils.Event.Observer;
import McLauncher.Utils.Exception.DownloadFailException;
import McLauncher.Utils.Exception.LoginException;
import McLauncher.Utils.Exception.RefreshProfileFailException;
import McLauncher.Utils.Exception.TokenRefreshException;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Controller implements Initializable {

    @FXML
    private Label userLabel;

    @FXML
    private VBox vbox;

    @FXML
    private Button optionButton;

    @FXML
    private TextField userText;

    @FXML
    private GridPane gridLogged;

    @FXML
    private ImageView faceImg;

    @FXML
    private Label leftLabelBar;

    @FXML
    private Label labelBar;

    @FXML
    private Pane body;

    @FXML
    private Button playButton;

    @FXML
    private Label passwordLabel;

    @FXML
    private GridPane grid;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label rightLabelBar;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label pseudoLabel;

    @FXML
    private Label dlSpeed;

    @FXML
    private Button disconectButton;

    boolean firstTime = true;
    public static Scene dialogScene;
    private GameProfileLoader gameProfileLoader;
    private Logger logger = LogManager.getLogger();
    private SaveUtils saveUtils;
    private ResourceBundle bundle;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        bundle = resources;
        saveUtils = SaveUtils.getINSTANCE();
        SaveUtils.getINSTANCE().checkConfig();
        gameProfileLoader = new GameProfileLoader();

        userText.setText(saveUtils.get("usernameor"));
        if (!userText.textProperty().isEmpty().get() && !passwordField.textProperty().isEmpty().get()) {
            playButton.setDisable(false);
        }
        userText.textProperty().addListener((observable, oldValue, newValue) -> {
            playButton.setDisable(newValue.trim().isEmpty() || passwordField.textProperty().isEmpty().get());

        });
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            playButton.setDisable(newValue.trim().isEmpty() || userText.textProperty().isEmpty().get());

        });
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                new LaunchThread().start();
            }
        });

        playButton.setOnMouseClicked(event -> new LaunchThread().start());

        optionButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
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

        if (gameProfileLoader.isLogged()) {
            userText.setVisible(false);
            passwordField.setVisible(false);
            playButton.setDisable(false);
            userLabel.setVisible(false);
            passwordLabel.setVisible(false);
            gridLogged.setVisible(true);
            pseudoLabel.setText(gameProfileLoader.getAccount().getDisplayName());
            disconectButton.setVisible(true);
            disconectButton.setDisable(false);

            // And as before now you can use URL and URLConnection
            String httpsURL;
            httpsURL = "https://mc-heads.net/head/" + gameProfileLoader.getAccount().getUUID() + "/98.png";
            URL myurl = null;
            try {
                myurl = new URL(httpsURL);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) myurl.openConnection();
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

        disconectButton.setOnMouseClicked(event -> disconnect());

    }

    protected void disconnect() {
        Platform.runLater(() -> {
            passwordField.setDisable(false);
            userText.setVisible(true);
            passwordField.setVisible(true);
            if (passwordField.textProperty().isEmpty().get())
                playButton.setDisable(true);
            else
                playButton.setDisable(false);
            userLabel.setVisible(true);
            passwordLabel.setVisible(true);
            gridLogged.setVisible(false);
            disconectButton.setVisible(false);
            saveUtils.save(new Account("", "", "", "", "", ""));
            gameProfileLoader.setLogged(false);
        });
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
                    labelBar.setText(bundle.getString("auth") + "...");
                    progressBar.setProgress(-1);
                });
                boolean official = SaveUtils.getINSTANCE().get("authType").equals("0");
                MojanLogin mojanLogin = new MojanLogin();
                if (!gameProfileLoader.isLogged())
                    gameProfileLoader
                            .setAccount(mojanLogin.login(userText.getText(), passwordField.getText(), official));
                else {
                    gameProfileLoader.setAccount(mojanLogin.refreshAccount(gameProfileLoader.getAccount(), official));
                }
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
                    final FutureTask wipeQuestion = new FutureTask(new Callable() {
                        @Override
                        public Object call() {
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, bundle.getString("needWipe"));
                            Optional<ButtonType> result = alert.showAndWait();
                            return result.get() == ButtonType.OK;
                        }
                    });
                    Platform.runLater(wipeQuestion);
                    Boolean result = (Boolean) wipeQuestion.get();
                    if (result) {
                        Platform.runLater(() -> {
                            labelBar.setText(bundle.getString("wipe") + "...");
                        });
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

            } catch (LoginException e) {

                logger.warn("Authentication Fail : Wrong User or Password!");
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText(bundle.getString("authFail") + " !");
                    Label label = new Label(bundle.getString("authFail") + " : \n" + e.getMessage());
                    label.setWrapText(true);
                    alert.getDialogPane().setContent(label);
                    alert.setTitle(bundle.getString("error"));
                    progressBar.setProgress(0);
                    labelBar.setText(bundle.getString("authFail") + " !");
                    alert.showAndWait();
                    grid.setDisable(false);
                    userLabel.setVisible(true);
                    passwordLabel.setVisible(true);
                    userText.setVisible(true);
                    passwordField.setVisible(true);
                    disconectButton.setDisable(false);
                    gridLogged.setVisible(false);
                    disconnect();
                });
            } catch (UnknownHostException e) {
                logger.catching(e);
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText(bundle.getString("error") + " !");
                    alert.setContentText(bundle.getString("servFail") + " ! \n(" + e.getMessage() + ")");
                    alert.setTitle(bundle.getString("error"));
                    progressBar.setProgress(0);
                    labelBar.setText(bundle.getString("error") + " !");
                    alert.showAndWait();
                    grid.setDisable(false);
                    userLabel.setVisible(true);
                    passwordLabel.setVisible(true);
                    userText.setVisible(true);
                    passwordField.setVisible(true);
                    disconectButton.setDisable(false);
                    gridLogged.setVisible(false);
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
                final FutureTask offlineQuestion = new FutureTask(new Callable() {
                    @Override
                    public Object call() {
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
                        Optional<ButtonType> result = alert.showAndWait();
                        return result.get() == okBtn;
                    }
                });
                Platform.runLater(offlineQuestion);
                Boolean result;
                try {
                    result = (Boolean) offlineQuestion.get();
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
                    progressBar.setProgress(0);
                    labelBar.setText(bundle.getString("error") + "!");
                    alert.showAndWait();

                });
            }

        }
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
