package Broken;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by seb65 on 13/02/2017.
 */
public class OptionController {
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private RadioButton radioPredef;

    @FXML
    private ToggleGroup authToggle;

    @FXML
    private ToggleGroup ramToogle;

    @FXML
    private ChoiceBox<String> predefRam;

    @FXML
    private TextField maxRam;

    @FXML
    private RadioButton radioMojang;

    @FXML
    private TextField minRam;

    @FXML
    private RadioButton radioPerso;

    @FXML
    private RadioButton radioCrack;

    @FXML
    private Button buttonRegister;

    @FXML
    private HBox persoHBox;

    @FXML
    private Button confirmer;

    @FXML
    private Button annuler;

    static String ramType;
    static String ramMax;
    static String ramMin;
    static String authType;
    boolean edited=false;
    Logger logger = LogManager.getLogger();

    @FXML
    void initialize() {
        checkConfig();
        annuler.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Controller.dialogScene.getWindow().hide();
            }
        });

        confirmer.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(ramToogle.getSelectedToggle().equals(radioPredef))
                    Main.saver.set("ramType","0");
                else Main.saver.set("ramType","1");

                Main.saver.set("ramMax",ramMax);
                Main.saver.set("ramMin",ramMin);
                Main.saver.set("authType",authType);
                Main.saver.set("accessToken","");
                Main.saver.set("clientToken","");
                Main.saver.set("uuid","");
                Main.saver.set("name","");
                Main.saver.set("id","");
                if(edited)
                {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText("Modification des paramètres.");
                    alert.setContentText("Veuillez redemarrer le launcher pour que les modifications des paramètres de Ram prennent effet.");
                    alert.setTitle("Info");
                    alert.showAndWait();
                }
                Controller.dialogScene.getWindow().hide();
                Main.controller.disconnect();
            }
        });


        predefRam.getItems().addAll("1G","2G","3G","4G","5G","6G","7G","8G");



        maxRam.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                ramMax = newValue;
                edited = true;
            }
        });
        minRam.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                ramMin= newValue;
                edited = true;
            }
        });

        if (ramType.equals("0")){
            ramToogle.selectToggle(radioPredef);
            persoHBox.setDisable(true);
            predefRam.setDisable(false);
            predefRam.getSelectionModel().select(ramMax);
        }
        else
        {
            ramToogle.selectToggle(radioPerso);
            minRam.setText(ramMin);
            maxRam.setText(ramMax);
            persoHBox.setDisable(false);
            predefRam.setDisable(true);
        }

        ramToogle.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if(newValue.equals(radioPredef))
                {
                    persoHBox.setDisable(true);
                    predefRam.setDisable(false);
                    predefRam.getSelectionModel().select(ramMax);
                }
                else
                {
                    minRam.setText(ramMin);
                    maxRam.setText(ramMax);
                    persoHBox.setDisable(false);
                    predefRam.setDisable(true);

                }
                edited = true;
            }
        });
        predefRam.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                ramMax=newValue;
                ramMin="256m";
                edited = true;

            }
        });






        if(authType.equals("0"))
            authToggle.selectToggle(radioMojang);
        else
            authToggle.selectToggle(radioCrack);

        authToggle.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if(newValue.equals(radioMojang))
                    authType="0";
                else
                    authType="1";
            }
        });





        buttonRegister.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    Desktop.getDesktop().browse(new URI("http://seb6596.freeboxos.fr/register"));
                } catch (IOException | URISyntaxException e1) {
                    logger.catching(e1);
                }
            }
        });

    }


    public static void checkConfig()
    {
        ramType = Main.saver.get("ramType");
        ramMax = Main.saver.get("ramMax");
        ramMin = Main.saver.get("ramMin");

        authType = Main.saver.get("authType");
        //Init all ram param if it don't exits
        if(ramType ==null)
        {

            Main.saver.set("ramType","0");
            ramType ="0";
            Main.saver.set("ramMax","2G");
            ramMax = "2G";
            Main.saver.set("ramMin","256m");
            ramMin = "256m";

        }

        if(authType==null)
        {
            Main.saver.set("authType","0");
            authType="0";
        }
    }
}
