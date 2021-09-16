package McLauncher.Utils;

public class OsIdentifer {
    private static String OS = System.getProperty("os.name").toLowerCase();

    public static boolean isWindows() {

        return (OS.contains("win"));

    }

    public static boolean isMac() {

        return (OS.contains("mac"));

    }

    public static boolean isLinux() {

        return (OS.contains("nix") || OS.contains("nux") || OS.indexOf("aix") > 0 );

    }


    public static String getInstallPath(){
        if(isLinux() || isMac()){
            return System.getProperty("user.home") + "/.MCLauncher/";
        }
        else{
            return System.getenv("APPDATA") + "/.MCLauncher/";
        }
    }
}
