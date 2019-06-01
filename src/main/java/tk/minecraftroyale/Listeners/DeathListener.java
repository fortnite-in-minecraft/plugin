package tk.minecraftroyale.Listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import tk.minecraftroyale.MinecraftRoyale;

import static org.bukkit.Bukkit.getLogger;

public final class DeathListener implements Listener {
    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        getLogger().info(event.getPlayer().getName() + " logged in");
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event){
        getLogger().info("");
        Object killer = event.getEntity().getKiller();
        if(killer != null) {
            if(killer instanceof Player) {
                getLogger().info(event.getEntity().getDisplayName() + " was yote");
                event.setDeathMessage(event.getEntity().getDisplayName() + " was yote by " + ((Player) killer).getDisplayName());
            }else{
                event.setDeathMessage("something2 " + event.getDeathMessage());
            }
        }else{
            event.setDeathMessage("something " + event.getDeathMessage());
        }
    }
}
