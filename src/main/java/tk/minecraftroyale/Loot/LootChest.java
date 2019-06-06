package tk.minecraftroyale.Loot;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.loot.LootTable;
import org.bukkit.plugin.java.JavaPlugin;
import tk.minecraftroyale.MinecraftRoyale;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Represents a loot chest.
 */
public class LootChest {
    private Location location;
    private LootTable lootTable;

    private LootTable getLootTable() {
        return Bukkit.getLootTable(new NamespacedKey(JavaPlugin.getPlugin(MinecraftRoyale.class),
                this instanceof Airdrop ? "airdrop" : "loot_chest"));
    }

    /**
     * Creates the loot chest at the given location.
     * @param location the location.
     */
    public LootChest(Location location) {
        this.location = location;
        this.lootTable = getLootTable();
    }

    /**
     * Creates the loot chest at a random point in a world.
     * The chest is only guaranteed to be within the world border when the world border's
     * center is at 0, 0.
     * @param world the world in which to spawn the loot chest
     */
    public LootChest(World world) {
        Random rand = new Random();
        int wbSize = (int) world.getWorldBorder().getSize();
        Bukkit.getLogger().info("got world border " + wbSize);
        int x = rand.nextInt(wbSize) - (wbSize / 2);
        int z = rand.nextInt(wbSize) - (wbSize / 2);
        int y = world.getHighestBlockYAt(x, z);

        this.location = new Location(world, x, y, z);
        this.lootTable = getLootTable();
    }

    public void place() {
        Block block = location.getWorld().getHighestBlockAt(location.getBlockX(), location.getBlockZ());
        location = block.getLocation(); // To update the y-axis of the location
        block.setType(Material.CHEST);
        BlockState bs = block.getState();
        ((Chest) bs).setLootTable(lootTable);
        bs.update();
        Bukkit.getLogger().info("Spawned " + (this instanceof Airdrop ? "airdrop" : "loot chest") + " in world " +
                location.getWorld().getName() + " at " +
                location.getBlockX() + " " +
                location.getBlockY() + " " +
                location.getBlockZ());
    }

    /**
     * Gets the location of the loot chest.
     * @return the location.
     */
    public Location getLocation() {
        return location;
    }

    public String getCommandResponse() {
        return "added loot chest at " +
                location.getBlockX() + ", " +
                location.getBlockZ();
    }

    /**
     * Installs loot tables for a world. Only needs to be called once per world.
     * @param w the world.
     * @param sender whoever sent the command, so we can make them reload the world.
     * @throws IOException if something goes wrong when accessing {@code resources/minecraftroyale.zip}.
     */
    public static void installLootTables(World w, CommandSender sender) throws IOException {
        System.out.println(w.getWorldFolder());
        Path basePath = Paths.get(w.getWorldFolder().toString(), "datapacks");
        deleteFolder(Paths.get(basePath.toString(), "minecraftroyale").toFile());

        InputStream is = LootChest.class.getResourceAsStream("/minecraftroyale.zip");
        if(is == null){
            throw new IOException("InputStream was null!");
        }
        ZipInputStream zis = new ZipInputStream(is);


        ZipEntry entry;

        /*
         Read each entry from the ZipInputStream until no
         more entry found indicated by a null return value
         of the getNextEntry() method.
        */
        while ((entry = zis.getNextEntry()) != null) {
            if(entry.isDirectory()) {
                Paths.get(basePath.toString(), entry.getName()).toFile().mkdirs();
            }else{
                System.out.println("Unzipping: " + entry.getName());

                int size;
                byte[] buffer = new byte[2048];

                try (FileOutputStream fos =
                             new FileOutputStream(Paths.get(basePath.toString(), entry.getName()).toFile());
                     BufferedOutputStream bos =
                             new BufferedOutputStream(fos, buffer.length)) {

                    while ((size = zis.read(buffer, 0, buffer.length)) != -1) {
                        bos.write(buffer, 0, size);
                    }
                    bos.flush();
                }
            }
        }

        Bukkit.dispatchCommand(sender, "minecraft:reload");
    }

    private static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
}
