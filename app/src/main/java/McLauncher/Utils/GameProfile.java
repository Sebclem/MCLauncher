package McLauncher.Utils;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameProfile {

    private final String logProfile;
    private GameProfileLoader gameProfileLoader;
    private String ram;
    private String assetDir = "assets/";
    private String assetIndex;
    private String gameDir;
    private String sysLibDir = "sysLib/";
    private String classPath;
    private List<String> logLines = new ArrayList<>();
    private int logCounter = 0;

    private BufferedReader error;
    private BufferedReader input;
    private int exitVal;

    private Logger logger = LogManager.getLogger();

    public GameProfile(GameProfileLoader gameProfileLoader, String ram, String assetIndex, String gameDir, String classPath, String logProfile) {
        this.gameProfileLoader = gameProfileLoader;
        this.ram = ram;
        this.assetIndex = assetIndex;
        this.gameDir = gameDir;
        this.classPath = classPath;
        this.logProfile = logProfile;
    }

    private List<String> buildCommand(){
        ArrayList<String> command = new ArrayList<>();


        String javaHome = getJrePath(gameDir);
        if(OsIdentifer.isWindows()){
            javaHome = javaHome + "\\bin\\java.exe";
        }
        else{
            javaHome = javaHome + "/bin/java";
        }
        command.add(javaHome);
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
        List<String> jvmArgs = getJvmArgs();
        command.addAll(jvmArgs);

        command.add(gameProfileLoader.getMainClass());

        List<String> gameArgs = getGameArgs();
        command.addAll(gameArgs);

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

    private List<String> getJvmArgs(){
        String rawArgs = SaveUtils.getINSTANCE().get("defaultJvmArgs");
        rawArgs = replaceAllValues(rawArgs);
        List<String> list = new ArrayList<>(List.of(rawArgs.split("\\|")));
        if(gameProfileLoader.getRawGameType().equals("FORGE")){
            String rawForgeArgs = SaveUtils.getINSTANCE().get("forgeJvmArgs");
            rawForgeArgs = replaceAllValues(rawForgeArgs);
            list.addAll(0, List.of(rawForgeArgs.split("\\|")));
        }
        return list;
    }

    private List<String> getGameArgs(){
        String rawArgs = SaveUtils.getINSTANCE().get("defaultGameArgs");
        rawArgs = replaceAllValues(rawArgs);
        List<String> list = new ArrayList<>(List.of(rawArgs.split("\\|")));
        if(gameProfileLoader.getRawGameType().equals("FORGE")){
            String rawForgeArgs = SaveUtils.getINSTANCE().get("forgeGameArgs");
            rawForgeArgs = replaceAllValues(rawForgeArgs);
            list.addAll(0, List.of(rawForgeArgs.split("\\|")));
        }
        return list;
    }

    private Map<String, String> getArgValueMapper(){
        Map<String, String> mapper = new HashMap<>();
        mapper.put("natives_directory", (gameDir + sysLibDir).replaceAll("\\\\", "\\\\\\\\"));
        mapper.put("launcher_name", "McLauncher SC");
        mapper.put("launcher_version", "McLauncher SC");
        mapper.put("classpath", (classPath).replaceAll("\\\\", "\\\\\\\\"));
        mapper.put("auth_player_name", gameProfileLoader.getAccount().getDisplayName());
        mapper.put("version_name", gameProfileLoader.getVersion());
        mapper.put("game_directory", gameDir.replaceAll("\\\\", "\\\\\\\\"));
        mapper.put("assets_root", (gameDir + assetDir).replaceAll("\\\\", "\\\\\\\\"));
        mapper.put("assets_index_name", assetIndex);
        mapper.put("auth_uuid", gameProfileLoader.getAccount().getUUID());
        mapper.put("auth_access_token", gameProfileLoader.getAccount().getAccessToken());
        mapper.put("user_type","mojang");
        mapper.put("version_type", "release");
        return mapper;
    }

    private String replaceAllValues(String rawArgs){
        for(Map.Entry<String, String> entry : getArgValueMapper().entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();
            rawArgs = rawArgs.replaceAll("\\$\\{" + key + "}", value);
        }
        return rawArgs;
    }

    private String getJrePath(String gameDir){
        File file = new File(gameDir + "/jre");
        return gameDir + "/jre/" + file.list()[0];
    }
}


