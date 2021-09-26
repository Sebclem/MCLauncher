package McLauncher.Utils.Installer;

import McLauncher.Utils.Event.Observable;
import McLauncher.Utils.Event.Observer;
import McLauncher.Utils.Exception.DownloadFailException;
import McLauncher.Utils.OsIdentifer;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JreInstaller extends Observable {
    public final int IDLE = 0;
    public final int DOWNLOADING = 1;
    public final int EXTRACTING = 2;
    public final int FINISH = 3;
    public final int ERROR = 4;
    private final String downloadUrl = "https://api.adoptopenjdk.net/v3/binary/latest/${version}/ga/${os}/x64/jre/hotspot/normal/adoptopenjdk";
    private final Logger logger = LogManager.getLogger();
    public int status = IDLE;
    public long downloaded;
    public Downloader downloader;

    public long getTotalSize(String version) throws IOException {
        final URL uri = new URL(getDownloadUrl(version));
        URLConnection ucon;
        ucon = uri.openConnection();
        ucon.connect();
        final String contentLengthStr = ucon.getHeaderField("content-length");
        return Long.parseLong(contentLengthStr);

    }

    private String getDownloadUrl(String version) {
        String os;
        if (OsIdentifer.isLinux()) {
            os = "linux";
        } else if (OsIdentifer.isWindows()) {
            os = "windows";
        } else {
            os = "mac";
        }
        String url = downloadUrl.replace("${version}", version);
        return url.replace("${os}", os);

    }

    public void download(String version, String path) throws InterruptedException, MalformedURLException, DownloadFailException {
        logger.info("Downloading Jre version " + version);
        String ext = OsIdentifer.isWindows() ? ".zip" : ".tar.gz";
        downloader = new Downloader(new URL(getDownloadUrl(version)), path + "jre" + ext);
        downloader.addObserver(new JreInstaller.DlObserver());
        while (downloader.getStatus() == Downloader.DOWNLOADING) {
            Thread.sleep(10);
        }
        if (downloader.getStatus() != Downloader.COMPLETE)
            throw new DownloadFailException();
    }

    public void extract(String gamePath) throws IOException {
        if (OsIdentifer.isWindows()) {
            logger.info("Unzip JRE");
            unZip(gamePath);
            new File(gamePath + "/jre.zip").delete();
        }else{
            tarGz(gamePath);
            new File(gamePath + "/jre.tar.gz").delete();
        }
    }

    private void unZip(String gamePath) throws IOException {
        String fileZip = gamePath + "/jre.zip";
        File destDir = new File(gamePath + "/jre");
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
        ZipEntry zipEntry = zis.getNextEntry();

        while (zipEntry != null) {
            File newFile = newFile(destDir, zipEntry);
            if (zipEntry.isDirectory()) {
                if (!newFile.isDirectory() && !newFile.mkdirs()) {
                    throw new IOException("Failed to create directory " + newFile);
                }
            } else {
                // fix for Windows-created archives
                File parent = newFile.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                // write file content
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
    }

    private File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    private void tarGz(String gamePath) throws IOException {
        File destDir = new File(gamePath + "/jre");
        InputStream fi = Files.newInputStream(Paths.get(gamePath + "jre.tar.gz"));
        InputStream bi = new BufferedInputStream(fi);
        InputStream gzi = new GzipCompressorInputStream(bi);
        ArchiveInputStream i = new TarArchiveInputStream(gzi);
        ArchiveEntry entry = null;
        while ((entry = i.getNextEntry()) != null) {
            if (!i.canReadEntryData(entry)) {
                logger.warn("Fail to extract " + entry.getName());
                continue;
            }
            File file = newFileGz(destDir, entry);
            if (entry.isDirectory()) {
                if (!file.isDirectory() && !file.mkdirs()) {
                    throw new IOException("failed to create directory " + file);
                }
            } else {
                File parent = file.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("failed to create directory " + parent);
                }
                try (OutputStream o = Files.newOutputStream(file.toPath())) {
                    IOUtils.copy(i, o);
                }
            }
        }
    }
    private File newFileGz(File destinationDir, ArchiveEntry archiveEntry) throws IOException {
        File destFile = new File(destinationDir, archiveEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + archiveEntry.getName());
        }

        return destFile;
    }

    private class DlObserver implements Observer {

        @Override
        public void update(Object subject) {
            Downloader downloader = (Downloader) subject;
            if (downloader.getStatus() == Downloader.DOWNLOADING) {
                downloaded = downloader.getProgress();
                change();
            }

        }
    }


}
