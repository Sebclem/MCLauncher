package McLauncher.Json.Auth.Msa;

import java.util.ArrayList;
import java.util.List;

public class XstsTokenRequest {
    public Properties Properties = new Properties();
    public final String RelyingParty = "rp://api.minecraftservices.com/";
    public final String TokenType = "JWT";

    public static class Properties{
        public final String SandboxId = "RETAIL";
        public List<String> UserTokens = new ArrayList<>();
    }
}
