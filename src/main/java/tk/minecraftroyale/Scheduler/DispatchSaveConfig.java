package tk.minecraftroyale.Scheduler;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import tk.minecraftroyale.MinecraftRoyale;

public class DispatchSaveConfig {
    public static void dispatchSaveConfig() {
        MinecraftRoyale plugin = JavaPlugin.getPlugin(MinecraftRoyale.class);
        BukkitTask task = new SaveConfig(plugin).runTaskTimer(plugin, 0, 20 * 30);
    }

}
