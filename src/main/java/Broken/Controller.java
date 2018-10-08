
package Broken;

import Broken.Utils.*;

import Broken.Utils.Exception.DownloadFailException;
import Broken.Utils.Exception.LoadingSaveException;
import Broken.Utils.Exception.LoginException;
import Broken.Utils.Exception.TokenRefreshException;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;

public class Controller {


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
    //    public static ThreadSpeed threadSpeed;
    public static Scene dialogScene;
    Account account;
    boolean isLogged = false;
    Logger logger = LogManager.getLogger();
    SaveUtils saveUtils;


    @FXML
    void initialize() throws MalformedURLException {
        saveUtils = SaveUtils.getINSTANCE();
        SaveUtils.getINSTANCE().checkConfig();

        try {
            account = saveUtils.getAccount();
            isLogged = true;
        } catch (LoadingSaveException e) {
            logger.info(e.getMessage());
            isLogged = false;
        }

//        threadSpeed = new ThreadSpeed();
//        dlListenner = new DlListenner();
        userText.setText(saveUtils.get("username"));
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
                    Parent popup = FXMLLoader.load(getClass().getResource("/option.fxml"));
                    final Stage dialog = new Stage();
                    dialog.initModality(Modality.APPLICATION_MODAL);
                    dialog.initOwner(Main.getPrimaryStage());
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

        if (isLogged) {
            userText.setVisible(false);
            passwordField.setVisible(false);
            playButton.setDisable(false);
            userLabel.setVisible(false);
            passwordLabel.setVisible(false);
            gridLogged.setVisible(true);
            pseudoLabel.setText(account.getDisplayName());
            disconectButton.setVisible(true);
            disconectButton.setDisable(false);

            // And as before now you can use URL and URLConnection
            String httpsURL ;
            httpsURL = "https://mc-heads.net/head/" + account.getUUID()+"/98.png";
            URL myurl = new URL(httpsURL);
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection)myurl.openConnection();
                con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
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
            isLogged = false;
        });
    }


    class LaunchThread extends Thread {
        @Override
        public void run() {
            Platform.runLater(() ->
            {
                grid.setDisable(true);
                disconectButton.setDisable(true);
                labelBar.setText("Authentification...");
                progressBar.setProgress(-1);
            });

            try {
                MojanLogin mojanLogin = new MojanLogin();
                if (!isLogged)
                    account = mojanLogin.login(userText.getText(), passwordField.getText());
                else{
                    account = mojanLogin.refreshAccount(account);
                }
                if(account == null){
                    throw new TokenRefreshException();
                }

                Platform.runLater(() -> {
                    progressBar.setProgress(-1);
                    leftLabelBar.setText("");
                    rightLabelBar.setText("");
                    leftLabelBar.setText("");
                    dlSpeed.setText("");
                    labelBar.setText("Checking game files...");

                });

                FullGameInstaller gameInstaller = new FullGameInstaller();
                gameInstaller.init(Main.gamePath);
                gameInstaller.addObserver(new DlListenner());
                gameInstaller.download(Main.gamePath, Main.version);
                String cassPath = new ClassPathBuilder(Main.gamePath).build();

                GameProfile gameProfile = new GameProfile(account, saveUtils.get("ramMax"), saveUtils.get("assetId"), Main.gamePath, Main.version, cassPath , GameProfile.MainClass.FORGE, saveUtils.get("logConfigPath") );



                Platform.runLater(() -> {
                    progressBar.setProgress(-1);
                    leftLabelBar.setText("");
                    rightLabelBar.setText("");
                    leftLabelBar.setText("");
                    dlSpeed.setText("");
                    labelBar.setText("Lancement du jeu...");

                });

                Thread threadLaunch= new Thread(() -> {

                    try{
                        gameProfile.launch();
                    }catch (IOException | InterruptedException e)
                    {
                        logger.error(e.getMessage());
                        Platform.runLater(()->
                        {
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setHeaderText("Echec de lancement!");
                            alert.setContentText("Echec lors du lancement du jeu:\n"+e.getMessage());
                            alert.setTitle("Erreur");
                            progressBar.setProgress(0);
                            labelBar.setText("Echec de lancement du jeu!");
                            alert.showAndWait();
                            grid.setDisable(false);
                        });
                    }

                });

//                threadLaunch.start();

                try {
                    Thread.sleep(10000);

                } catch (InterruptedException e) {
                    logger.catching(e);
                }
                Platform.runLater(() -> labelBar.getScene().getWindow().hide());


            } catch (LoginException e) {


                logger.warn("Authentication Fail : Wrong User or Password!");
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText("Echec d'Authentification!");
                    Label label = new Label("Echec d'Authentification : \n" + e.getMessage());
                    label.setWrapText(true);
                    alert.getDialogPane().setContent(label);
                    alert.setTitle("Erreur");
                    progressBar.setProgress(0);
                    labelBar.setText("Echec d'Authentification!");
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
                    alert.setHeaderText("Erreur !");
                    alert.setContentText("Impossible de contacter le server! \n(" + e.getMessage() + ")");
                    alert.setTitle("Erreur");
                    progressBar.setProgress(0);
                    labelBar.setText("Erreur !");
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
                    alert.setHeaderText("Erreur !");
                    alert.setContentText("Ereur : " + e.getClass().toString());
                    alert.setTitle("Erreur");
                    progressBar.setProgress(0);
                    labelBar.setText("Erreur !");
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
            } catch (DownloadFailException e) {
                logger.catching(e);
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText("Download Error!");
                    alert.setContentText("Une erreur est survenue lors du téléchargement.");
                    alert.setTitle("Erreur");
                    progressBar.setProgress(0);
                    labelBar.setText("Download Error!");
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
            } catch (TokenRefreshException e) {
                logger.info("Refresh token fail. Please re-login.");
                Platform.runLater(() -> {
                    progressBar.setProgress(0);
                    labelBar.setText("Echec d'Authentification!");
                    grid.setDisable(false);
                    userLabel.setVisible(true);
                    passwordLabel.setVisible(true);
                    userText.setVisible(true);
                    passwordField.setVisible(true);
                    disconectButton.setDisable(false);
                    gridLogged.setVisible(false);
                    disconnect();
                });
            }

        }
    }


