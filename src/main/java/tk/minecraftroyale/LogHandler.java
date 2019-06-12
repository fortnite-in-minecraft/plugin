package tk.minecraftroyale;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.LogRecord;


public class LogHandler extends Thread {
    String x = null;
    Boolean keepReading = false;

    LogHandler() {
    }

    @Override
    public void run() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("logs/latest.log"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line;
        keepReading = true;

        while (keepReading) {
            try {
                line = reader.readLine();

                if (line == null) {
                    //wait until there is more of the file for us to read
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    FileConfiguration c = JavaPlugin.getPlugin(MinecraftRoyale.class).getConfig();

                    List<String> stringList =c.getStringList("logMessagesOfInterest");
                    boolean shouldLog = false;
                    for(String str : stringList) {
                        if(line.contains(str) && !line.contains("[\"logLine\"")) shouldLog = true;
                    }
                    if (shouldLog) {
                        MinecraftRoyale.appender.logLine(line);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void interrupt() {
        System.out.println("Stopping log handler.");
        keepReading = false;
    }
}