package McLauncher.Json;

public class GameProfile {
    public String packUUID;
    public String gameVersion;
    public String gameType;
    public String forgeVersion;
    public transient String mainClass;

    public GameProfile(String packUUID, String gameVersion, String gameType, String forgeVersion, String mainClass) {
        this.packUUID = packUUID;
        this.gameVersion = gameVersion;
        this.gameType = gameType;
        this.forgeVersion = forgeVersion;
        this.mainClass = mainClass;
    }

    public GameProfile() {
    }

}
