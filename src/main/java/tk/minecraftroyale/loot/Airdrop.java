package tk.minecraftroyale.loot;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import tk.minecraftroyale.MinecraftRoyale;

import javax.annotation.Nullable;

/**
 * Represents an airdrop.
 */
public class Airdrop extends LootChest {
    Location thisLocation;
    private void announce() {
        // TODO Give players a special compass that points to the nearest airdrop instead of telling them the coordinates
        Bukkit.broadcastMessage(ChatColor.BOLD + "" + ChatColor.GREEN + "An airdrop has appeared at the coordinates " + super.getLocation().getBlockX() + ", " + super.getLocation().getBlockY() + ", " + super.getLocation().getBlockZ());
    }

    public Airdrop(Location location) {
        super(location);
    }

    public Airdrop(World world) {
        super(world);
    }

    /**
     * Same as {@link LootChest#place()}, but also announces in chat that
     * an airdrop has been spawned.
     */
    @Override
    public void place() {
        super.place();
        announce();
    }

    /**
     * Calculates whether or not to spawn an airdrop. This is intended to be called every five minutes.
     * @return whether or not an airdrop should be spawned.
     */
    public static boolean dropCheck() {
        int n = Bukkit.getOnlinePlayers().size();
        double probability = 60d;
        int configInt = JavaPlugin.getPlugin(MinecraftRoyale.class).getConfig().getInt("probability.airdrop");
        if(configInt > 0) probability = configInt;
        Bukkit.getLogger().info("probability for airdrop: 1/" + configInt + " " + probability);
        return Math.random() < (n / probability);
    }

    @Nullable
    public static Location runAirdrop(World w){
        if(dropCheck()){
            Airdrop a = new Airdrop(w);
            a.place();
            return a.getLocation();
        }
        return null;
    }
}
