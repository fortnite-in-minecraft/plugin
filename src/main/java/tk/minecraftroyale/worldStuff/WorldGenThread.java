package tk.minecraftroyale.worldStuff;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;
import tk.minecraftroyale.MinecraftRoyale;

import java.util.ArrayList;
import java.util.function.Consumer;

public class WorldGenThread extends Thread {

    private final WorldCreator creator;
    private final Consumer<World> callback;
    private final JavaPlugin plugin = JavaPlugin.getPlugin(MinecraftRoyale.class);

    private static ArrayList<String> inProgress = new ArrayList<>();

    private boolean done;

    public WorldGenThread(WorldCreator creator, Consumer<World> callback) {
        this.creator = creator;
        this.callback = callback;
        this.done = false;
    }

    @Override
    public void run() {
//        String name = creator.name();
//
//        if (threadInProgress(name)) return;
//        inProgress.add(name);
//        World world = creator.createWorld();
//        World mainWorld = plugin.getServer().getWorld("world");
//        String worldPathConfigPath = "worlds.world" + roundNum + ".path";
//        String worldPath = plugin.getConfig().getString(worldPathConfigPath);
//
//        if (mainWorld == null) {
//            throw new FileNotFoundException("Unable to find world \"world\". The main world should be called this.");
//        }
//
//        if (worldPath == null) {
//            throw new ConfigException(worldPathConfigPath);
//        }
//
//        creator
//                .copy(mainWorld)
//                .generateStructures(true)
//                .type(WorldType.LARGE_BIOMES)
//                .seed(plugin.getConfig().getLong("worlds.world" + roundNum + ".seed"));
//
//        new WorldGenThread(worldCreator, world -> {
//            addWorld(roundNum, world);
//            if (sender != null) {
//                sender.sendMessage("World generation complete.");
//            }
//        }).start();
//
//        if (sender != null) {
//            sender.sendMessage("World generation started. You will be notified when it is complete.");
//        }
//        callback.accept(world);
//        inProgress.remove(name);
    }

    public boolean isDone() {
        return this.done;
    }

    public static boolean threadInProgress(String worldName) {
        for (String s : inProgress)
            if (s.equals(worldName)) return true;

        return false;
    }
}
