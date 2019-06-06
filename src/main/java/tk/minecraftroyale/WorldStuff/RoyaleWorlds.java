package tk.minecraftroyale.WorldStuff;

import javax.annotation.Nonnull;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import tk.minecraftroyale.Exceptions.ConfigException;

import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Random;

public class RoyaleWorlds {

    private final JavaPlugin plugin;
    private static final HashMap<Integer, World> worlds = new HashMap<>();

    public RoyaleWorlds(JavaPlugin plugin) { this.plugin = plugin; }

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


    public void generateWorld(int roundNum, @Nullable CommandSender sender) throws IllegalArgumentException, FileNotFoundException, ConfigException {
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
                .type(WorldType.LARGE_BIOMES)
                .seed(plugin.getConfig().getLong("worlds.world" + roundNum + ".seed")).createWorld();

        if (sender != null) {
            sender.sendMessage("World generation started. You will be notified when it is complete.");
        }
    }

    public void generateWorld(int roundNum) throws IllegalArgumentException, FileNotFoundException, ConfigException {
        generateWorld(roundNum, null);
    }

    public void setUpWorldBorder(int world) {
        World w = getWorld(world);
        if(w == null){
            throw new IllegalArgumentException();
        }
        setUpWorldBorder(w);
    }

    public void setUpWorldBorder(@Nonnull World world){
        WorldBorder border = world.getWorldBorder();
        border.setCenter(0, 0);
        border.setCenter(0, 0);
        border.setWarningTime(30); // 30 second warning
        border.setWarningDistance(15);
        border.setDamageBuffer(1);
        border.setSize(plugin.getConfig().getInt("worldBorder.startDistance"));
        border.setSize(plugin.getConfig().getInt("worldBorder.secondDistance"), plugin.getConfig().getLong("worldBorder.secondDistanceShrinkTime"));

    }

    public static Location getRandomLocation(@Nonnull World world) {
        Random rand = new Random();
        int wbSize = (int) world.getWorldBorder().getSize();
        int x = rand.nextInt(wbSize) - (wbSize / 2);
        int z = rand.nextInt(wbSize) - (wbSize / 2);
        int y = world.getHighestBlockYAt(x, z);

        return new Location(world, x, y, z);
    }
}
