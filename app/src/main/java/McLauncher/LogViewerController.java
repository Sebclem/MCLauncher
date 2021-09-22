package McLauncher;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.message.Message;

public class LogViewerController {

    @FXML
    private TableView<MyLogEvent> table;

    @FXML
    private TableColumn<MyLogEvent, Long> time;

    @FXML
    private TableColumn<MyLogEvent, Level> level;

    @FXML
    private TableColumn<MyLogEvent, String> classCol;

    @FXML
    private TableColumn<MyLogEvent, Message> message;


    private ObservableList<MyLogEvent> data = FXCollections.observableArrayList();

    @FXML
    void initialize() {
        LogGuiAppender.setController(this);
        time.setCellValueFactory(new PropertyValueFactory<>("Time"));
        level.setCellValueFactory(new PropertyValueFactory<>("Level"));
        classCol.setCellValueFactory(new PropertyValueFactory<>("ClassName"));
        message.setCellValueFactory(new PropertyValueFactory<>("Message"));
        table.setItems(data);
        table.getItems().addListener((ListChangeListener<? super MyLogEvent>) c -> {
            table.scrollTo(c.getList().size()-1);
        });

    }

    public void addData(MyLogEvent event){
        Platform.runLater(()->{
            data.add(event);
        });
    }



}