package tk.minecraftroyale.Loot;

import org.bukkit.*;

/**
 * Represents an airdrop.
 */
public class Airdrop extends LootChest {
    private void announce() {
        Bukkit.broadcastMessage("An airdrop has appeared! Search for it if you dare...");
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
        return Math.random() < (n / 60d);
    }
}
