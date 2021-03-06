package tk.minecraftroyale.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import tk.minecraftroyale.ClearInventory;
import tk.minecraftroyale.MinecraftRoyale;
import tk.minecraftroyale.worldStuff.RoyaleWorlds;

import java.util.List;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;

@SuppressWarnings("unused")
public final class DeathListener implements Listener {
    static final private MinecraftRoyale plugin = JavaPlugin.getPlugin(MinecraftRoyale.class);
    @EventHandler
    public void onPvp(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player && !plugin.getConfig().getBoolean("state.isInProgress")){
            event.setCancelled(true);
        }
    }

    private void addPoints(Player player, int points, String reason){
        int oldPoints = plugin.getConfig().getInt("state.playerData." + player.getUniqueId() + ".points");
        plugin.getConfig().set("state.playerData." + player.getUniqueId() + ".points", oldPoints + points);
        MinecraftRoyale.appender.pointChange(player.getDisplayName(), player.getUniqueId().toString(), oldPoints, oldPoints + points, reason);
        player.sendMessage(ChatColor.GREEN + "You got " + (points) + " points because " + reason.replaceAll(player.getDisplayName(), "you"));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        if(event.getBlock().getState() instanceof Chest){
            Chest block = (Chest) event.getBlock().getState();
            if(block.getLootTable() != null){
                String key = block.getLootTable().getKey().toString();
                Player player = event.getPlayer();
                if(key.equals("minecraftroyale:loot_chest")){
                    int pointsToAdd = plugin.getConfig().getInt("gameSettings.points.lootChestOpen");
                    addPoints(player, pointsToAdd, player.getDisplayName() + " opened a loot chest");
                }else if(key.equals("minecraftroyale:airdrop")){
                    int pointsToAdd = plugin.getConfig().getInt("gameSettings.points.airdropOpen");
                    addPoints(player, pointsToAdd, player.getDisplayName() + " opened an airdrop");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChestOpen(InventoryOpenEvent event){
        if(event.getInventory().getHolder() instanceof Chest){
            Chest block = (Chest) event.getInventory().getHolder();
            if((System.currentTimeMillis() - block.getLastFilled()) < 5){
                Player player = (Player)  event.getPlayer();
//                int pointsToAdd = plugin.getConfig().getInt("gameSettings.points.lootChestOpen");
//                addPoints(player, pointsToAdd, player.getDisplayName() + " opened a loot chest");
                player.sendMessage(ChatColor.RED + "Loot chests and airdrops must be broken instead of opened to receive points.");
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLogin(PlayerLoginEvent event) {
        MinecraftRoyale.appender.logLine("Player " + event.getPlayer().getDisplayName() + " logged in with UUID " + event.getPlayer().getUniqueId());
        getLogger().info(event.getPlayer().getDisplayName() + " logged in");
        Object obj = plugin.getConfig().get("state.playerData." + event.getPlayer().getUniqueId() + ".regenHealth");
        plugin.getLogger().info("found player's regen health: " + obj);

        plugin.getConfig().get("state.playerData." + event.getPlayer().getUniqueId().toString() + ".name", event.getPlayer().getDisplayName());

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

                if(plugin.royaleWorlds != null && plugin.royaleWorlds.manager != null) {
                    plugin.royaleWorlds.manager.addPlayer(event.getPlayer());
                }

                boolean isInProgress = plugin.getConfig().getBoolean("state.isInProgress");
                boolean hasJoined = plugin.getConfig().getBoolean("state.playerData." + event.getPlayer().getUniqueId() + ".hasJoined");
                boolean isDead = plugin.getConfig().getBoolean("state.playerData." + event.getPlayer().getUniqueId().toString() + ".isDead");

                if(event.getPlayer().isDead()) event.getPlayer().spigot().respawn();

                if(isInProgress){
                    if(isDead){
                        plugin.getLogger().info(event.getPlayer().getDisplayName() + " is already dead");
                        event.getPlayer().kickPlayer("You already died this round. See the discord server for info on the next round.");
                    }else if(!hasJoined || !event.getPlayer().getWorld().getName().equals(MinecraftRoyale.getCurrentWorld().getName())){
                        plugin.getLogger().info("Randomly teleporting " + event.getPlayer().getDisplayName());
                        RoyaleWorlds.randomlyTeleportPlayer(event.getPlayer(), MinecraftRoyale.getCurrentWorld());
                        plugin.getConfig().set("state.playerData." + event.getPlayer().getUniqueId() + ".hasJoined", true);
                    }else{
                        plugin.getLogger().info(event.getPlayer().getDisplayName() + " is not dead");
                    }
                }else{
                    plugin.getLogger().info("0, 0 ing " + event.getPlayer().getDisplayName());
                    event.getPlayer().teleport(getServer().getWorlds().get(0).getHighestBlockAt(0, 0).getLocation());
                    ClearInventory.clearInventory(event.getPlayer());
                }
            }
        }.runTaskLater(plugin, 3);

        if(obj != null && (Boolean) obj){
            new BukkitRunnable() {
                @Override
                public void run() {
                    MinecraftRoyale.boostPlayerHealth(event.getPlayer());
                    //TODO: what???
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
            int normalPoints = plugin.getConfig().getInt("gameSettings.points.normal");
            int bonusPoints = 0;
            int potentialBonusPointsForKillingSomeoneWhoHasMorePoints = plugin.getConfig().getInt("gameSettings.points.bonus.killingSomeoneWhoHasMorePoints");

            // add points for killing someone who has more points than you
            if(oldPoints < plugin.getConfig().getInt("state.playerData." + victim.getUniqueId() + ".points")){
                bonusPoints += potentialBonusPointsForKillingSomeoneWhoHasMorePoints;
            }

            addPoints(killer, normalPoints + bonusPoints, event.getDeathMessage());
        }

        plugin.getConfig().set("state.playerData." + victim.getUniqueId().toString() + ".isDead", true);

        String path = "state.playerData." + victim.getUniqueId() + ".points";
        plugin.getLogger().info("had points: " + plugin.getConfig().getInt(path));
        int oldPoints = plugin.getConfig().getInt(path);
        int deathPenalty = plugin.getConfig().getInt("gameSettings.points.deathPenalty");

        addPoints(victim, -deathPenalty, event.getDeathMessage());

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
