package moveIt;

import necesse.engine.GlobalData;
import necesse.inventory.InventoryItem;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

public class BackupFileGenerator {
    static File modDirectory = new File(GlobalData.appDataPath() + "cfg/mods/moveit/");
    static File backupFile;

    public static File getModSavesPath() {
        if (!modDirectory.exists()) {
            modDirectory.mkdir();
        }
        return modDirectory;
    }

    public static File generateNewFile(int tileX, int tileY, InventoryItem item) {
        backupFile = new File (getModSavesPath().getPath() + "/" + tileX + "_" + tileY + "_" + LocalDateTime.now().getYear() + "_" + LocalDateTime.now().getMonth() + "_" + LocalDateTime.now().getDayOfMonth() + "_" + LocalDateTime.now().getHour() + "_" + LocalDateTime.now().getMinute() + "_" + LocalDateTime.now().getSecond() + ".dat");
        try {
            FileWriter generateData = new FileWriter(backupFile);
            generateData.write(item.getGndData().toString());
            generateData.close();
        } catch (IOException e) {
            System.err.println("Error generating backup file.");
            e.printStackTrace();
        }
        purgeOldFiles();
        return backupFile;
    }

    //purge generated backup files once over the limit
    public static void purgeOldFiles() {
        if (modDirectory.exists()) {
            if (Objects.requireNonNull(modDirectory.listFiles()).length > 50) {
                long lowestTime = 0;
                File lowestFile = null;
                Iterator<File> files = Arrays.stream(Objects.requireNonNull(modDirectory.listFiles())).iterator();
                while (files.hasNext()) {
                    File file = (File) files.next();
                    if (lowestTime == 0) {
                        lowestTime = file.lastModified();
                        lowestFile = file;
                    } else if (file.lastModified() < lowestTime) {
                        lowestTime = file.lastModified();
                        lowestFile = file;
                    }
                }
                lowestFile.delete();
            }
        }
    }
}
