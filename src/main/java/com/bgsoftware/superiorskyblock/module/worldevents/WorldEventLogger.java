package com.bgsoftware.superiorskyblock.module.worldevents;

import com.bgsoftware.superiorskyblock.api.island.Island;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WorldEventLogger {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final File logFile;

    public WorldEventLogger(File moduleFolder) {
        this.logFile = new File(moduleFolder, "worldevents.log");
    }

    public void log(Island island, WorldEventType type, int instability, String result) {
        String line = "[" + LocalDateTime.now().format(FMT) + "] "
                + type.getDisplayName()
                + " | Island: " + island.getOwner().getName()
                + " | Level: " + island.getIslandLevel().intValue()
                + " | Instability: " + instability + "%"
                + " | Result: " + result;
        try (FileWriter fw = new FileWriter(logFile, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
