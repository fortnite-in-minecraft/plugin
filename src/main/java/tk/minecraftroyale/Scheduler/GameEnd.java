package tk.minecraftroyale.Scheduler;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class GameEnd extends BukkitRunnable {

    private final JavaPlugin plugin;

    public GameEnd(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // What you want to schedule goes here
        plugin.getServer().broadcastMessage("test");
    }

}