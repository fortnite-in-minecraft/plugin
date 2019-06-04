package tk.minecraftroyale.Scheduler;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SaveConfig extends BukkitRunnable {

    private final JavaPlugin plugin;

    public SaveConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getLogger().info("Saving config");
        plugin.saveConfig();
    }

}