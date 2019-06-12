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
import org.bukkit.scoreboard.*;
import tk.minecraftroyale.Exceptions.ConfigException;
import tk.minecraftroyale.Listeners.DeathListener;
import tk.minecraftroyale.Listeners.PlayerLoginListener;
import tk.minecraftroyale.Loot.Airdrop;
import tk.minecraftroyale.Scheduler.Time;
import tk.minecraftroyale.WorldStuff.RoyaleWorlds;
import tk.minecraftroyale.WorldStuff.WorldCommandExecutor;
import tk.minecraftroyale.game.Round;

import java.io.IOException;
import java.util.*;
import javax.annotation.Nonnull;


public class MinecraftRoyale extends JavaPlugin {

    public RoyaleWorlds royaleWorlds;
    public static Round currentRound;
    public static final JSONFileAppender appender = new JSONFileAppender();

    public static void boostPlayerHealth(Player player){
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double amount = JavaPlugin.getPlugin(MinecraftRoyale.class).getConfig().getInt("gameSettings.healthRegen");
        double actualValue = Math.min(player.getHealth() + amount, maxHealth);
        String str = "Your health has been boosted by " + ((actualValue - player.getHealth()) / 2) + " hearts for logging in today!";

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
            }.runTaskTimer(this, ticks - ticks, 24 * 60 * 60 * 20);
        } else {
            throw new RuntimeException("time machine broke");
        }
    }

    public void updateScoreBoard(Player player) {


    }

    public static Comparator<World> ageComparator = new Comparator<World>() {
        @Override
        public int compare(World w1, World w2) {
            int w1Name = 0;
            try{
                w1Name = Integer.parseInt(w1.getName().substring(5));
            }catch(NumberFormatException e){}
            int w2Name = 0;
            try{
                w2Name = Integer.parseInt(w2.getName().substring(5));
            }catch(NumberFormatException e){}
            return w1Name - w2Name;
        }
    };

    public static World getCurrentWorld() {
        int roundNumber = JavaPlugin.getPlugin(MinecraftRoyale.class).getConfig().getInt("gameSettings.currentRound");
        Bukkit.getLogger().info("current roundNumber " + roundNumber);
        if(roundNumber == 0 && Bukkit.getWorld("world") != null){
            return Bukkit.getWorld("world");
        }else if(roundNumber >= 1 && roundNumber <= 7 && Bukkit.getWorld("world" + roundNumber) != null){
            return Bukkit.getWorld("world" + roundNumber);
        }else{
            List<World> worlds = Bukkit.getWorlds();
            worlds.sort(ageComparator);
            World finalWorld = (World) worlds.stream().filter(w -> !w.getName().contains("nether") && !w.getName().contains("end")).toArray()[0];
            if(finalWorld != null) return finalWorld;
            return Bukkit.getWorld("world");
        }
    }

    public BukkitRunnable runner;

    @Override
    public void onEnable() {
        Bukkit.getLogger().addHandler(new LogHandler());
        this.getLogger().addHandler(new LogHandler());
        appender.logLine("Enabled!");

        saveDefaultConfig();
        royaleWorlds = new RoyaleWorlds(this);

        if(getConfig().getBoolean("gameSettings.isInProgress")) {
            World currentWorld = MinecraftRoyale.getCurrentWorld();
            currentRound = new Round(this, new Time(0, 0, 0l, this.getConfig().getLong("timeConfig.roundDuration"), 0l), currentWorld, () -> royaleWorlds.setUpWorldBorder(currentWorld, true));
            currentRound.checkStatus();
            try{if(runner != null)runner.cancel();}catch(IllegalStateException e){}
            runner = new BukkitRunnable() {
                @Override
                public void run() {
                    currentRound.autosaveStatus();
                }
            };
            runner.runTaskTimer(this, 1, 10);
        }

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Team team = board.registerNewTeam("teamname");
        team.setPrefix("prefix");
        team.setDisplayName("health") ;
        Objective healthObjective = board.registerNewObjective("health", "health", "Health");
        healthObjective.setDisplaySlot(DisplaySlot.PLAYER_LIST);

        Objective pointsObjective = board.registerNewObjective("points", "dummy", "Points");
        pointsObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        healthObjective.setRenderType(RenderType.HEARTS);

        Objective healthObjectiveBelowName = board.registerNewObjective("health2", "health", "HP");
        healthObjectiveBelowName.setDisplaySlot(DisplaySlot.BELOW_NAME);
        healthObjectiveBelowName.setDisplayName("/ 20 HP");


        for (World w : Bukkit.getWorlds()) {
            for (Player p : w.getPlayers()) {
                setDevCommands(p, false);
                updateScoreBoard(p);
            }
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                for(OfflinePlayer player : Bukkit.getOfflinePlayers()) {
//                    if(!team.hasEntry()) team.addEntry(p.getDisplayName());

                    Score score = pointsObjective.getScore(player.getName());
                    score.setScore(getConfig().getInt("playerData." + player.getUniqueId() + ".points"));

                    if(player instanceof Player && ((Player) player).getScoreboard() != board) ((Player) player).setScoreboard(board);
                }
            }

        }.runTaskTimer(this, 10, 10);


        Objects.requireNonNull(this.getCommand("loadworld")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("resetconfig")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("mrtp")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("createworld")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("setupwborder")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("addloottables")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("airdrop")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("addlootchest")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("addlootchests")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("endround")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("dopostworldgenstuff")).setExecutor(new WorldCommandExecutor(this));
        getServer().getPluginManager().registerEvents(new DeathListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerLoginListener(), this);

        this.getConfig().options().copyDefaults(true);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            getLogger().info("Saving config");
            saveConfig();
        }, 0, 20 * 30);

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

        if(!getConfig().getBoolean("hasGeneratedWorlds")){
            for(int i = 1; i <= 7; i ++) {
                World w = Bukkit.getWorld("world" + i);
                if (w == null) {
                    try {
                        w = royaleWorlds.generateWorld(i, Bukkit.getConsoleSender());
                    } catch (IOException | ConfigException e) {
                        e.printStackTrace();
                    }
                }
                if(w != null) w.getWorldBorder().setSize(getConfig().getLong("worldBorder.startDistance"));
            }
            //getConfig().set("hasGeneratedWorlds", true);
        }
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
