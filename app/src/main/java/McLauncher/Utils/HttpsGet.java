package McLauncher.Utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class HttpsGet {
    private static Logger logger = LogManager.getLogger();
    public static String get(String url) throws IOException {
        URL myUrl = new URL(url);
        logger.debug("Downloading => " + url + "...");
        HttpsURLConnection conn = (HttpsURLConnection)myUrl.openConnection();
        InputStream is = conn.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        String inputLine;
        StringBuilder stringBuilder = new StringBuilder();

        while ((inputLine = br.readLine()) != null) {
            stringBuilder.append(inputLine);
        }

        br.close();
        return stringBuilder.toString();
    }
}
