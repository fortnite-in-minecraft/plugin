package tk.minecraftroyale.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import tk.minecraftroyale.ClearInventory;
import tk.minecraftroyale.MinecraftRoyale;
import tk.minecraftroyale.worldStuff.RoyaleWorlds;

import java.util.List;

import static org.bukkit.Bukkit.getLogger;

@SuppressWarnings("unused")
public final class DeathListener implements Listener {
    static final private MinecraftRoyale plugin = JavaPlugin.getPlugin(MinecraftRoyale.class);
    @EventHandler
    public void onPvp(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player && !plugin.getConfig().getBoolean("state.isInProgress")){
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLogin(PlayerLoginEvent event) {
        MinecraftRoyale.appender.logLine("Player " + event.getPlayer().getDisplayName() + " logged in with UUID " + event.getPlayer().getUniqueId());
        getLogger().info(event.getPlayer().getDisplayName() + " logged in");
        Object obj = plugin.getConfig().get("state.playerData." + event.getPlayer().getUniqueId() + ".regenHealth");
        plugin.getLogger().info("found player's regen health: " + obj);


        new BukkitRunnable() {
            @Override
            public void run() {
                List inventoriesToClear = plugin.getConfig().getStringList("state.inventoriesToClear");
                if(inventoriesToClear.contains(event.getPlayer().getUniqueId().toString())){
                    plugin.getLogger().info("clearing the inventory of " + event.getPlayer().getDisplayName());
                    inventoriesToClear.remove(event.getPlayer().getUniqueId());
                    plugin.getConfig().set("state.inventoriesToClear", inventoriesToClear);
                    ClearInventory.clearInventory(event.getPlayer());
                }

                if(plugin.royaleWorlds.manager != null) plugin.royaleWorlds.manager.addPlayer(event.getPlayer());


                if(plugin.getConfig().getBoolean("state.isInProgress") && plugin.getConfig().getBoolean("state.playerData." + event.getPlayer().getUniqueId().toString() + ".isDead")){
                    plugin.getLogger().info("kicking a player that is not already dead");
                    event.getPlayer().kickPlayer("You already died this round. See the discord server for info on the next round.");
                }else{
                    event.getPlayer().spigot().respawn();
                    if(!plugin.getConfig().getBoolean("state.playerData." + event.getPlayer().getUniqueId() + ".hasJoined")){
                        event.getPlayer().teleport(RoyaleWorlds.getRandomLocation(MinecraftRoyale.getCurrentWorld()));
                        plugin.getConfig().set("state.playerData." + event.getPlayer().getUniqueId() + ".hasJoined", true);
                    }
                }
            }
        }.runTaskLater(plugin, 3);

        if(obj != null && (Boolean) obj){
            new BukkitRunnable() {
                @Override
                public void run() {
                    MinecraftRoyale.boostPlayerHealth(event.getPlayer());

//                    setScoreBoard(event.getPlayer());
                }
            }.runTaskLater(plugin, 3);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            MinecraftRoyale.appender.playerDeath(victim.getDisplayName(), victim.getUniqueId().toString(), event.getDeathMessage());
        }else{
            MinecraftRoyale.appender.playerKill(victim.getDisplayName(), victim.getUniqueId().toString(), killer.getDisplayName(), killer.getUniqueId().toString(), event.getDeathMessage());

            String path = "state.playerData." + killer.getUniqueId() + ".points";
            plugin.getLogger().info("had points: " + plugin.getConfig().getInt(path));
            int oldPoints = plugin.getConfig().getInt(path);
            int newPoints = oldPoints + plugin.getConfig().getInt("gameSettings.points.normal");
            MinecraftRoyale.appender.pointChange(victim.getDisplayName(), victim.getUniqueId().toString(), oldPoints, newPoints, event.getDeathMessage());

            // TODO: add points based on who has the most

            plugin.getConfig().set(path, newPoints);
        }

        plugin.getConfig().set("state.playerData." + victim.getUniqueId().toString() + ".isDead", true);
        plugin.saveConfig();
        new BukkitRunnable() {
            @Override
            public void run() {
                victim.kickPlayer(event.getDeathMessage());
            }
        }.runTaskLater(plugin, 2);
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
        moneyCounter.setPrefix(ChatColor.GREEN + "$" + plugin.getConfig().getInt("state.playerData." + player.getUniqueId() + ".points"));
        obj.getScore(ChatColor.RED + "" + ChatColor.WHITE).setScore(12);
        player.setScoreboard(board);


    }

//    PlayerEvent
}