//    class ThreadSpeed extends Thread{
//        @Override
//        public void run() {
//            DecimalFormat myFormatter = new DecimalFormat("##0.0");
//            DecimalFormat timeFormatter = new DecimalFormat("00");
//            long save;
//            long val;
//            long max;
//            int seconde;
//
//            Platform.runLater(()->{
//                rightLabelBar.setVisible(true);
//                dlSpeed.setVisible(true);
//                dlSpeed.setVisible(true);
//                dlSpeed.setText("  -  --MB/s");
//                rightLabelBar.setText("--min --sec");
//            });
//            while(!this.isInterrupted())
//            {
//                save = BarAPI.getNumberOfTotalDownloadedBytes()/1000;
//                try {
//                    Thread.sleep(1000);
//                    max = BarAPI.getNumberOfTotalBytesToDownload()/1000;
//                    val = BarAPI.getNumberOfTotalDownloadedBytes()/1000;
//                    final double speed = (val-save);
//
//
//                    seconde= (int) ((max-val)/speed);
//                    int hours = seconde / 3600;
//                    int remainder = seconde - hours * 3600;
//                    int mins = remainder / 60;
//                    remainder = remainder - mins * 60;
//                    int secs = remainder;
//                    String toDisplay="";
//                    if(hours>=1)
//                        toDisplay=toDisplay+hours+"h ";
//                    if(mins>=1||hours>=1)
//                        toDisplay=toDisplay+timeFormatter.format(mins)+"min ";
//                    toDisplay=toDisplay+timeFormatter.format(secs)+"sec";
//                    String finalToDisplay = toDisplay;
//                    if(speed>1000)
//                        Platform.runLater(()->dlSpeed.setText("  -  "+myFormatter.format(speed/1000)+"MB/s"));
//                    else
//                        Platform.runLater(()->dlSpeed.setText("  -  "+myFormatter.format(speed)+"kB/s"));
//                    Platform.runLater(()->rightLabelBar.setText(finalToDisplay));
//
//                } catch (InterruptedException e) {
//                    this.interrupt();
//                }
//
//            }
//
//        }
//
//    }

    class DlListenner implements Observer {
        private long old = 0;


        @Override
        public void update(Observable observable, Object o) {
            FullGameInstaller installer = (FullGameInstaller) observable;

            DecimalFormat myFormatter = new DecimalFormat("##0.00");

            if (old != installer.downloaded / 1000) {
                old = installer.downloaded / 1000;

                double pour = ((old * 1.0) / (installer.totalSize / 1000.0)) * 100;

//                logger.debug(old /1000+ "M/" + installer.totalSize / 1000000.0 + "M -> " + myFormatter.format(pour));
                Platform.runLater(() -> {
                    progressBar.setProgress(pour / 100);
                    leftLabelBar.setText(old / 1000 + "MB / " + installer.totalSize / 1000000 + "MB");
                    if (pour > 100)
                        labelBar.setText("Téléchargement: 100%");
                    else
                        labelBar.setText("Téléchargement: " + myFormatter.format(pour) + "%");

                });
            }

        }
    }
}
