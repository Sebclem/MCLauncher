package McLauncher.Json.Auth.Msa;

import java.util.List;

public class XblTokenResponse {
    public String IssueInstant;
    public String NotAfter;
    public String Token;
    public DisplayClaims DisplayClaims;

    public static class DisplayClaims{
        public List<Xui> xui;

        public static class Xui{
            public String uhs;
        }
    }
}
