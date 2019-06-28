package tk.minecraftroyale.game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Contract;
import tk.minecraftroyale.ClearInventory;
import tk.minecraftroyale.MinecraftRoyale;
import tk.minecraftroyale.worldStuff.RoyaleWorlds;

import java.util.*;

import static tk.minecraftroyale.MinecraftRoyale.getGameSetting;

public class Round {

    private World world;
    private MinecraftRoyale plugin;
    private HashMap<String, Long> unixSecondsThatTheThingWasStartedAt;
    private HashMap<String, Long> secondsLeftThatTheThingWasStartedAt;
    private HashMap<String, BukkitTask> taskHolder;

    @Contract(pure = true)
    public Round(MinecraftRoyale plugin, World world) {
        this.plugin = plugin;
        this.world = world;
        unixSecondsThatTheThingWasStartedAt = new HashMap<>();
        secondsLeftThatTheThingWasStartedAt = new HashMap<>();
        taskHolder = new HashMap<>();
    }

    public void teleportAllToRoundWorld() {
        for (OfflinePlayer offlinePlayer : plugin.getAllPlayers()) {
            if (offlinePlayer.getPlayer() != null) {
                offlinePlayer.getPlayer().teleport(RoyaleWorlds.getRandomLocation(world));
            }  //                PlayerLoginListener.addLoginCallback(offlinePlayer, (player) -> player.teleport(RoyaleWorlds.getRandomLocation(world)));

        }
    }

    public void checkStatus(){
        checkGeneric("roundEnd");
        checkGeneric("wborderShrinkPart2");
    }

    public void autosaveStatus(){
        if(MinecraftRoyale.currentRound != this){
            throw new IllegalStateException("Bad round loop!");
        }

        autosaveGeneric("roundEnd");
        autosaveGeneric("wborderShrinkPart2");
    }

    @SuppressWarnings("Duplicates")
    private void checkGeneric(String nameOfTheThingToCheck){
        MinecraftRoyale plugin = JavaPlugin.getPlugin(MinecraftRoyale.class);
        FileConfiguration config = plugin.getConfig();

        long secondsLeft = config.getLong("secondsLeft." + nameOfTheThingToCheck);
        long now = System.currentTimeMillis() / 1000L;
        long durationOfTheThing = getGameSetting("timeConfig." + nameOfTheThingToCheck, this.world.getName());
        long timer;

        if(secondsLeft > 0){
            timer = secondsLeft;
            secondsLeftThatTheThingWasStartedAt.put(nameOfTheThingToCheck, secondsLeft);
        }else{
            timer = durationOfTheThing;
            secondsLeftThatTheThingWasStartedAt.put(nameOfTheThingToCheck, durationOfTheThing);
        }

        plugin.getLogger().info("checkGeneric: " + nameOfTheThingToCheck + " secondsLeft " + secondsLeft + ", now " + now + ", durationOfTheThing " + durationOfTheThing + ", timer " +  timer);
        unixSecondsThatTheThingWasStartedAt.put(nameOfTheThingToCheck, now);

        if(taskHolder.get(nameOfTheThingToCheck) != null){
            plugin.getLogger().warning("Duplicate callback runnable!!!");
        }else{

            BukkitTask run = new BukkitRunnable() {
                @Override
                public void run() {
                    if(Objects.equals(nameOfTheThingToCheck, "roundEnd")){
                        endRound();
                    }else{
                        plugin.royaleWorlds.setUpWorldBorder(world, true);
                    }
                }

            }.runTaskLater(plugin, timer * 20);
            taskHolder.put(nameOfTheThingToCheck, run);
            plugin.getLogger().info(ChatColor.GREEN + "Running " + nameOfTheThingToCheck + " in " + timer + " seconds");
        }


        autosaveGeneric(nameOfTheThingToCheck);
    }

    private void autosaveGeneric(String nameOfTheThingToCheck){
        FileConfiguration config = plugin.getConfig();

        long now = System.currentTimeMillis() / 1000L;
        if(unixSecondsThatTheThingWasStartedAt == null || unixSecondsThatTheThingWasStartedAt.get(nameOfTheThingToCheck) == null){
            Bukkit.broadcastMessage("the whole game is over.");
        }else {
            long secondsAgoWeStarted = now - unixSecondsThatTheThingWasStartedAt.get(nameOfTheThingToCheck);
            long secondsLeftStartedAt = secondsLeftThatTheThingWasStartedAt.get(nameOfTheThingToCheck);
            long secondsLeft = secondsLeftStartedAt - secondsAgoWeStarted;
            long totalTime = getGameSetting("timeConfig." + nameOfTheThingToCheck, MinecraftRoyale.getCurrentWorld().getName());
            config.set("secondsLeft." + nameOfTheThingToCheck, secondsLeft);


            plugin.getLogger().info("autosaveGeneric: " + nameOfTheThingToCheck + " secondsAgoWeStarted " + secondsAgoWeStarted + ", secondsLeftStartedAt " + secondsLeftStartedAt + ", secondsLeft " + secondsLeft + ", totalTime " + totalTime);
            plugin.saveConfig();



            if(nameOfTheThingToCheck.equals("roundEnd") && plugin.royaleWorlds.manager != null){
                plugin.royaleWorlds.manager.setProgress(1 - ((double) secondsLeft) / totalTime);
            }
        }
    }

