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
    private ChoiceBox<?> predefRam;

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
    void initialize() {
        if(ramToogle.getSelectedToggle().equals(radioPredef))
        {
            persoHBox.setDisable(true);
            predefRam.setDisable(false);
        }
        else
        {
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
                }
                else
                {
                    persoHBox.setDisable(false);
                    predefRam.setDisable(true);

                }
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
