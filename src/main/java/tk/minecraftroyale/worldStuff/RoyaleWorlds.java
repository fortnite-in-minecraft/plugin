package tk.minecraftroyale.worldStuff;

import javax.annotation.Nonnull;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import tk.minecraftroyale.BossbarManager;
import tk.minecraftroyale.exceptions.ConfigException;

import org.bukkit.plugin.java.JavaPlugin;
import tk.minecraftroyale.loot.LootChest;
import tk.minecraftroyale.MinecraftRoyale;
import tk.minecraftroyale.scheduler.Time;
import tk.minecraftroyale.game.Round;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class RoyaleWorlds {

    private final MinecraftRoyale plugin = JavaPlugin.getPlugin(MinecraftRoyale.class);
    private static final HashMap<Integer, World> worlds = new HashMap<>();
    public BossbarManager manager;

    public RoyaleWorlds(JavaPlugin plugin) { }

    /**
     * Gets a world from a round number if it exists. Does not create it if it doesn't, instead returning null.
     * @param roundNum the round number.
     * @return the world or null.
     */
    @Nullable
    public World getWorld(int roundNum) throws IllegalArgumentException {
        if (roundNum < 1 || roundNum > 7)
            throw new IllegalArgumentException();

        World world;
        if ((world = worlds.get(roundNum)) != null) {
            return world;
        } else {
            world = Bukkit.getWorld("world" + roundNum);
            if (world != null) {
                worlds.put(roundNum, world);
            }
            return world;
        }
    }


    public World generateWorld(int roundNum, @Nullable CommandSender sender) throws IllegalArgumentException, IOException, ConfigException {
        if(sender  != null) sender.sendMessage("LAG time");
        if (roundNum < 1 || roundNum > 7)
            throw new IllegalArgumentException();

//        World mainWorld = plugin.getServer().getWorld("world");
        String worldPathConfigPath = "worlds.world" + roundNum + ".path";
        String worldPath = plugin.getConfig().getString(worldPathConfigPath);

//        if (mainWorld == null) {
//            throw new FileNotFoundException("Unable to find world \"world\". The main world should be called this.");
//        }

        if (worldPath == null) {
            throw new ConfigException(worldPathConfigPath);
        }

        World newWorld = new WorldCreator(worldPath)
//                .copy(mainWorld)
                .generateStructures(true)
//                .type(WorldType.LARGE_BIOMES)
                .seed(plugin.getConfig().getLong("worlds.world" + roundNum + ".seed")).createWorld();

        // doPostWorldGenStuff(sender, newWorld);
        return newWorld;
    }

    public void doPostWorldGenStuff(CommandSender sender, World newWorld, int roundNum){
        if(manager != null) manager.deleteBar();
        manager = new BossbarManager(roundNum, "the round ends");
        try{if(plugin.runner != null) plugin.runner.cancel();}catch(IllegalStateException e){}
        if (sender != null) {
//            sender.sendMessage("World generation started. You will be notified when it is complete.");
        }

        setUpWorldBorder(newWorld);


        try {
            LootChest.installLootTables(newWorld, null);
        } catch (IOException e) {
        }


        newWorld.setGameRule(GameRule.NATURAL_REGENERATION, false);

        // disables F3 coordinates if set to true
        newWorld.setGameRule(GameRule.REDUCED_DEBUG_INFO, false);

        newWorld.setTime(0);

        plugin.getConfig().set("state.currentRound", roundNum);

        MinecraftRoyale.currentRound = new Round((MinecraftRoyale) plugin, new Time(0, 0, 0l, plugin.getConfig().getLong("timeConfig.roundDuration"), 0l), newWorld, () -> setUpWorldBorder(newWorld, true));
        MinecraftRoyale.currentRound.teleportAllToRoundWorld();
        MinecraftRoyale.currentRound.checkStatus();

        for(OfflinePlayer player : Bukkit.getOfflinePlayers()){
            plugin.getConfig().set("state.playerData." + player.getUniqueId().toString() + ".isDead", false);
            plugin.getConfig().set("state.playerData." + player.getUniqueId().toString() + ".hasJoined", false);
        }

        for(Player player : Bukkit.getOnlinePlayers()){
            plugin.getConfig().set("state.playerData." + player.getUniqueId().toString() + ".hasJoined", true);
        }
        try{if(plugin.runner != null)plugin.runner.cancel();}catch(IllegalStateException e){}
        plugin.runner = new BukkitRunnable() {
            @Override
            public void run() {
                plugin.currentRound.autosaveStatus();
            }
        };
        plugin.runner.runTaskTimer(plugin, 1, 10);

        Bukkit.broadcastMessage("STARTING NEW ROUND # " + newWorld.getName().substring(5));
        plugin.appender.roundInfo(roundNum, " is starting");
        plugin.getConfig().set("state.isInProgress", true);
    }

    public void generateWorld(int roundNum) throws IllegalArgumentException, IOException, ConfigException {
        generateWorld(roundNum, null);
    }

    public void setUpWorldBorder(int world) {
        World w = getWorld(world);
        if(w == null){
            throw new IllegalArgumentException();
        }
        setUpWorldBorder(w);
    }

    public void setUpWorldBorder(@Nonnull World world) {
        setUpWorldBorder(world, plugin.getConfig().getInt("worldBorder.startDistance"), plugin.getConfig().getInt("worldBorder.secondDistance"), plugin.getConfig().getLong("worldBorder.startDistanceTime"));
    }

    public void setUpWorldBorder(@Nonnull World world, boolean secondRound) {
        if(secondRound){
            plugin.getLogger().info("secondRound true");
            Bukkit.broadcastMessage("The world border will be shrinking for the final time!");
            plugin.appender.roundInfo(Character.getNumericValue(world.getName().charAt(5)), "\'s worldborder is shrinking for the final time");
            setUpWorldBorder(world, plugin.getConfig().getInt("worldBorder.secondDistance"), plugin.getConfig().getInt("worldBorder.finalDistance"), plugin.getConfig().getLong("timeConfig.roundEnd") - plugin.getConfig().getLong("timeConfig.wborderShrinkPart2"));
        }else{
            setUpWorldBorder(world);
        }
    }

    public void setUpWorldBorder(@Nonnull World world, int firstDist, int secondDistance, long time){
        WorldBorder border = world.getWorldBorder();
        border.setCenter(0, 0);
        border.setCenter(0, 0);
        border.setWarningTime(30); // 30 second warning
        border.setWarningDistance(15);
        border.setDamageBuffer(1);
        plugin.getLogger().info("set wborder to " + firstDist + ", " + secondDistance + " after " + time + " seconds in world " + world.getName());
        border.setSize(firstDist);
        border.setSize(secondDistance, time);

    }

    public static Location getRandomLocation(@Nonnull World world) {
        Random rand = new Random();
        int wbSize = (int) world.getWorldBorder().getSize();
        int x = rand.nextInt(wbSize) - (wbSize / 2);
        int z = rand.nextInt(wbSize) - (wbSize / 2);

        // Load the chunk. Chunks aren't cubic (yet ;)), so we can safely load the chunk from the location at bedrock.
        world.getChunkAt(new Location(world, x, 0, z)).load(true);

        // Get the location at the top
        Location location = world.getHighestBlockAt(x, z).getLocation();

        // Make sure it's actually a safe location, by checking if the block above it is air, and
        // making sure the block itself isn't lava (surface lava pools are annoying).
        // also avoid oceans because they are disadvantageous.
        if (world.getBlockAt(location).getType() == Material.WATER || world.getBlockAt(location).getType() == Material.LAVA || world.getBlockAt(location).getRelative(BlockFace.UP).getType() != Material.AIR) {

            // Recurse to try again. When a safe location is found, it will resolve up the stack and be returned from the initial call.
            return getRandomLocation(world);
        }

        return location;

        // Max's code; left for reference
//        Location finalLoc = null;
//        Location loc;
//        for (int y2 = 250; y2 > 1; y2--) {
//            for (int y = y2; y < 255; y--) {
//                loc = new Location(world, x, y, z);
//                if (loc.getBlock().getType() == Material.AIR && loc.add(0, 1, 0).getBlock().getType() == Material.AIR) {
//                    finalLoc = loc;
//                } else {
//                    return finalLoc;
//                }
//            }
//        }
//        return null;
    }
}
