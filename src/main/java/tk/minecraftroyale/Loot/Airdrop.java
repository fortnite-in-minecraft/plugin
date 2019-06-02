package tk.minecraftroyale.Loot;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.loot.LootTable;
import org.bukkit.plugin.java.JavaPlugin;
import tk.minecraftroyale.MinecraftRoyale;

public class Airdrop {
    public static void airdrop(World world) {
        airdrop(world, 0, 0);
    }

    public static void airdrop(World world, int x, int z){
        Block block = world.getHighestBlockAt(x, z);
        CreateLootChest.createLootChest("airdrop", world, x, z);
    }
}
