package McLauncher;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.message.Message;

public class LogViewerController {

    @FXML
    private TableView<MyLogEvent> table;

    @FXML
    private TableColumn<MyLogEvent, LocalDateTime> time;

    @FXML
    private TableColumn<MyLogEvent, String> level;

    @FXML
    private TableColumn<MyLogEvent, String> classCol;

    @FXML
    private TableColumn<MyLogEvent, String> message;

    @FXML
    private ChoiceBox<String> levelSelector;


    private ObservableList<MyLogEvent> data = FXCollections.observableArrayList();
    private FilteredList<MyLogEvent> filteredData = new FilteredList<>(data);
    private Map<String, Integer> logLevelValue = new HashMap<>();

    private final PseudoClass debug = PseudoClass.getPseudoClass("debug");
    private final PseudoClass info = PseudoClass.getPseudoClass("info");
    private final PseudoClass warn = PseudoClass.getPseudoClass("warn");
    private final PseudoClass error = PseudoClass.getPseudoClass("error");




    @FXML
    void initialize() {
        logLevelValue.put("DEBUG", 0);
        logLevelValue.put("INFO", 1);
        logLevelValue.put("WARN", 2);
        logLevelValue.put("ERROR", 3);
        logLevelValue.put("FATAL", 4);
        LogGuiAppender.setController(this);
        levelSelector.getItems().addAll("DEBUG", "INFO", "WARN", "ERROR");
        levelSelector.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(myLogEvent -> {
                String selected = levelSelector.getSelectionModel().getSelectedItem();
                return logLevelValue.get(myLogEvent.getLevel()) >= logLevelValue.get(selected);
            });
        });
        levelSelector.getSelectionModel().select(0);

        time.setCellValueFactory(new PropertyValueFactory<>("Time"));
        level.setCellValueFactory(new PropertyValueFactory<>("Level"));
        classCol.setCellValueFactory(new PropertyValueFactory<>("ClassName"));
        message.setCellValueFactory(new PropertyValueFactory<>("Message"));
        table.setRowFactory(param -> new TableRow<>() {
            @Override
            protected void updateItem(MyLogEvent item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    pseudoClassStateChanged(debug, false);
                    pseudoClassStateChanged(info, false);
                    pseudoClassStateChanged(warn, false);
                    pseudoClassStateChanged(error, false);
                    switch (item.getLevel()) {
                        case "DEBUG":
                            pseudoClassStateChanged(debug, true);
                            break;
                        case "INFO":
                            pseudoClassStateChanged(info, true);
                            break;
                        case "WARN":
                            pseudoClassStateChanged(warn, true);
                            break;
                        default:
                            pseudoClassStateChanged(error, true);
                    }
                }


            }
        });
        table.setItems(filteredData);
        table.getItems().addListener((ListChangeListener<? super MyLogEvent>) c -> {
            table.scrollTo(c.getList().size() - 1);
        });

    }

    public void addData(MyLogEvent event) {
        Platform.runLater(() -> {
            data.add(event);
        });
    }


}