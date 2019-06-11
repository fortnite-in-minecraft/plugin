package tk.minecraftroyale.Scheduler;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import tk.minecraftroyale.MinecraftRoyale;
import tk.minecraftroyale.WorldStuff.RoyaleWorlds;

public class GameEnd extends BukkitRunnable {

    private final JavaPlugin plugin;

    public GameEnd(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getServer().broadcastMessage("GAME (round) OVER");
        int currentRound = Integer.parseInt(MinecraftRoyale.currentRound.getWorld().getName().substring(5));
        if(currentRound == 7){
            plugin.getLogger().info("GAME OVER!!!");
        }else{
            currentRound ++;
            World newWorld = Bukkit.getWorld("world" + currentRound);
            if(newWorld != null){
                ((MinecraftRoyale) plugin).royaleWorlds.doPostWorldGenStuff(null, newWorld, currentRound);
            }
        }
    }

}