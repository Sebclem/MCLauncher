package Broken.Utils;

import Broken.Utils.Exception.LoadingSaveException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Properties;

public class SaveUtils {

    static private SaveUtils INSTANCE ;

    private String path;

    private Properties prop = new Properties();
    private OutputStream output = null;
    private InputStream input = null;

    private Logger logger = LogManager.getLogger();


    public static SaveUtils getINSTANCE(String savePath){
        File file = new File(savePath);
        if(!file.exists()){
            new File(savePath.substring(0,savePath.lastIndexOf("/"))).mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(INSTANCE == null){
            INSTANCE = new SaveUtils(savePath);
        }
        return INSTANCE;
    }

    public static SaveUtils getINSTANCE(){
        return INSTANCE;
    }

    private SaveUtils(String savePath){
        this.path = savePath;

        try {
            input = new FileInputStream(path);
            prop.load(input);
        } catch (IOException e) {
            logger.catching(e);
        }finally {
            if(input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    logger.catching(e);
                }
            }
        }
    }

    public void save(Account account){
        try {
            output = new FileOutputStream(path);
            prop.setProperty("uuid", account.getUUID());
            prop.setProperty("displayName", account.getDisplayName());
            prop.setProperty("accessToken", account.getAccessToken());
            prop.setProperty("userId", account.getUserId());
            prop.setProperty("usernameor", account.getUsername());
            prop.setProperty("clientToken", account.getClientToken());

            prop.store(output, null);


        } catch (IOException e) {
            logger.catching(e);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    logger.catching(e);
                }
            }

        }

    }

    public  void save(String id, String value){
        try {
            output = new FileOutputStream(path);
            prop.setProperty(id, value);
            prop.store(output, null);


        } catch (IOException e) {
            logger.catching(e);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    logger.catching(e);
                }
            }

        }
    }


    public String get(String id){
        return prop.getProperty(id);
    }

    public Account getAccount() throws LoadingSaveException {

        if(prop.getProperty("clientToken") == null || prop.getProperty("clientToken").equals("")){
            throw new LoadingSaveException("No logged");
        }

        return new Account(
                prop.getProperty("uuid"),
                prop.getProperty("displayName"),
                prop.getProperty("accessToken"),
                prop.getProperty("clientToken"),
                prop.getProperty("userId"),
                prop.getProperty("usernameor")
        );
    }



    public void checkConfig(){
        String ramType = get("ramType");
        String ramMax = get("ramMax");

        String authType = get("authType");
        //Init all ram param if it don't exits
        if(ramType ==null || ramMax == null)
        {
            save("ramType","0");
            save("ramMax","2G");
        }

        if(authType == null)
        {
            save("authType","0");
        }
    }
}
