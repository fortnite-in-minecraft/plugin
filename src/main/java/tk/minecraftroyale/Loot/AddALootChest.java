package tk.minecraftroyale.Loot;

import org.bukkit.World;
import org.bukkit.WorldBorder;

import java.lang.reflect.Array;
import java.util.Random;

public class AddALootChest {
    public static int[] addALootChest(World world){
        WorldBorder border = world.getWorldBorder();
        double size = border.getSize();
        int x = (int) ((Math.random() * size) - (size / 2));
        int z = (int) ((Math.random() * size) - (size / 2));
        CreateLootChest.createLootChest("loot_chest", world, x, z);
        return new int[]{x, z};
    }
}
