package McLauncher.Json.Auth.Msa;

import java.util.List;

public class XblTokenRequest {
    public Properties Properties = new Properties();
    public final String RelyingParty = "http://auth.xboxlive.com";
    public String TokenType = "JWT";


    public static class Properties{
        public final String AuthMethod = "RPS";
        public final String SiteName = "user.auth.xboxlive.com";
        public String RpsTicket;
    }
}
