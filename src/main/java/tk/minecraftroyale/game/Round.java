package tk.minecraftroyale.game;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import tk.minecraftroyale.Listeners.PlayerLoginListener;
import tk.minecraftroyale.MinecraftRoyale;
import tk.minecraftroyale.Scheduler.GameEnd;
import tk.minecraftroyale.Scheduler.Time;
import tk.minecraftroyale.WorldStuff.RoyaleWorlds;

import java.util.ArrayList;
import java.util.List;

public class Round {

    private Time length;
    private World world;
    private MinecraftRoyale plugin;
    private Runnable wborderShrinkPart2Callback;

    @org.jetbrains.annotations.Contract(pure = true)
    public Round(MinecraftRoyale plugin, Time length, World world, Runnable wborderShrinkPart2Callback) {
        this.plugin = plugin;
        this.length = length;
        this.world = world;
        this.wborderShrinkPart2Callback = wborderShrinkPart2Callback;
    }

    public void teleportAllToRoundWorld() {
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.getPlayer() != null) {
                offlinePlayer.getPlayer().teleport(RoyaleWorlds.getRandomLocation(world));
            } else {
                PlayerLoginListener.addLoginCallback(offlinePlayer, (player) -> player.teleport(RoyaleWorlds.getRandomLocation(world)));
            }
        }
    }

    public void checkStatus(){
        checkGeneric("roundEnd", () -> endRound());
        checkGeneric("wborderShrinkPart2", () -> wborderShrinkPart2Callback.run());
    }

    @SuppressWarnings("Duplicates")
    private void checkGeneric(String nameOfTheThingToCheck, Runnable callback){
        MinecraftRoyale plugin = JavaPlugin.getPlugin(MinecraftRoyale.class);
        @NotNull FileConfiguration config = plugin.getConfig();

        long unixSecondsToEndAt = config.getLong("dates." + nameOfTheThingToCheck);
        long now = System.currentTimeMillis() / 1000l;
        long duration = config.getLong("timeConfig." + nameOfTheThingToCheck);
        long timer;
        if (unixSecondsToEndAt - now < 1) {
            plugin.getLogger().info("updating");
            config.getLong("timeConfig." + nameOfTheThingToCheck);
            config.set("dates." + nameOfTheThingToCheck, now + duration);
            timer = duration;
        }else{
            timer = unixSecondsToEndAt - now;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                callback.run();
            }

        }.runTaskLater(plugin, timer * 20);


        plugin.saveConfig();
        plugin.getLogger().info(nameOfTheThingToCheck +" in " + timer + "s " + unixSecondsToEndAt + " - " + now);
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

            if(p.getPlayer() != null) p.getPlayer().getInventory().clear();
            else{
                List l = plugin.getConfig().getStringList("inventoriesToClear");
                l.add(p.getUniqueId().toString());
                plugin.getConfig().set("inventoriesToClear", l);
            }
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