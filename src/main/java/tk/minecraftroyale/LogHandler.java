package tk.minecraftroyale;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;


public class LogHandler extends Thread {
    private Boolean keepReading = false;

    LogHandler() {
    }

    @Override
    public void run() {
        BufferedReader reader = null;
        try {
            File file = new File("logs/latest.log");
            FileReader fileReader = new FileReader("logs/latest.log");
            reader = new BufferedReader(fileReader);
            //noinspection ResultOfMethodCallIgnored
            reader.skip (file.length());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String line;
        keepReading = true;

        while (keepReading) {
            try {
                assert reader != null;
                line = reader.readLine();

                if (line == null) {
                    //wait until there is more of the file for us to read
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else {
                    FileConfiguration c = JavaPlugin.getPlugin(MinecraftRoyale.class).getConfig();

                    List<String> stringList =c.getStringList("logMessagesOfInterest");
                    boolean shouldLog = false;
                    for(String str : stringList) {
                        if(line.contains(str) && !line.contains("[\"logLine\"")) shouldLog = true;
                    }

                    List<String> stringBlacklist = c.getStringList("logMessagesBlacklist");
                    for(String str : stringBlacklist) {
                        if(line.contains(str)) shouldLog = false;
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