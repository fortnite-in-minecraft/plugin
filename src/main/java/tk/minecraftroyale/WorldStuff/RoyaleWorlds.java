package tk.minecraftroyale.WorldStuff;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import tk.minecraftroyale.Exceptions.ConfigException;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.util.HashMap;

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
    public World getWorld(int roundNum) throws IllegalArgumentException{
        if (roundNum < 1 || roundNum > 7)
            throw new IllegalArgumentException();

        World world;
        if ((world = worlds.get(roundNum)) != null) {
            return world;
        } else {
            World w = Bukkit.getWorld("world" + roundNum);
            if (w != null) {
                worlds.put(roundNum, w);
            }
            return w;
        }
    }


    public void generateWorld(int roundNum, @Nullable CommandSender sender) throws IllegalArgumentException, FileNotFoundException, ConfigException {
        if (roundNum < 1 || roundNum > 7)
            throw new IllegalArgumentException();

        World mainWorld = plugin.getServer().getWorld("world");
        String worldPathConfigPath = "worlds.world" + roundNum + ".path";
        String worldPath = plugin.getConfig().getString(worldPathConfigPath);

        if (mainWorld == null) {
            throw new FileNotFoundException("Unable to find world \"world\". The main world should be called this.");
        }

        if (worldPath == null) {
            throw new ConfigException(worldPathConfigPath);
        }

        WorldCreator worldCreator = new WorldCreator(worldPath)
                .copy(mainWorld)
                .generateStructures(true)
                .type(WorldType.LARGE_BIOMES)
                .seed(plugin.getConfig().getLong("worlds.world" + roundNum + ".seed"));

        new WorldGenThread(worldCreator, world -> {
            addWorld(roundNum, world);
            if (sender != null) {
                sender.sendMessage("World generation complete.");
            }
        }).start();

        if (sender != null) {
            sender.sendMessage("World generation started. You will be notified when it is complete.");
        }
    }

    public void generateWorld(int roundNum) throws IllegalArgumentException, FileNotFoundException, ConfigException {
        generateWorld(roundNum, null);
    }

    public void addWorld(int round, World world) {
        worlds.put(round, world);
    }

    // TODO create methods to set up borders for a world



}
