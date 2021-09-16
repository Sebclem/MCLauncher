package McLauncher.Utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import McLauncher.Json.LoginPost;
import McLauncher.Json.LoginResponse;
import McLauncher.Json.ValidatePost;
import McLauncher.Utils.Exception.LoginException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class MojanLogin {

    private Logger logger = LogManager.getLogger();
    private String mojangAuthServer = "https://authserver.mojang.com/";
    private String customAuthServer = "https://auth.minecraft.seb6596.ovh/";


    public Account login(String username, String password, boolean official) throws LoginException, IOException {
        logger.debug("Send login info...");
        LoginPost loginPost = new LoginPost(username, password);
        GsonBuilder gsonBuilder = new GsonBuilder();
        String json = gsonBuilder.create().toJson(loginPost);
        LoginResponse response = postLogin(json, official);
        if(response != null){

            Account account = new Account(response.selectedProfile.id,response.selectedProfile.name, response.accessToken, response.clientToken, response.selectedProfile.userId, username);
            SaveUtils.getINSTANCE().save(account);
            return account;
        }
        logger.warn("Response object == null!");
        throw new LoginException("");
    }

    private LoginResponse postLogin(String json, boolean official) throws LoginException, IOException {
        String url;
        if(official)
            url = mojangAuthServer;
        else
            url = customAuthServer;


        HttpResponse response = post(url +"authenticate", json);


        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), Charset.defaultCharset()));
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }


        if(response.getStatusLine().getStatusCode() == 200){
            return new GsonBuilder().create().fromJson(stringBuilder.toString(), LoginResponse.class);

        }
        else{
            logger.debug(stringBuilder.toString());
            throw new LoginException(stringBuilder.toString());
        }


    }

    public Account refreshAccount(Account account, boolean official) throws IOException {
        String url;
        if(official)
            url = mojangAuthServer;
        else
            url = customAuthServer;

        if(validate(account, url)){
            return account;
        }

        return refresh(account, url);
    }



    private HttpResponse post(String url, String json) throws IOException {
        logger.info("ok");
        HttpClient httpClient = HttpClientBuilder.create().build(); //Use this instead

        HttpPost request = new HttpPost(url);
        StringEntity params =new StringEntity(json);
        request.addHeader("content-type", "application/json");
        request.setEntity(params);
        return httpClient.execute(request);







    }

    private boolean validate(Account account, String url) throws IOException {
        Gson gson = new Gson();
        ValidatePost valPost = new ValidatePost(account.getAccessToken(), account.getClientToken());
        String json = gson.toJson(valPost);
        logger.info("ok");
        HttpResponse response = post(url + "validate", json);
        logger.debug("Token is valid ? " + (response.getStatusLine().getStatusCode() == 204));
        return response.getStatusLine().getStatusCode() == 204;
    }


    private Account refresh(Account account, String url) throws IOException {
        GsonBuilder gsonBuilder = new GsonBuilder();
        String json = gsonBuilder.create().toJson(new ValidatePost(account.getAccessToken(), account.getClientToken()));
        HttpResponse response = post(url + "refresh", json);
        if(response.getStatusLine().getStatusCode() == 200){
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), Charset.defaultCharset()));
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            ValidatePost validatePost =new GsonBuilder().create().fromJson(stringBuilder.toString(), ValidatePost.class);
            account.setAccessToken(validatePost.accessToken);
            account.setClientToken(validatePost.clientToken);
            SaveUtils.getINSTANCE().save(account);
            return account;

        }
        return null;


    }
}
