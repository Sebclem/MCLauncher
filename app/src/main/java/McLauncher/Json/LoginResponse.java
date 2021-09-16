package McLauncher.Json;

public class LoginResponse {
    public String accessToken;
    public String clientToken;
    public SelectedProfile selectedProfile;

    public class SelectedProfile{
        public String id;
        public String name;
        public String userId;

    }
}
