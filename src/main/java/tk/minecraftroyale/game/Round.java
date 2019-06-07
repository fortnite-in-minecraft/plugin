package tk.minecraftroyale.game;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import tk.minecraftroyale.Listeners.PlayerLoginListener;
import tk.minecraftroyale.MinecraftRoyale;
import tk.minecraftroyale.Scheduler.Time;
import tk.minecraftroyale.WorldStuff.RoyaleWorlds;

import java.util.ArrayList;
import java.util.List;

public class Round {

    private Time length;
    private World world;
    private MinecraftRoyale plugin;

    public Round(MinecraftRoyale plugin, Time length, World world) {
        this.plugin = plugin;
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



    public void endRound(){
        ArrayList mostPoints = new ArrayList();
        // mostPoints[0] = int maxPoints
        // mostPoints[1..] = OfflinePlayer winningPlayers
        for(OfflinePlayer p : Bukkit.getOfflinePlayers()) {
            plugin.getLogger().info("getting data for " + p.getUniqueId());
            int points = plugin.getConfig().getInt("playerData." + p.getUniqueId() + ".points");
            if(mostPoints.size() < 2){
                mostPoints.add(points);
                mostPoints.add(p);
            }else{
                int competition = (int) mostPoints.get(0);
                if(points > competition){
                    mostPoints.clear();
                    mostPoints.add(points);
                    mostPoints.add(p);
                }else if(points == competition){
                    mostPoints.add(p);
                }
            }
            plugin.getConfig().set("playerData." + p.getUniqueId() + ".points", 0);
        }

        int maxPoints = (int) mostPoints.get(0);
        mostPoints.remove(0);

        plugin.getLogger().info("Winning points: " + maxPoints);
        String str = String.valueOf(
                mostPoints.stream().reduce((a, b) -> "" + a + ", " + ((OfflinePlayer) b).getName())
                        .get()
        );
        Bukkit.broadcastMessage("WINNERS: " + str);

        for(Object winner : mostPoints){
            int oldGamePoints = plugin.getConfig().getInt("playerData." + ((OfflinePlayer) winner).getUniqueId() + ".gamePoints");
            plugin.getConfig().set("playerData." + ((OfflinePlayer) winner).getUniqueId() + ".gamePoints", oldGamePoints + 1);
        }
    }

    public World getWorld() {
        return world;
    }


    public Time getLength() {
        return length;
    }
}