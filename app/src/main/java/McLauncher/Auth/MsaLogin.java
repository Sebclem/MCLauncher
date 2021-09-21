package McLauncher.Auth;

import McLauncher.App;
import McLauncher.Json.Auth.Msa.*;
import McLauncher.Json.ValidatePost;
import McLauncher.Utils.Exception.LoginException;
import McLauncher.Utils.Exception.TokenRefreshException;
import McLauncher.Utils.SaveUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

public class MsaLogin extends AbstractLogin {
    private static final String authTokenUrl = "https://login.live.com/oauth20_token.srf";
    private static final String xblAuthUrl = "https://user.auth.xboxlive.com/user/authenticate";
    private static final String xstsAuthUrl = "https://xsts.auth.xboxlive.com/xsts/authorize";
    private static final String mcLoginUrl = "https://api.minecraftservices.com/authentication/login_with_xbox";
    private static final String mcStoreUrl = "https://api.minecraftservices.com/entitlements/mcstore";
    private static final String mcProfileUrl = "https://api.minecraftservices.com/minecraft/profile";
    public final String loginUrl = "https://login.live.com/oauth20_authorize.srf" +
            "?client_id=00000000402b5328" +
            "&response_type=code" +
            "&scope=XboxLive.signin%20offline_access" +
            "&tenant=consumers" +
            "&redirect_uri=https%3A%2F%2Flogin.live.com%2Foauth20_desktop.srf";
    public final String redirectUrlSuffix = "https://login.live.com/oauth20_desktop.srf?code=";
    private String authToken;
    private Account account;

    public MsaLogin() {
        super();
    }

    @Override
    public Account refreshToken(Account account) throws TokenRefreshException {
        if(account.getTokenExpireDate().isBefore(LocalDateTime.now())){
            logger.warn("MSA token expired. Refreshing it...");
            try {
                MsaAccessTokenResponse refreshTokenResponse = refreshAccessToken(account.getRefreshToken());
                LocalDateTime expireDate = LocalDateTime.now().plusSeconds(refreshTokenResponse.expires_in);
                account.setAccessToken(refreshTokenResponse.access_token);
                account.setRefreshToken(refreshTokenResponse.refresh_token);
                account.setTokenExpireDate(expireDate);
            } catch (IOException | InterruptedException e) {
                logger.catching(e);
                throw new TokenRefreshException();
            }
        }
        try {
            XblTokenResponse xblTokenResponse;
            try{
                xblTokenResponse = getXblToken(account.getAccessToken());
            } catch (LoginException e) {
//              Token is maybe revoked ? Try to refresh it !
                logger.warn("Fail to get Xbl token. MSA token is maybe revoked/expired. Refreshing it...");
                MsaAccessTokenResponse refreshTokenResponse = refreshAccessToken(account.getRefreshToken());
                LocalDateTime expireDate = LocalDateTime.now().plusSeconds(refreshTokenResponse.expires_in);
                account.setAccessToken(refreshTokenResponse.access_token);
                account.setRefreshToken(refreshTokenResponse.refresh_token);
                account.setTokenExpireDate(expireDate);
                xblTokenResponse = getXblToken(account.getAccessToken());
            }
            XstsTokenResponse xstsTokenResponse = getXstsToken(xblTokenResponse.Token);
            MinecraftTokenResponse minecraftTokenResponse = getMinecraftToken(xblTokenResponse.DisplayClaims.xui.get(0).uhs, xstsTokenResponse.Token);
            MinecraftProfileResponse minecraftProfileResponse = getMinecraftProfile(minecraftTokenResponse.access_token);
            account = new Account(
                    minecraftProfileResponse.id,
                    minecraftProfileResponse.name,
                    minecraftTokenResponse.access_token,
                    account.getAccessToken(),
                    account.getTokenExpireDate(),
                    account.getRefreshToken(),
                    null
            );
            return account;
        } catch (LoginException | IOException | InterruptedException e) {
            logger.catching(e);
            throw new TokenRefreshException();
        }


    }

    @Override
    protected void loginThreadMethod() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/MsaWebView.fxml"));
                Parent popup = loader.load();
                MsaController controller = (MsaController) loader.getController();
                controller.setMsaLogin(this);
                final Stage dialog = new Stage();
                dialog.initModality(Modality.APPLICATION_MODAL);
                dialog.initOwner(App.getPrimaryStage());
                dialog.setTitle("Microsoft Login");
                dialog.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
                Scene dialogScene = new Scene(popup, 460, 600);

