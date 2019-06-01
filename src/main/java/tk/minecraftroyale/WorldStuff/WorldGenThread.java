package tk.minecraftroyale.WorldStuff;

import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.util.ArrayList;
import java.util.function.Consumer;

public class WorldGenThread extends Thread {

    private final WorldCreator creator;
    private final Consumer<World> callback;

    private static ArrayList<String> inProgress = new ArrayList<>();

    private boolean done;

    public WorldGenThread(WorldCreator creator, Consumer<World> callback) {
        this.creator = creator;
        this.callback = callback;
        this.done = false;
    }

    @Override
    public void run() {
        String name = creator.name();

        if (threadInProgress(name)) return;
        inProgress.add(name);
        World world = creator.createWorld();
        callback.accept(world);
        inProgress.remove(name);
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
