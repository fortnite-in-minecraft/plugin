package tk.minecraftroyale.listeners;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

import static java.lang.System.nanoTime;

public class PlayerLoginListener implements Listener {
    private static HashMap<UUID, ArrayList<Consumer<Player>>> loginCallbacks = new HashMap<>();

    public static void addLoginCallback(OfflinePlayer player, Consumer<Player> callback) {
        if (loginCallbacks.get(player.getUniqueId()) == null) {
            ArrayList<Consumer<Player>> callbackList = new ArrayList<>();
            callbackList.add(callback);
            loginCallbacks.put(player.getUniqueId(), callbackList);
        } else {
            loginCallbacks.get(player.getUniqueId()).add(callback);
        }
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        long startTime = nanoTime();
        for (UUID uuid : loginCallbacks.keySet()) {
            if (uuid.equals(event.getPlayer().getUniqueId())) {
                for (Consumer<Player> callback : loginCallbacks.get(uuid)) {
                    callback.accept(event.getPlayer());
                }
                loginCallbacks.remove(uuid);
            }
        }
        Bukkit.getLogger().info("onLogin took " + ((nanoTime() - startTime) / 1000000d) + "ms");
    }
}
