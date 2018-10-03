package Broken.Utils;

import Broken.Json.LoginPost;
import Broken.Json.LoginResponse;
import Broken.Json.ValidatePost;
import Broken.Utils.Exception.LoginException;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class MojanLogin {

    private Logger logger = LogManager.getLogger();
    private String mojangAuthServer = "https://authserver.mojang.com/";


    public Account login(String username, String password) throws LoginException, IOException {
        logger.debug("Send login info...");
        LoginPost loginPost = new LoginPost(username, password);
        GsonBuilder gsonBuilder = new GsonBuilder();
        String json = gsonBuilder.create().toJson(loginPost);
        LoginResponse response = postLogin(json);
        if(response != null){

            Account account = new Account(response.selectedProfile.id,response.selectedProfile.name, response.accessToken, response.clientToken, response.selectedProfile.userId, username);
            SaveUtils.getINSTANCE().save(account);
            return account;
        }
        logger.warn("Response object == null!");
        throw new LoginException("");
    }

    private LoginResponse postLogin(String json) throws LoginException, IOException {
        HttpResponse response = post(mojangAuthServer +"authenticate", json);


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

    public Account refreshAccount(Account account) throws IOException {
        if(validate(account)){
            return account;
        }

        return refresh(account);
    }



    private HttpResponse post(String url, String json) throws IOException {
        HttpClient httpClient = HttpClientBuilder.create().build(); //Use this instead

        HttpPost request = new HttpPost(url);
        StringEntity params =new StringEntity(json);
        request.addHeader("content-type", "application/json");
        request.setEntity(params);
        return httpClient.execute(request);







    }

    private boolean validate(Account account) throws IOException {

        GsonBuilder gsonBuilder = new GsonBuilder();
        String json = gsonBuilder.create().toJson(new ValidatePost(account.getAccessToken(), account.getClientToken()));
        HttpResponse response = post(mojangAuthServer + "validate", json);
        logger.debug("Token is valid ? " + (response.getStatusLine().getStatusCode() == 204));
        return response.getStatusLine().getStatusCode() == 204;
    }


    private Account refresh(Account account) throws IOException {
        GsonBuilder gsonBuilder = new GsonBuilder();
        String json = gsonBuilder.create().toJson(new ValidatePost(account.getAccessToken(), account.getClientToken()));
        HttpResponse response = post(mojangAuthServer + "refresh", json);
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
