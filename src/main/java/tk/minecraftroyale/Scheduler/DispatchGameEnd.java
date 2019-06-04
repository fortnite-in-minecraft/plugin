package tk.minecraftroyale.Scheduler;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import tk.minecraftroyale.MinecraftRoyale;

import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DispatchGameEnd {
    public static void dispatchGameEnd() {
        MinecraftRoyale plugin = JavaPlugin.getPlugin(MinecraftRoyale.class);
        BukkitTask task = new GameEnd(plugin).runTaskLater(plugin, 20);


//        String roundEndDateString = plugin.getConfig().getString("dates.roundEnd");
//        System.out.println("roundEndDateString " + roundEndDateString);
//        if (roundEndDateString == null) {
//            plugin.getConfig().set("dates.roundEnd", DateTimeFormatter.ISO_INSTANT.format(LocalTime.now().plusSeconds(plugin.getConfig().getLong("dates.roundDuration"))));
//        }else{
//            LocalTime date = LocalTime.from(Instant.parse(roundEndDateString));
//
//            System.out.println("got date " + date);
//        }
    }
}
