package McLauncher.Utils;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Collection;

public class ClassPathBuilder {
    private String path;
    Logger logger = LogManager.getLogger();

    public ClassPathBuilder(String path) {
        this.path = path;
    }

    public String build(){
        File filePath = new File(path + "libraries/");
        Collection<File> files = FileUtils.listFiles(filePath, new String[]{"jar"}, true);
        String seperator;
        if(OsIdentifer.isLinux() || OsIdentifer.isMac()){
            seperator = ":";
        }
        else{
            seperator = ";";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for(File file : files){
            stringBuilder.append(file.getAbsolutePath());
            stringBuilder.append(seperator);
        }

        stringBuilder.append(path).append("client.jar");

        return stringBuilder.toString();
    }
}
