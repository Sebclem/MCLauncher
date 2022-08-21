package McLauncher.Json;

public class GameProfile {
    public String packUUID;
    public String gameVersion;
    public String gameType;
    public String modLoaderVersion;
    public transient String mainClass;

    public GameProfile(String packUUID, String gameVersion, String gameType, String modLoaderVersion, String mainClass) {
        this.packUUID = packUUID;
        this.gameVersion = gameVersion;
        this.gameType = gameType;
        this.modLoaderVersion = modLoaderVersion;
        this.mainClass = mainClass;
    }

    public GameProfile() {
    }

}
