package fr.BrokenFire;

import fr.theshark34.openlauncherlib.LaunchException;
import fr.theshark34.supdate.BarAPI;
import fr.theshark34.supdate.SUpdate;
import fr.theshark34.supdate.exception.BadServerResponseException;
import fr.theshark34.supdate.exception.BadServerVersionException;
import fr.theshark34.supdate.exception.ServerDisabledException;
import fr.theshark34.supdate.exception.ServerMissingSomethingException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import org.pdfsam.ui.RingProgressIndicator;

import java.io.IOException;
import java.text.DecimalFormat;


public class Controller {
    @FXML
    private Label label;


    @FXML
    private StackPane stack;

    RingProgressIndicator progress;
    DlListenner dlListenner;
    Downloader downloader;
    @FXML
    void initialize() {

        progress = new RingProgressIndicator();
        progress.setRingWidth(180);
        progress.setBackground(Background.EMPTY);
        progress.setProgress(-1);
        stack.getChildren().add(progress);

        dlListenner = new DlListenner();
        dlListenner.start();
        downloader = new Downloader();
        downloader.start();

    }


    class DlListenner extends Thread
    {
        long value = BarAPI.getNumberOfTotalDownloadedBytes()/1000;
        long max = BarAPI.getNumberOfTotalBytesToDownload()/1000;


        @Override
        public void run() {

            while (max == 0) max = BarAPI.getNumberOfTotalBytesToDownload() / 1000;


            Platform.runLater(()->{
                progress.setProgress(0);
                label.setText("Downloading Update...");
            });

            DecimalFormat myFormatter = new DecimalFormat("##0.00");
            while (!this.isInterrupted()) {
                if (value != BarAPI.getNumberOfTotalDownloadedBytes() / 1000) {
                    value = BarAPI.getNumberOfTotalDownloadedBytes() / 1000;
                    double pour = (value*100.0) / max;
                    System.out.println(value/1000 + "M/" + max/1000 + "M -> " +myFormatter.format(pour));
                    Platform.runLater(() ->progress.setProgress((int)pour));
                }
            }
        }
    }

    class Downloader extends Thread
    {
        @Override
        public void run() {
            SUpdate updater = Main.getUpdater();
            try {
                updater.start();
                dlListenner.interrupt();
                System.out.println("test");
                Platform.runLater(()->{
                    label.setText("Launching...");
                    progress.setProgress(-1);
                });
                Main.launchB();
            } catch (BadServerVersionException |ServerMissingSomethingException e) {
                e.printStackTrace();
                Platform.runLater(()-> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText("Erreur Serveur!");
                    alert.setContentText("Ereur Serveur: " + e.getMessage());
                    alert.setTitle("Erreur");
                    progress.setProgress(-1);
                    alert.showAndWait();
                    dlListenner.interrupt();
                    Platform.exit();
                    System.exit(1);
                });
                this.interrupt();
            } catch (ServerDisabledException e) {
                e.printStackTrace();
                Platform.runLater(()->{
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText("Erreur Serveur!");
                    alert.setContentText("Le serveur de mise à jour est désactivé.");
                    alert.setTitle("Erreur");
                    progress.setProgress(-1);
                    alert.showAndWait();
                    dlListenner.interrupt();
                    Platform.exit();
                    System.exit(1);
                });
            } catch (BadServerResponseException e) {
                e.printStackTrace();
                Platform.runLater(()->{
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText("Erreur Serveur!");
                    alert.setContentText("Mauvaise reponsse du serveur: \n"+e.getMessage());
                    alert.setTitle("Erreur");
                    progress.setProgress(-1);
                    alert.showAndWait();
                    dlListenner.interrupt();
                    Platform.exit();
                    System.exit(1);
                });
            } catch (IOException e) {
                e.printStackTrace();
                Platform.runLater(()->{
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText("Erreur !");
                    alert.setContentText("Erreur : \n"+e.getMessage());
                    alert.setTitle("Erreur");
                    progress.setProgress(-1);
                    alert.showAndWait();
                    dlListenner.interrupt();
                    Platform.exit();
                    System.exit(1);
                });

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (LaunchException e) {
                Platform.runLater(()->{
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText("Erreur de lancement!");
                    alert.setContentText("Impossible de démarrer le launcher: \n"+e.getMessage());
                    alert.setTitle("Erreur");
                    progress.setProgress(-1);
                    alert.showAndWait();
                    dlListenner.interrupt();
                    Platform.exit();
                    System.exit(1);
                });
            }
        }
    }
}
