package tk.minecraftroyale.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import tk.minecraftroyale.MinecraftRoyale;

import java.util.List;

import static org.bukkit.Bukkit.getLogger;

@SuppressWarnings("unused")
public final class DeathListener implements Listener {
    static final private MinecraftRoyale plugin = JavaPlugin.getPlugin(MinecraftRoyale.class);
    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        MinecraftRoyale.appender.logLine("Player " + event.getPlayer().getDisplayName() + " logged in with UUID " + event.getPlayer().getUniqueId());
        getLogger().info(event.getPlayer().getDisplayName() + " logged in");
        Object obj = plugin.getConfig().get("playerData." + event.getPlayer().getUniqueId() + ".regenHealth");
        plugin.getLogger().info("found player's regen health: " + obj);

        List inventoriesToClear = plugin.getConfig().getStringList("inventoriesToClear");
        if(inventoriesToClear.contains(event.getPlayer().getUniqueId())){
            inventoriesToClear.remove(event.getPlayer().getUniqueId());
            plugin.getConfig().set("inventoriesToClear", inventoriesToClear);
            event.getPlayer().getInventory().clear();
        }


        new BukkitRunnable() {
            @Override
            public void run() {
                plugin.royaleWorlds.manager.addPlayer(event.getPlayer());
            }
        }.runTaskLater(DeathListener.plugin, 10);

        if(obj != null && (Boolean) obj){
            new BukkitRunnable() {
                @Override
                public void run() {
                    MinecraftRoyale.boostPlayerHealth(event.getPlayer());

//                    setScoreBoard(event.getPlayer());
                }

            }.runTaskLater(DeathListener.plugin, 10);
        }


    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            MinecraftRoyale.appender.playerDeath(victim.getDisplayName(), victim.getUniqueId().toString(), event.getDeathMessage());
        }else{
            MinecraftRoyale.appender.playerKill(victim.getDisplayName(), victim.getUniqueId().toString(), killer.getDisplayName(), killer.getUniqueId().toString(), event.getDeathMessage());
            String path = "playerData." + killer.getUniqueId() + ".points";
            plugin.getLogger().info("had points: " + plugin.getConfig().getInt(path));
            int points = plugin.getConfig().getInt(path) + plugin.getConfig().getInt("gameSettings.points.normal");
            // TODO: add points based on who has the most
            plugin.getConfig().set(path, points);
//          event.setDeathMessage(event.getDeathMessage() + " " + event.getEntity().getDisplayName() + " was yote by " + killer.getDisplayName());
        }
    }


    public void setScoreBoard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        @SuppressWarnings("deprecation") Objective obj = board.registerNewObjective("ServerName", "dummy");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName("Test Server ");
        Score onlineName = obj.getScore(ChatColor.GRAY + "» Online");
        onlineName.setScore(15);
        Team onlineCounter = board.registerNewTeam("onlineCounter");
        onlineCounter.addEntry(ChatColor.BLACK + "" + ChatColor.WHITE);
        if (Bukkit.getOnlinePlayers().size() == 0) {
            onlineCounter.setPrefix(ChatColor.DARK_RED + "0" + ChatColor.RED + "/" + ChatColor.DARK_RED + Bukkit.getMaxPlayers());
        } else {
            onlineCounter.setPrefix("" + ChatColor.DARK_RED + Bukkit.getOnlinePlayers().size() + ChatColor.RED + "/" + ChatColor.DARK_RED + Bukkit.getMaxPlayers());
        }
        obj.getScore(ChatColor.BLACK + "" + ChatColor.WHITE).setScore(14);
        Score money = obj.getScore(ChatColor.GRAY + "» Money");
        money.setScore(13);

        Team moneyCounter = board.registerNewTeam("moneyCounter");
        moneyCounter.addEntry(ChatColor.RED + "" + ChatColor.WHITE);
        moneyCounter.setPrefix(ChatColor.GREEN + "$" + plugin.getConfig().getInt("playerData." + player.getUniqueId() + ".points"));
        obj.getScore(ChatColor.RED + "" + ChatColor.WHITE).setScore(12);
        player.setScoreboard(board);


    }

//    PlayerEvent
}
