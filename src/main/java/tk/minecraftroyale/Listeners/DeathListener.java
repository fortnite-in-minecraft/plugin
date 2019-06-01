package tk.minecraftroyale.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import static org.bukkit.Bukkit.getLogger;

@SuppressWarnings("unused")
public final class DeathListener implements Listener {
    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        getLogger().info(event.getPlayer().getName() + " logged in");
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
