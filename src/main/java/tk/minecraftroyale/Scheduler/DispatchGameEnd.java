package tk.minecraftroyale.Scheduler;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import tk.minecraftroyale.MinecraftRoyale;

public class DispatchGameEnd {
    public static void dispatchGameEnd() {
        MinecraftRoyale plugin = JavaPlugin.getPlugin(MinecraftRoyale.class);


        long unixSecondsToEndAt = plugin.getConfig().getLong("dates.roundEnd");
        long now = System.currentTimeMillis() / 1000l;
        long duration = plugin.getConfig().getLong("timeConfig.roundDuration");
        long timer;
        if (unixSecondsToEndAt - now < 1) {
            plugin.getLogger().info("updating");
            plugin.getConfig().getLong("timeConfig.roundDuration");
            plugin.getConfig().set("dates.roundEnd", now + duration);
            timer = duration;
        }else{
            timer = unixSecondsToEndAt - now;
        }
        BukkitTask task = new GameEnd(plugin).runTaskLater(plugin, timer * 20);


        plugin.saveConfig();
        plugin.getLogger().info("round ending in " + timer + "s " + unixSecondsToEndAt + " - " + now);
    }
}
