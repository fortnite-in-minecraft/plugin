package tk.minecraftroyale.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
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

                    setScoreBoard(event.getPlayer());
                }

            }.runTaskLater(this.plugin, 10);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            String path = "playerData." + killer.getUniqueId() + ".points";
            plugin.getLogger().info("had points: " + plugin.getConfig().getInt(path));
            int points = plugin.getConfig().getInt(path) + plugin.getConfig().getInt("gameSettings.points.normal");
            // TODO: add points based on who has the most
            plugin.getConfig().set(path, points);
//          event.setDeathMessage(event.getDeathMessage() + " " + event.getEntity().getDisplayName() + " was yote by " + killer.getDisplayName());
        } else {
            // If the player wasn't killed by another player

//            event.setDeathMessage(event.getDeathMessage() + " " + event.getEntity().getDisplayName() + " was yote");

        }
    }


    public void setScoreBoard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = board.registerNewObjective("ServerName", "dummy");
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
