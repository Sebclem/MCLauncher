package McLauncher.Auth;

import java.net.URL;
import java.util.ResourceBundle;

import McLauncher.Controller;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
public class MsaController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private WebView webView;
    private MsaLogin msaLogin;

    @FXML
    void initialize() {


    }

    void setMsaLogin(MsaLogin msaLogin){
        this.msaLogin = msaLogin;
        webView.getEngine().load(msaLogin.loginUrl);
        webView.getEngine().setJavaScriptEnabled(true);

        webView.getEngine().getHistory().getEntries().addListener((ListChangeListener<WebHistory.Entry>) c -> {
            if (c.next() && c.wasAdded()) {
                for (WebHistory.Entry entry : c.getAddedSubList()) {
                    if (entry.getUrl().startsWith(msaLogin.redirectUrlSuffix)) {
                        String authCode = entry.getUrl().substring(entry.getUrl().indexOf("=") + 1, entry.getUrl().indexOf("&"));
                        // once we got the auth code, we can turn it into a oauth token
                        msaLogin.setAuthToken(authCode);
                        Stage stage = (Stage) webView.getScene().getWindow();
                        stage.close();
                    }
                }
            }
        });
    }
}
