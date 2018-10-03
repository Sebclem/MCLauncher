package Broken;

import Broken.Utils.SaveUtils;
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

    @FXML
    private HBox authBox;

    private static String ramType;
    private static String ramMax;
    private static String ramMin;
    private static String authType;
    private Logger logger = LogManager.getLogger();

    @FXML
    void initialize() {
        SaveUtils.getINSTANCE().checkConfig();
        getConfig();
        authBox.setDisable(true);

        annuler.setOnMouseClicked(event -> Controller.dialogScene.getWindow().hide());

        confirmer.setOnMouseClicked(event -> {
            if(ramToogle.getSelectedToggle().equals(radioPredef))
                SaveUtils.getINSTANCE().save("ramType","0");
            else SaveUtils.getINSTANCE().save("ramType","1");

            SaveUtils.getINSTANCE().save("ramMax",ramMax);
//                SaveUtils.getINSTANCE().save("ramMin",ramMin);
//                SaveUtils.getINSTANCE().save("authType",authType);
//                SaveUtils.getINSTANCE().save("accessToken","");
//                SaveUtils.getINSTANCE().save("clientToken","");
//                SaveUtils.getINSTANCE().save("uuid","");
//                SaveUtils.getINSTANCE().save("name","");
//                SaveUtils.getINSTANCE().save("id","");

            Controller.dialogScene.getWindow().hide();
//            Main.controller.disconnect();
        });


        predefRam.getItems().addAll("1G","2G","3G","4G","5G","6G","7G","8G");



        maxRam.textProperty().addListener((observable, oldValue, newValue) -> ramMax = newValue);
        if (ramType.equals("0")){
            ramToogle.selectToggle(radioPredef);
            persoHBox.setDisable(true);
            predefRam.setDisable(false);
            predefRam.getSelectionModel().select(ramMax);
        }
        else
        {
            ramToogle.selectToggle(radioPerso);
            maxRam.setText(ramMax);
            persoHBox.setDisable(false);
            predefRam.setDisable(true);
        }

        ramToogle.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.equals(radioPredef))
            {
                persoHBox.setDisable(true);
                predefRam.setDisable(false);
                predefRam.getSelectionModel().select(ramMax);
            }
            else
            {
                maxRam.setText(ramMax);
                persoHBox.setDisable(false);
                predefRam.setDisable(true);

            }
        });
        predefRam.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            ramMax=newValue;
            ramMin="256m";

        });






        if(authType.equals("0"))
            authToggle.selectToggle(radioMojang);
        else
            authToggle.selectToggle(radioCrack);

        authToggle.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue.equals(radioMojang))
                authType="0";
            else
                authType="1";
        });





        buttonRegister.setOnMouseClicked(event -> {
            try {
                Desktop.getDesktop().browse(new URI("http://seb6596.freeboxos.fr/register"));
            } catch (IOException | URISyntaxException e1) {
                logger.catching(e1);
            }
        });

    }


    private static void getConfig()
    {
        ramType = SaveUtils.getINSTANCE().get("ramType");
        ramMax = SaveUtils.getINSTANCE().get("ramMax");
        ramMin = SaveUtils.getINSTANCE().get("ramMin");

        authType = SaveUtils.getINSTANCE().get("authType");
        //Init all ram param if it don't exits
        if(ramType ==null)
        {

            SaveUtils.getINSTANCE().save("ramType","0");
            ramType ="0";
            SaveUtils.getINSTANCE().save("ramMax","2G");
            ramMax = "2G";
            SaveUtils.getINSTANCE().save("ramMin","256m");
            ramMin = "256m";

        }

        if(authType==null)
        {
            SaveUtils.getINSTANCE().save("authType","0");
            authType="0";
        }
    }
}
