package tk.minecraftroyale.game;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import tk.minecraftroyale.Listeners.PlayerLoginListener;
import tk.minecraftroyale.MinecraftRoyale;
import tk.minecraftroyale.Scheduler.Time;
import tk.minecraftroyale.WorldStuff.RoyaleWorlds;

public class Round {

    private Time length;
    private World world;
    private MinecraftRoyale plugin;

    public Round(MinecraftRoyale plugin, Time length, World world) {
        this.length = length;
        this.world = world;
    }

    private void teleportAllToRoundWorld() {
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.getPlayer() != null) {
                offlinePlayer.getPlayer().teleport(RoyaleWorlds.getRandomLocation(world));
            } else {
                PlayerLoginListener.addLoginCallback(offlinePlayer, (player) -> player.teleport(RoyaleWorlds.getRandomLocation(world)));
            }
        }
    }

    public World getWorld() {
        return world;
    }


}
