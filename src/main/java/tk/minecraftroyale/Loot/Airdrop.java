package tk.minecraftroyale.Loot;

import org.bukkit.*;

import javax.annotation.Nullable;

/**
 * Represents an airdrop.
 */
public class Airdrop extends LootChest {
    Location thisLocation;
    private void announce() {
        Bukkit.broadcastMessage("An airdrop has appeared! Search for it if you dare... " + super.getLocation().getBlockX() + "," + super.getLocation().getBlockY() + "," + super.getLocation().getBlockZ());
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
