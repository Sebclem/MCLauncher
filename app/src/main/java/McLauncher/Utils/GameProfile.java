package McLauncher.Utils;

import McLauncher.Auth.Account;
import javafx.application.Platform;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import McLauncher.App;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GameProfile {

    private final String logProfile;
    private Account account;
    private String ram;
    private String assetDir = "assets/";
    private String assetIndex;
    private String gameDir;
    private String version;
    private String sysLibDir = "sysLib/";
    private String classPath;
    private MainClass mainClass;
    private List<String> logLines = new ArrayList<>();
    private int logCounter = 0;

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

        String javaHome = System.getProperty("java.home");
        if(OsIdentifer.isWindows()){
            javaHome = javaHome + "\\bin\\java.exe";
        }
        else{
            javaHome = javaHome + "/bin/java";
        }
        command.add(javaHome);

        command.add("-Djava.library.path=" +  gameDir +sysLibDir);
        command.add("-Dminecraft.client.jar=" + gameDir +"/client.jar");
        command.add("-Duser.dir="+ App.gamePath);

        command.add("-Xmx" + ram);

        command.add("-XX:+UnlockExperimentalVMOptions");
        command.add("-XX:+UseG1GC");
        command.add("-XX:G1NewSizePercent=20");
        command.add("-XX:G1ReservePercent=20");
        command.add("-XX:MaxGCPauseMillis=50");
        command.add("-XX:G1HeapRegionSize=32M");

        command.add("--add-opens=com.google.gson/com.google.gson.stream=ALL-UNNAMED");

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

        command.add("--assetsDir");
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

        StringBuilder builder = new StringBuilder();
        for(String elem : command){
            logger.debug(elem);
            builder.append(elem);
            builder.append(" ");

        }
        logger.info(builder.toString());

        return command;




    }

    public void launch() throws IOException, InterruptedException {
        List<String> commandList = buildCommand();
        ProcessBuilder builder = new ProcessBuilder();
        builder.directory(new File(App.gamePath));
        builder.command(commandList);

        //final Process command = re.exec(cmdString, args.toArray(new String[0]));
        Process command = builder.start();
        this.error = new BufferedReader(new InputStreamReader(command.getErrorStream()));
        this.input = new BufferedReader(new InputStreamReader(command.getInputStream()));
        // Wait for the application to Finish
        String line;

        while ((line = this.input.readLine()) != null) {
            processLogs(line);
        }
        this.input.close();

        this.exitVal = command.exitValue();
        if (this.exitVal != 0) {
            throw new IOException("Failed to execute Minecraft:\n " +getExecutionLog());
        }
        logger.info("Minecraft is now closed, bye bye !");
        Thread.sleep(2000);
        Platform.runLater(() -> App.getLogStage().close());


    }

    private void processLogs(String line){
        line = line.trim();
        if(line.startsWith("<")){
            if(line.startsWith("<log4j:Event")){
                logLines.add(0, line);
            }
            if(line.startsWith("<log4j:Message>")){
                logLines.add(1, line);
            }
            if(line.startsWith("</log4j:Event>")){
                logLines.add(2, line);
                String lines = logLines.get(0) + logLines.get(1) + logLines.get(2);
                try{
                    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                    InputSource is = new InputSource(new StringReader(lines));
                    Document document = documentBuilder.parse(is);
                    Node event = document.getFirstChild();
                    String logger = event.getAttributes().getNamedItem("logger").getNodeValue();
                    String level = event.getAttributes().getNamedItem("level").getNodeValue();
                    String message = event.getFirstChild().getFirstChild().getNodeValue();
                    LogManager.getLogger(logger).log(Level.getLevel(level), message);
                } catch (ParserConfigurationException | IOException | SAXException e) {
                    logger.catching(e);
                }
            }
        }
        else{
            LogManager.getLogger("APP_STDOUT").info(line);
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


