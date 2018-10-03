package Broken.Utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Extractor {
    private Logger logger = LogManager.getLogger();

    public void extrac(String destDir, String jarFile) throws IOException {

        logger.debug("Extracting " + jarFile + " to " + destDir);
        JarFile jar = new JarFile(jarFile);
        Enumeration enumEntries = jar.entries();
        File dest = new File(destDir);
        if(!dest.exists()){
            dest.mkdirs();
        }

        while (enumEntries.hasMoreElements()) {

            JarEntry file = (JarEntry) enumEntries.nextElement();
            File f = new File(destDir + File.separator + file.getName());
            if (file.isDirectory()) { // if its a directory, create it
                f.mkdir();
                continue;
            }
            InputStream is = jar.getInputStream(file); // get the input stream
            FileOutputStream fos = new FileOutputStream(f);
            while (is.available() > 0) {  // write contents of 'is' to 'fos'
                fos.write(is.read());
            }
            fos.close();
            is.close();
        }
        jar.close();
    }
}
