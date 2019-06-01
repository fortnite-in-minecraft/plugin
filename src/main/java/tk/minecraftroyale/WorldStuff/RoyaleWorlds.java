package tk.minecraftroyale.WorldStuff;

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
    private final HashMap<Integer, World> worlds = new HashMap<>();

    public RoyaleWorlds(JavaPlugin plugin) { this.plugin = plugin; }

    @Nullable
    public World getWorld(int worldNum) throws FileNotFoundException, ConfigException {
        if (worldNum < 1 || worldNum > 7)
            return null;

        World world;
        if ((world = worlds.get(worldNum)) != null) {
            return world;
        } else {
            World mainWorld = plugin.getServer().getWorld("world");
            String worldPathConfigPath = "worlds.world" + worldNum + ".path";
            String worldPath = plugin.getConfig().getString(worldPathConfigPath);

            if (mainWorld == null) {
                throw new FileNotFoundException("Unable to find world \"world\"");
            }

            if (worldPath == null) {
                throw new ConfigException(worldPathConfigPath);
            }

            world = new WorldCreator(worldPath)
                    .copy(mainWorld)
                    .generateStructures(true)
                    .type(WorldType.LARGE_BIOMES)
                    .seed(plugin.getConfig().getLong("worlds.world" + worldNum + ".seed"))
                    .createWorld();

            worlds.put(worldNum, world);
            return world;
        }
    }

    // TODO create methods to set up borders for a world



}
