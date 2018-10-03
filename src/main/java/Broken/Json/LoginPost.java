package Broken.Json;

public class LoginPost {
    public AgentMinecraft agent = new AgentMinecraft();
    public String username;
    public String password;

    public LoginPost(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public class AgentMinecraft{
        public String name = "Minecraft";
        public int version = 1;
    }
}
