package tk.minecraftroyale.game;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Contract;
import tk.minecraftroyale.ClearInventory;
import tk.minecraftroyale.MinecraftRoyale;
import tk.minecraftroyale.scheduler.Time;
import tk.minecraftroyale.worldStuff.RoyaleWorlds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Round {

    private Time length;
    private World world;
    private MinecraftRoyale plugin;
    private Runnable wborderShrinkPart2Callback;
    private HashMap<String, Long> unixSecondsThatTheThingWasStartedAt;
    private HashMap<String, Long> secondsLeftThatTheThingWasStartedAt;

    @Contract(pure = true)
    public Round(MinecraftRoyale plugin, Time length, World world, Runnable wborderShrinkPart2Callback) {
        this.plugin = plugin;
        this.length = length;
        this.world = world;
        this.wborderShrinkPart2Callback = wborderShrinkPart2Callback;
        unixSecondsThatTheThingWasStartedAt = new HashMap<>();
        secondsLeftThatTheThingWasStartedAt = new HashMap<>();
    }

    public void teleportAllToRoundWorld() {
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.getPlayer() != null) {
                offlinePlayer.getPlayer().teleport(RoyaleWorlds.getRandomLocation(world));
            } else {
//                PlayerLoginListener.addLoginCallback(offlinePlayer, (player) -> player.teleport(RoyaleWorlds.getRandomLocation(world)));
            }
        }
    }

    public void checkStatus(){
        checkGeneric("roundEnd", () -> endRound());
        checkGeneric("wborderShrinkPart2", () -> wborderShrinkPart2Callback.run());
    }

    public void autosaveStatus(){
        autosaveGeneric("roundEnd");
        autosaveGeneric("wborderShrinkPart2");
    }

    @SuppressWarnings("Duplicates")
    private void checkGeneric(String nameOfTheThingToCheck, Runnable callback){
        MinecraftRoyale plugin = JavaPlugin.getPlugin(MinecraftRoyale.class);
        FileConfiguration config = plugin.getConfig();

        long secondsLeft = config.getLong("secondsLeft." + nameOfTheThingToCheck);
        long now = System.currentTimeMillis() / 1000l;
        long durationOfTheThing = config.getLong("timeConfig." + nameOfTheThingToCheck);
        long timer = 100;

        if(secondsLeft > 0){
            timer = secondsLeft;
            secondsLeftThatTheThingWasStartedAt.put(nameOfTheThingToCheck, secondsLeft);
        }else{
            timer = durationOfTheThing;
            secondsLeftThatTheThingWasStartedAt.put(nameOfTheThingToCheck, durationOfTheThing);
        }

        plugin.getLogger().info("checkGeneric: " + nameOfTheThingToCheck + " secondsLeft " + secondsLeft + ", now " + now + ", durationOfTheThing " + durationOfTheThing + ", timer " +  timer);
        unixSecondsThatTheThingWasStartedAt.put(nameOfTheThingToCheck, now);

        new BukkitRunnable() {
            @Override
            public void run() {
                callback.run();
            }

        }.runTaskLater(plugin, timer * 20);


        autosaveGeneric(nameOfTheThingToCheck);
    }

    private void autosaveGeneric(String nameOfTheThingToCheck){
        FileConfiguration config = plugin.getConfig();

        long now = System.currentTimeMillis() / 1000l;
        if(unixSecondsThatTheThingWasStartedAt == null || unixSecondsThatTheThingWasStartedAt.get(nameOfTheThingToCheck) == null){
            Bukkit.broadcastMessage("the whole game is over.");
        }else {
            long secondsAgoWeStarted = now - unixSecondsThatTheThingWasStartedAt.get(nameOfTheThingToCheck);
            long secondsLeftStartedAt = secondsLeftThatTheThingWasStartedAt.get(nameOfTheThingToCheck);
            long secondsLeft = secondsLeftStartedAt - secondsAgoWeStarted;
            long totalTime = config.getLong("timeConfig." + nameOfTheThingToCheck);
            config.set("secondsLeft." + nameOfTheThingToCheck, secondsLeft);


            //plugin.getLogger().info("autosaveGeneric: " + nameOfTheThingToCheck + " secondsAgoWeStarted " + secondsAgoWeStarted + ", secondsLeftStartedAt " + secondsLeftStartedAt + ", secondsLeft " + secondsLeft + ", totalTime " + totalTime);
            plugin.saveConfig();



            if(nameOfTheThingToCheck == "roundEnd" && plugin.royaleWorlds.manager != null){
                plugin.royaleWorlds.manager.setProgress(1 - ((double) secondsLeft) / totalTime);
            }
        }
    }

    public void endRound(){
        plugin.getLogger().info("world name " + world.getName());
        plugin.appender.roundInfo(Character.getNumericValue(world.getName().charAt(5)), " is ending");
        try{if(plugin.runner != null) plugin.runner.cancel();}catch(IllegalStateException e){}

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

            if(p.getPlayer() != null) ClearInventory.clearInventory(p.getPlayer());
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

        plugin.getConfig().set("gameSettings.isInProgress", false);


        secondsLeftThatTheThingWasStartedAt.clear();
        unixSecondsThatTheThingWasStartedAt.clear();
        plugin.getConfig().set("secondsLeft.roundEnd", 0);
        plugin.getConfig().set("secondsLeft.wborderShrinkPart2", 0);

        plugin.getServer().broadcastMessage("GAME (round) OVER");
        int currentRound = Integer.parseInt(MinecraftRoyale.currentRound.getWorld().getName().substring(5));
        if(currentRound == 7){
            plugin.getLogger().info("GAME OVER!!!");
            plugin.getConfig().set("gameSettings.isInProgress", false);
            MinecraftRoyale.currentRound = null;
            plugin.getConfig().set("gameSettings.currentRound", 0);
        }else{
            currentRound ++;
            plugin.getLogger().info("currentRound " + currentRound);
            World newWorld = Bukkit.getWorld("world" + currentRound);
            if(newWorld != null){
                ((MinecraftRoyale) plugin).royaleWorlds.doPostWorldGenStuff(null, newWorld, currentRound);
            }
        }
    }

    public World getWorld() {
        return world;
    }


    public Time getLength() {
        return length;
    }
}