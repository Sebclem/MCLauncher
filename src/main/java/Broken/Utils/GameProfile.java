package Broken.Utils;

import Broken.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GameProfile {

    private Account account;
    private String ram;
    private String assetDir = "assets/";
    private String assetIndex;
    private String gameDir;
    private String version;
    private String sysLibDir = "sysLib/";
    private String classPath;
    private MainClass mainClass;
    private String logProfile;


    private BufferedReader error;
    private BufferedReader input;
    private int exitVal;

    public enum MainClass{
        VANILLA("net.minecraft.client.main.Main"),
        FORGE("net.minecraft.launchwrapper.Launch");

        private String val;

        MainClass(String val) {
            this.val = val;
        }

        @Override
        public String toString() {
            return val;
        }
    }

    private Logger logger = LogManager.getLogger();

    public GameProfile(Account account, String ram, String assetIndex, String gameDir, String version, String classPath, MainClass mainClass, String logProfile) {
        this.account = account;
        this.ram = ram;
        this.assetIndex = assetIndex;
        this.gameDir = gameDir;
        this.version = version;
        this.classPath = classPath;
        this.mainClass = mainClass;
        this.logProfile = logProfile;
    }

    private List<String> buildCommand(){
        ArrayList<String> command = new ArrayList<>();


        command.add("java");
        command.add("-Djava.library.path=" +  gameDir +sysLibDir);
        command.add("-Dminecraft.client.jar=" + gameDir +"/client.jar");
        command.add("-Duser.dir="+ Main.gamePath);

        command.add("-Xmx" + ram);

        command.add("-XX:+UnlockExperimentalVMOptions");
        command.add("-XX:+UseG1GC");
        command.add("-XX:G1NewSizePercent=20");
        command.add("-XX:G1ReservePercent=20");
        command.add("-XX:MaxGCPauseMillis=50");
        command.add("-XX:G1HeapRegionSize=32M");






        command.add("-Dlog4j.configurationFile=" + logProfile);

        command.add("-cp");
        command.add(classPath);

        command.add(mainClass.toString());

        command.add("--username");
        command.add(account.getDisplayName());

        command.add("--version");
        command.add(version);

        command.add("--gameDir");
        command.add(gameDir);

        command.add("--assetDir");
        command.add(gameDir + assetDir);

        command.add("--assetIndex");
        command.add(assetIndex);

        command.add("--uuid");
        command.add(account.getUUID());

        command.add("--accessToken");
        command.add(account.getAccessToken());

        if(mainClass == MainClass.FORGE){
            command.add("--tweakClass");
            command.add("net.minecraftforge.fml.common.launcher.FMLTweaker");
            command.add("--versionType");
            command.add("Forge");
        }


        for(String elem : command){
            logger.debug(elem);

        }

        return command;




    }

    public void launch() throws IOException, InterruptedException {
        List<String> commandList = buildCommand();
        Runtime re = Runtime.getRuntime();
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(new File(Main.gamePath));
        builder.command(commandList);

        //final Process command = re.exec(cmdString, args.toArray(new String[0]));
        Process command = builder.start();
        this.error = new BufferedReader(new InputStreamReader(command.getErrorStream()));
        this.input = new BufferedReader(new InputStreamReader(command.getInputStream()));
        // Wait for the application to Finish
        String line;

        while ((line = this.input.readLine()) != null) {
            System.out.println(line);
        }
        this.input.close();

        this.exitVal = command.exitValue();
        if (this.exitVal != 0) {
            throw new IOException("Failed to execute Minecraft:\n " +getExecutionLog());
        }

    }

    public String getExecutionLog() {
        String error = "";
        String line;
        try {
            while((line = this.error.readLine()) != null) {
                error = error + "\n" + line;
            }
        } catch (final IOException e) {
        }
        String output = "";
        try {
            while((line = this.input.readLine()) != null) {
                output = output + "\n" + line;
            }
        } catch (final IOException e) {
        }
        try {
            this.error.close();
            this.input.close();
        } catch (final IOException e) {
        }
        return "exitVal: " + this.exitVal + ", error: " + error + ", output: " + output;
    }
}


