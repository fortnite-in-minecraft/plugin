package tk.minecraftroyale;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import tk.minecraftroyale.Listeners.DeathListener;
import tk.minecraftroyale.Loot.Airdrop;
import tk.minecraftroyale.Scheduler.DispatchSaveConfig;
import tk.minecraftroyale.WorldStuff.RoyaleWorlds;
import tk.minecraftroyale.WorldStuff.WorldCommandExecutor;
import tk.minecraftroyale.Scheduler.DispatchGameEnd;

import javax.security.auth.login.LoginException;
import java.time.chrono.MinguoChronology;
import java.util.*;
import java.util.logging.Logger;
import javax.annotation.Nonnull;


public class MinecraftRoyale extends JavaPlugin {

    public RoyaleWorlds royaleWorlds;

    public static void boostPlayerHealth(Player player){
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double amount = JavaPlugin.getPlugin(MinecraftRoyale.class).getConfig().getInt("gameSettings.healthRegen");
        double actualValue = Math.min(player.getHealth() + amount, maxHealth);
        String str = "Your health has been boosted by " + (actualValue - player.getHealth() / 2) + " hearts for logging in today!";

        JavaPlugin.getPlugin(MinecraftRoyale.class).getLogger().info("updating health for player " + player.getUniqueId() + "\n" + player.getHealth());
        player.sendMessage(str);
        player.setHealth(actualValue);
        JavaPlugin.getPlugin(MinecraftRoyale.class).getConfig().set("playerData." + player.getUniqueId() + ".regenHealth", false);
    }

    private void setupMidnight(){
        Calendar now = new GregorianCalendar();
        Calendar next = new GregorianCalendar();

// Set the next execution time
        next.set(Calendar.MILLISECOND, 0);
        next.set(Calendar.SECOND, 0);

        next.set(Calendar.HOUR_OF_DAY, 12 + 5); // 4:00 AM
        next.set(Calendar.MINUTE, 17);
        next.set(Calendar.SECOND, 35);

// If it's after 4:00 AM already we need to set the next execution to the next day
        if (now.after(next)) {
            next.add(Calendar.DATE, 1); // Add a day
        }

// This should always be true
        if (now.before(next)) {
            // Ticks until execution
            long ticks = (next.getTimeInMillis() - now.getTimeInMillis()) / 1000 * 20;
            System.out.println("updating in " + ticks + " ticks");
            // Now schedule the runnable
            new BukkitRunnable() {
                @Override
                public void run() {
                    for(OfflinePlayer possiblyOfflinePlayer : Bukkit.getOfflinePlayers()){
                        System.out.println("got offline player " + possiblyOfflinePlayer.getUniqueId() );
                        JavaPlugin.getPlugin(MinecraftRoyale.class).getConfig().set("playerData." + possiblyOfflinePlayer.getUniqueId() + ".regenHealth", true);
                        JavaPlugin.getPlugin(MinecraftRoyale.class).saveConfig();
                        if(possiblyOfflinePlayer.isOnline() && possiblyOfflinePlayer instanceof Player){
                            Player player = (Player) possiblyOfflinePlayer;
                            boostPlayerHealth(player);
                        }
                    }
                }
            }.runTaskTimer(this, ticks - ticks, 24 * 60 * 60 * 20); // And repeat again in 24h
        } else {
            throw new RuntimeException("time machine broke");
        }
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        royaleWorlds = new RoyaleWorlds(this);

        for (World w : Bukkit.getWorlds()) {
            for (Player p : w.getPlayers()) {
                setDevCommands(p, false);
            }
        }

        Objects.requireNonNull(this.getCommand("loadworld")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("resetconfig")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("mrtp")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("createworld")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("setupwborder")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("addloottables")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("airdrop")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("addlootchest")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("addlootchests")).setExecutor(new WorldCommandExecutor(this));
        getServer().getPluginManager().registerEvents(new DeathListener(), this);

        DispatchGameEnd.dispatchGameEnd();
        DispatchSaveConfig.dispatchSaveConfig();
        this.getConfig().options().copyDefaults(true);


            // Create the task anonymously and schedule to run it once, after 20 ticks
        new BukkitRunnable() {

            @Override
            public void run() {
                // What you want to schedule goes here
                JavaPlugin.getPlugin(MinecraftRoyale.class).getLogger().info("Checking airdrop...");
                Object[] players = Bukkit.getOnlinePlayers().toArray();
                if(players.length > 0) Airdrop.runAirdrop(((Player) (players[0])).getWorld());
            }

        }.runTaskTimer(this, 0, 20 * 60 * 5);

        setupMidnight();
    }

    @Override
    public void onDisable() {}

    private void setDevCommands(Player player, boolean state) {
        player.setMetadata("devCommandsEnabled", new FixedMetadataValue(this, state));

        if (state) {
            player.sendMessage("Development commands enabled.");
        } else {
            player.sendMessage("Development commands disabled.");
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean getDevCommands(Player player) {
        List<MetadataValue> vals = player.getMetadata("devCommandsEnabled");
        for (MetadataValue val : vals) {
            if (val.getOwningPlugin() == this) {
                return val.asBoolean();
            }
        }
        return false;
    }


    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String label, @Nonnull String[] args) {
        if (cmd.getName().equalsIgnoreCase("toggledevcommands")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Error: must be a player");
                return true;
            }

            setDevCommands((Player) sender, !getDevCommands((Player) sender));
            return true;
        }

        return false;
    }

}
