package Broken;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.awt.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
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

    String ramEtat;
    String ramMax;
    String ramMin;
    String authEtat;

    @FXML
    void initialize() {
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
                    Main.saver.set("ramEtat","0");
                else Main.saver.set("ramEtat","1");

                Main.saver.set("ramMax",ramMax);
                Main.saver.set("ramMin",ramMin);
                Controller.dialogScene.getWindow().hide();
            }
        });

        predefRam.getItems().addAll("1G","2G","3G","4G","5G","6G","7G","8G");
        if((ramEtat = Main.saver.get("ramEtat"))==null)
        {
            Main.saver.set("ramEtat","0");
            ramEtat="0";
            Main.saver.set("ramMax","2G");
            ramMax = "2G";
            Main.saver.set("ramMin","256m");
            ramMin = "256m";

        }
        ramMax = Main.saver.get("ramMax");
        ramMin = Main.saver.get("ramMin");

        if (ramEtat.equals("0")){
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

        maxRam.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                ramMax= newValue;
            }
        });
        minRam.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                ramMin= newValue;
            }
        });


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
            }
        });
        predefRam.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                ramMax=newValue;
                ramMin="256m";

            }
        });

        buttonRegister.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try {
                    Desktop.getDesktop().browse(new URI("http://seb6596.freeboxos.fr/register"));
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
