package tk.minecraftroyale.Listeners;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import tk.minecraftroyale.MinecraftRoyale;

import static org.bukkit.Bukkit.getLogger;

@SuppressWarnings("unused")
public final class DeathListener implements Listener {
    static final Plugin plugin = JavaPlugin.getPlugin(MinecraftRoyale.class);
    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        getLogger().info(event.getPlayer().getName() + " logged in");
        Object obj = plugin.getConfig().get("playerData." + event.getPlayer().getUniqueId() + ".regenHealth");
        plugin.getLogger().info("found player's regen health: " + obj);
        if(obj != null && (Boolean) obj){
            //noinspection ConstantConditions
            new BukkitRunnable() {
                @Override
                public void run() {
                    MinecraftRoyale.boostPlayerHealth(event.getPlayer());
                }

            }.runTaskLater(this.plugin, 10);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            event.setDeathMessage(event.getEntity().getDisplayName() + " was yote by " + killer.getDisplayName());
        } else {
            // If the player wasn't killed by another player

            event.setDeathMessage(event.getEntity().getDisplayName() + " was yote");

        }
    }
}
