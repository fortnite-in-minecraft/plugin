package tk.minecraftroyale;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.LogRecord;
import java.util.logging.Handler;


public class LogHandler extends Handler{
    String x = null;
    @Override
    public void publish(LogRecord record) {
        FileConfiguration c = JavaPlugin.getPlugin(MinecraftRoyale.class).getConfig();

        List<String> stringList =c.getStringList("logMessagesOfInterest");
        boolean shouldLog = false;
        for(String str : stringList) {
            if(record.getMessage().contains(str)) shouldLog = true;
        }
        if (shouldLog) {
            MinecraftRoyale.appender.logLine(record.getMessage());
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }
}
