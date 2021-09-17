package McLauncher.Json;

public class GameProfile {
    public String packUUID;
    public String gameVersion;
    public String gameType;

    public GameProfile(String packUUID, String gameVersion, String gameType) {
        this.packUUID = packUUID;
        this.gameVersion = gameVersion;
        this.gameType = gameType;
    }

    public GameProfile() {
    }

}
