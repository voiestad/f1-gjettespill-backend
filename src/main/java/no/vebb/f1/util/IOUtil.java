package no.vebb.f1.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class IOUtil {
    public static void getFileNamesInFolder(List<String> fileNames, File folder) throws IOException {
        if (!folder.exists()) {
            throw new IOException("Folder not found");
        }
        File[] filesInFolder = folder.listFiles();
        if (filesInFolder == null) {
            throw new IOException("Folder is not a folder");
        }
        for (File file : filesInFolder) {
            fileNames.add(file.getName());
        }
    }
}