    public void endRound(){
        plugin.getLogger().info("Ending round: world name " + world.getName());
        plugin.getLogger().info(Arrays.toString(new Throwable().getStackTrace()));
        MinecraftRoyale.appender.roundInfo(Integer.parseInt(world.getName().substring(5)), " is ending");
        try{
            if(plugin.runner != null){
                plugin.getLogger().info("Cancelling runner...3");
                plugin.runner.cancel();
            }
        }catch(IllegalStateException e){
            plugin.getLogger().info("Couldn't cancel runner...3 " + e.toString());
        }

        ArrayList<Object> mostPoints = new ArrayList<>();
        // mostPoints[0] = int maxPoints
        // mostPoints[1..] = OfflinePlayer winningPlayers
        for(OfflinePlayer p : plugin.getAllPlayers()) {
//            plugin.getLogger().info("getting data for " + p.getUniqueId());
            int points = plugin.getConfig().getInt("state.playerData." + p.getUniqueId() + ".points");
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
            plugin.getConfig().set("state.playerData." + p.getUniqueId() + ".points", 0);

            if(p.getPlayer() != null) ClearInventory.clearInventory(p.getPlayer());
            else{
                List<String> l = plugin.getConfig().getStringList("state.inventoriesToClear");
                l.add(p.getUniqueId().toString());
                plugin.getConfig().set("state.inventoriesToClear", l);
            }
        }

        int maxPoints = (int) mostPoints.get(0);
        mostPoints.remove(0);

//        plugin.getLogger().info("Winning points: " + maxPoints);
        String str = String.valueOf(
                mostPoints.stream().reduce((a, b) -> "" + ((OfflinePlayer) a).getName() + ", " + ((OfflinePlayer) b).getName())
                        .get()
        );
        Bukkit.broadcastMessage("WINNERS: " + str);

        for(Object winner : mostPoints){
            int oldGamePoints = plugin.getConfig().getInt("state.playerData." + ((OfflinePlayer) winner).getUniqueId() + ".gamePoints");
            plugin.getConfig().set("state.playerData." + ((OfflinePlayer) winner).getUniqueId() + ".gamePoints", oldGamePoints + 1);
        }

        plugin.getConfig().set("state.isInProgress", false);


        secondsLeftThatTheThingWasStartedAt.clear();
        unixSecondsThatTheThingWasStartedAt.clear();
        taskHolder.get("roundEnd").cancel();
        taskHolder.get("wborderShrinkPart2").cancel();
        taskHolder.clear();
        plugin.getConfig().set("secondsLeft.roundEnd", 0);
        plugin.getConfig().set("secondsLeft.wborderShrinkPart2", 0);

        int currentRound = Integer.parseInt(MinecraftRoyale.currentRound.getWorld().getName().substring(5));
        if(currentRound == plugin.getConfig().getInt("gameSettings.numWorlds")){
            plugin.getLogger().info("GAME OVER!!!");
            plugin.getConfig().set("state.isInProgress", false);
            MinecraftRoyale.currentRound = null;
            plugin.getConfig().set("state.currentRound", 0);

            ArrayList<ArrayList<Object>> data = new ArrayList<>();
            for(OfflinePlayer p : plugin.getAllPlayers()){
                ArrayList<Object> l = new ArrayList<>();
                l.add(p);
                l.add(plugin.getConfig().getInt("state.playerData." + p.getUniqueId() + ".gamePoints"));
                data.add(l);
            }
            MinecraftRoyale.appender.endGame(data);
        }else{
            currentRound ++;
            plugin.getLogger().info("currentRound " + currentRound);
            World newWorld = Bukkit.getWorld("world" + currentRound);
            if(newWorld != null){
                plugin.royaleWorlds.doPostWorldGenStuff(newWorld, currentRound);
            }
        }
    }

    private World getWorld() {
        return world;
    }
}