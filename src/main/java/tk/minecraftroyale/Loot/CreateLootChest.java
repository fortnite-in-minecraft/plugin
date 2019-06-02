package tk.minecraftroyale.Loot;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.plugin.java.JavaPlugin;
import tk.minecraftroyale.MinecraftRoyale;

public class CreateLootChest {
    public static void createLootChest(String loot, World world) {
        createLootChest(loot, world, 0, 0);
    }

    public static void createLootChest(String loot, World world, int x, int z){
        Block block = world.getHighestBlockAt(x, z);
        Location location = block.getLocation();
        block.setType(Material.CHEST);
        BlockState state = block.getState();
        ((Chest) state).setLootTable(Bukkit.getLootTable(new NamespacedKey(JavaPlugin.getPlugin(MinecraftRoyale.class), loot)));
        state.update();
        System.out.println("added loot " + loot + "@" + location.toString());
    }
}