                dialog.setScene(dialogScene);
                dialog.setResizable(false);
                dialog.showAndWait();
                logger.debug("Auth token: " + authToken);
                if (authToken == null) {
                    loginCancel();
                    return;
                }
                new Thread(() -> {
                    try {
                        MsaAccessTokenResponse accessTokenResponse = getAccessToken(authToken);
                        LocalDateTime expireDate = LocalDateTime.now().plusSeconds(accessTokenResponse.expires_in);
                        XblTokenResponse xblTokenResponse = getXblToken(accessTokenResponse.access_token);
                        XstsTokenResponse xstsTokenResponse = getXstsToken(xblTokenResponse.Token);
                        MinecraftTokenResponse minecraftTokenResponse = getMinecraftToken(xblTokenResponse.DisplayClaims.xui.get(0).uhs, xstsTokenResponse.Token);
                        MinecraftProfileResponse minecraftProfileResponse = getMinecraftProfile(minecraftTokenResponse.access_token);
                        super.account = new Account(
                                minecraftProfileResponse.id,
                                minecraftProfileResponse.name,
                                minecraftTokenResponse.access_token,
                                accessTokenResponse.access_token,
                                expireDate,
                                accessTokenResponse.refresh_token,
                                null
                        );
                        SaveUtils.getINSTANCE().save(super.account);
                        loginSuccess();

                    } catch (URISyntaxException | IOException | InterruptedException | LoginException e) {
                        connectionError(e);
                    }
                }).start();


            } catch (IOException e) {
                logger.catching(e);
            }
        });
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }


    private MsaAccessTokenResponse getAccessToken(String authToken) throws LoginException, IOException, InterruptedException, URISyntaxException {
        logger.debug("Getting Msa AccessToken...");
        Map<String, String> data = Map.of(
                "client_id", "00000000402b5328",
                "code", authToken,
                "grant_type", "authorization_code",
                "redirect_uri", "https://login.live.com/oauth20_desktop.srf",
                "scope", "service::user.auth.xboxlive.com::MBI_SSL"
        );

        HttpResponse<String> response = makeXFormPost(authTokenUrl, data);
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            logger.debug("...Ok");
            Gson gson = new Gson();
            return gson.fromJson(response.body(), MsaAccessTokenResponse.class);
        }
        logger.error("Response code: " + response.statusCode());
        logger.error(response.body());
        throw new LoginException("Fail to get AccessToken !");
    }

    private MsaAccessTokenResponse refreshAccessToken(String refreshToken) throws IOException, InterruptedException, TokenRefreshException {
        logger.debug("Refreshing Msa AccessToken...");
        Map<String, String> data = Map.of(
                "client_id", "00000000402b5328",
                "refresh_token", refreshToken,
                "grant_type", "refresh_token",
                "redirect_uri", "https://login.live.com/oauth20_desktop.srf",
                "scope", "service::user.auth.xboxlive.com::MBI_SSL"
        );

        HttpResponse<String> response = makeXFormPost(authTokenUrl, data);
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            logger.debug("...Ok");
            Gson gson = new Gson();
            return gson.fromJson(response.body(), MsaAccessTokenResponse.class);
        }
        logger.error("Response code: " + response.statusCode());
        logger.error(response.body());
        throw new TokenRefreshException();
    }




    private XblTokenResponse getXblToken(String accessToken) throws IOException, InterruptedException, LoginException {
        logger.debug("Getting XBL Token...");
        XblTokenRequest xblTokenRequest = new XblTokenRequest();
        xblTokenRequest.Properties.RpsTicket = accessToken;

        HttpResponse<String> response = makePostJson(xblAuthUrl, xblTokenRequest);
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            logger.debug("...Ok");
            return new Gson().fromJson(response.body(), XblTokenResponse.class);
        }
        logger.error("Response code: " + response.statusCode());
        logger.error(response.body());
        throw new LoginException("Fail to get Xbox live Token !");
    }

    private XstsTokenResponse getXstsToken(String xblToken) throws IOException, InterruptedException, LoginException {
        logger.debug("Getting XSTS Token...");
        XstsTokenRequest xstsTokenRequest = new XstsTokenRequest();
        xstsTokenRequest.Properties.UserTokens.add(xblToken);

        HttpResponse<String> response = makePostJson(xstsAuthUrl, xstsTokenRequest);
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            logger.debug("...Ok");
            return new Gson().fromJson(response.body(), XstsTokenResponse.class);
        }
        logger.error("Response code: " + response.statusCode());
        logger.error(response.body());
        throw new LoginException("Fail to get XSTS Token !");


    }

    private MinecraftTokenResponse getMinecraftToken(String userhash, String xstsToken) throws IOException, InterruptedException, LoginException {
        logger.debug("Getting Minecraft AccessToken...");
        MinecraftTokenRequest minecraftTokenRequest = new MinecraftTokenRequest();
        minecraftTokenRequest.identityToken = "XBL3.0 x=" + userhash + ";" + xstsToken;
        HttpResponse<String> response = makePostJson(mcLoginUrl, minecraftTokenRequest);
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            logger.debug("...Ok");
            return new Gson().fromJson(response.body(), MinecraftTokenResponse.class);
        }
        logger.error("Response code: " + response.statusCode());
        logger.error(response.body());
        throw new LoginException("Fail to get Minecraft Token !");

    }

    private MinecraftProfileResponse getMinecraftProfile(String minecraftToken) throws IOException, InterruptedException, LoginException {
        logger.debug("Getting Minecraft profile...");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(mcProfileUrl))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + minecraftToken)
                .GET()
                .build();
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            logger.debug("...Ok");
            return new Gson().fromJson(response.body(), MinecraftProfileResponse.class);
        }
        logger.error("Response code: " + response.statusCode());
        logger.error(response.body());
        throw new LoginException("Fail to get Minecraft Profile !");
    }

    private HttpResponse<String> makePostJson(String url, Object data) throws IOException, InterruptedException {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        String dataStr = gson.toJson(data);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(dataStr))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> makeXFormPost(String url, Map<String,String> data) throws IOException, InterruptedException {
        String form = data.keySet().stream()
                .map(key -> key + "=" + URLEncoder.encode(data.get(key), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form)).build();
        HttpClient client = HttpClient.newHttpClient();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
