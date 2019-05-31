package tk.minecraftroyale.WorldStuff;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.HashMap;

public class RoyaleWorlds {

    private JavaPlugin plugin;
    private HashMap<Integer, World> worlds = new HashMap<>();

    public RoyaleWorlds(JavaPlugin plugin) { this.plugin = plugin; }

    @Nullable
    public World getWorld(int worldNum) {
        if (worldNum < 1 || worldNum > 7)
            return null;

        World world;
        if ((world = worlds.get(worldNum)) != null) {
            return world;
        } else {
            String worldName = plugin.getConfig().getString("worlds.world" + worldNum + ".path");
            if(worldName != null){
                world = new WorldCreator(worldName);
                Object oldWorld = plugin.getServer().getWorld("world")
                if(oldWorld != null){
                    world.copy(oldWorld);
                }else{
                    // TODO: warn about error
                }
                world.generateStructures(true)
                        .type(WorldType.LARGE_BIOMES)
                        .seed(plugin.getConfig().getLong("worlds.world" + worldNum + ".seed"))
                        .createWorld();

                worlds.put(worldNum, world);
                return world;
            }else{
                // TODO: warn about error
            }
        }
    }

    // TODO create methods to set up borders for a world

}
