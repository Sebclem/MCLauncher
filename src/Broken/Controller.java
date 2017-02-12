package Broken;

import com.sun.org.apache.bcel.internal.generic.LADD;
import fr.theshark34.openauth.AuthenticationException;
import fr.theshark34.openlauncherlib.LaunchException;
import fr.theshark34.supdate.BarAPI;
import fr.theshark34.supdate.SUpdate;
import fr.theshark34.supdate.exception.BadServerResponseException;
import fr.theshark34.supdate.exception.BadServerVersionException;
import fr.theshark34.supdate.exception.ServerDisabledException;
import fr.theshark34.supdate.exception.ServerMissingSomethingException;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.Time;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Stack;

public class Controller {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button optionButton;

    @FXML
    private TextField userText;

    @FXML
    private Label labelBar;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button playButton;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Pane body;

    @FXML
    private GridPane grid;

    @FXML
    private VBox vbox;

    @FXML
    private Label leftLabelBar;

    @FXML
    private Label rightLabelBar;

    @FXML
    private Label dlSpeed;



    boolean firstTime = true;
    ThreadSpeed threadSpeed = new ThreadSpeed();


    @FXML
    void initialize() {

        userText.setText(Main.saver.get("username"));
        if(!userText.textProperty().isEmpty().get()&&!passwordField.textProperty().isEmpty().get())
        {
            playButton.setDisable(false);
        }
        Platform.runLater(()->optionButton.requestFocus());
        userText.textProperty().addListener((observable, oldValue, newValue) -> {
            playButton.setDisable(newValue.trim().isEmpty()||passwordField.textProperty().isEmpty().get());

        });
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            playButton.setDisable(newValue.trim().isEmpty()||userText.textProperty().isEmpty().get());

        });
        passwordField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode()== KeyCode.ENTER)
                {
                    new LaunchThread().start();
                }
            }
        });


        playButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                new LaunchThread().start();
            }
        });






    }


    class LaunchThread extends Thread{
        @Override
        public void run() {
            Platform.runLater(()->
            {
                grid.setDisable(true);
                labelBar.setText("Authentification...");
                //progressBar.getStylesheets().clear();
                progressBar.setProgress(-1);
            });

            try {
                Launcher.auth(userText.getText(),passwordField.getText());
                Main.saver.set("username",userText.getText());
                SUpdate su = Launcher.update();
                DlListenner dlListenner = new DlListenner();
                dlListenner.start();
                su.start();
                dlListenner.interrupt();
                threadSpeed.interrupt();
                Platform.runLater(() -> {
                    //progressBar.getStylesheets().clear();
                    progressBar.setProgress(-1);
                    leftLabelBar.setText("");
                    rightLabelBar.setText("");
                    leftLabelBar.setText("");
                    dlSpeed.setText("");
                    labelBar.setText("Lancement du jeu...");

                });

                Thread threadLaunch=new Thread(){
                    @Override
                    public void run(){

                        try{
                            Launcher.lauch();
                        }catch (LaunchException e)
                        {
                            System.out.println(e.getMessage());
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

                    }
                };

                threadLaunch.start();

                try {
                    Thread.sleep(5000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Platform.runLater(() ->labelBar.getScene().getWindow().hide());


            } catch (AuthenticationException e) {
                System.out.println(e.getErrorModel().getCause()+"   "+e.getErrorModel().getError()+"    "+e.getErrorModel().getErrorMessage());

                Platform.runLater(()->{
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText("Echec d'Authentification!");
                    alert.setContentText("Echec d'Authentification: "+e.getErrorModel().getErrorMessage());
                    alert.setTitle("Erreur");
                    progressBar.setProgress(0);
                    labelBar.setText("Echec d'Authentification!");
                    alert.showAndWait();
                    grid.setDisable(false);
                });

            } catch (IOException e) {
                e.printStackTrace();
                grid.setDisable(false);
            } catch (BadServerVersionException |ServerMissingSomethingException e) {
                e.printStackTrace();
                Platform.runLater(()->{
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText("Erreur Serveur!");
                    alert.setContentText("Ereur Serveur: "+e.getMessage());
                    alert.setTitle("Erreur");
                    progressBar.setProgress(0);
                    labelBar.setText("Erreur serveur!");
                    alert.showAndWait();
                    grid.setDisable(false);
                });
                grid.setDisable(false);

            } catch (ServerDisabledException e) {
                e.printStackTrace();
                Platform.runLater(()->{
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText("Erreur Serveur!");
                    alert.setContentText("Le serveur de mise à jour est désactivé.");
                    alert.setTitle("Erreur");
                    progressBar.setProgress(0);
                    labelBar.setText("Erreur serveur!");
                    alert.showAndWait();
                    grid.setDisable(false);
                });
                grid.setDisable(false);
            } catch (BadServerResponseException e) {
                e.printStackTrace();
                Platform.runLater(()->{
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText("Erreur Serveur!");
                    alert.setContentText("Mauvaise reponsse du serveur: \n"+e.getMessage());
                    alert.setTitle("Erreur");
                    progressBar.setProgress(0);
                    labelBar.setText("Erreur serveur!");
                    alert.showAndWait();
                    grid.setDisable(false);
                });
                grid.setDisable(false);
            }
        }
    }



    class ThreadSpeed extends Thread{
        @Override
        public void run() {
            DecimalFormat myFormatter = new DecimalFormat("##0.0");
            DecimalFormat timeFormatter = new DecimalFormat("00");
            long save;
            long val;
            long max;
            int seconde;

            Platform.runLater(()->{
                rightLabelBar.setVisible(true);
                dlSpeed.setVisible(true);
                dlSpeed.setVisible(true);
                dlSpeed.setText("  -  --MB/s");
                rightLabelBar.setText("--min --sec");
            });
            while(!this.isInterrupted())
            {
                save = BarAPI.getNumberOfTotalDownloadedBytes()/1000;
                try {
                    Thread.sleep(1000);
                    max = BarAPI.getNumberOfTotalBytesToDownload()/1000;
                    val = BarAPI.getNumberOfTotalDownloadedBytes()/1000;
                    final double speed = (val-save);


                    seconde= (int) ((max-val)/speed);
                    int hours = seconde / 3600;
                    int remainder = seconde - hours * 3600;
                    int mins = remainder / 60;
                    remainder = remainder - mins * 60;
                    int secs = remainder;
                    String toDisplay="";
                    if(hours>=1)
                        toDisplay=toDisplay+hours+"h ";
                    if(mins>=1||hours>=1)
                        toDisplay=toDisplay+timeFormatter.format(mins)+"min ";
                    toDisplay=toDisplay+timeFormatter.format(secs)+"sec";
                    String finalToDisplay = toDisplay;
                    if(speed>1000)
                        Platform.runLater(()->dlSpeed.setText("  -  "+myFormatter.format(speed/1000)+"MB/s"));
                    else
                        Platform.runLater(()->dlSpeed.setText("  -  "+myFormatter.format(speed)+"kB/s"));
                    Platform.runLater(()->rightLabelBar.setText(finalToDisplay));

                } catch (InterruptedException e) {
                    this.interrupt();
                }

            }

        }

    }

    class DlListenner extends Thread
    {
        long value = BarAPI.getNumberOfTotalDownloadedBytes()/1000;
        long max = BarAPI.getNumberOfTotalBytesToDownload()/1000;


        @Override
        public void run() {

            Platform.runLater(()-> labelBar.setText("Verification des fichiers..."));

            while (max == 0) max = BarAPI.getNumberOfTotalBytesToDownload() / 1000;


            threadSpeed.start();


            Platform.runLater(()-> {
                labelBar.setText("Télécharment: 0.00%");
                leftLabelBar.setText("0MB / 0MB");
                //progressBar.getStylesheets().add("Broken/Resources/triped-progress.css");
                progressBar.setProgress(0);
            });

            DecimalFormat myFormatter = new DecimalFormat("##0.00");
            while (!this.isInterrupted()) {
                if (value != BarAPI.getNumberOfTotalDownloadedBytes() / 1000) {
                    value = BarAPI.getNumberOfTotalDownloadedBytes() / 1000;

                    double pour = (value*100.0) / max;
                    System.out.println(value/1000 + "M/" + max/1000 + "M -> " +myFormatter.format(pour));
                    Platform.runLater(() -> {
                        progressBar.setProgress(pour/100.0);
                        leftLabelBar.setText(value/1000 + "MB / " + max/1000 + "MB");
                        if(pour>100)
                            labelBar.setText("Téléchargement: 100%");
                        else
                            labelBar.setText("Téléchargement: "+myFormatter.format(pour)+"%");

                    });
                }

            }



        }
    }
}
