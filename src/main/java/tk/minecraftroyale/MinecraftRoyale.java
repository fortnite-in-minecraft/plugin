package tk.minecraftroyale;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import tk.minecraftroyale.exceptions.ConfigException;
import tk.minecraftroyale.game.Round;
import tk.minecraftroyale.listeners.DeathListener;
import tk.minecraftroyale.listeners.PlayerLoginListener;
import tk.minecraftroyale.loot.Airdrop;
import tk.minecraftroyale.loot.LootChest;
import tk.minecraftroyale.worldStuff.RoyaleWorlds;
import tk.minecraftroyale.worldStuff.WorldCommandExecutor;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class MinecraftRoyale extends JavaPlugin {

    public RoyaleWorlds royaleWorlds;
    public static Round currentRound;
    private LogHandler handler;
    public static final JSONFileAppender appender = new JSONFileAppender();

    public static void boostPlayerHealth(Player player){
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        double maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();
        double amount = JavaPlugin.getPlugin(MinecraftRoyale.class).getConfig().getInt("gameSettings.healthRegen");
        double actualValue = Math.min(player.getHealth() + amount, maxHealth);
        String str = "Your health has been boosted by " + ((actualValue - player.getHealth()) / 2) + " hearts for logging in today!";

        JavaPlugin.getPlugin(MinecraftRoyale.class).getLogger().info("updating health for player " + player.getUniqueId() + "\n" + player.getHealth());
        player.sendMessage(str);
        player.setHealth(actualValue);
        JavaPlugin.getPlugin(MinecraftRoyale.class).getConfig().set("state.playerData." + player.getUniqueId() + ".regenHealth", false);
    }

    private void setupMidnight(){
        Calendar now = new GregorianCalendar();
        Calendar next = new GregorianCalendar();

// Set the next execution time
        next.set(Calendar.MILLISECOND, 0);
        next.set(Calendar.SECOND, 0);

        next.set(Calendar.HOUR_OF_DAY, 0);
        next.set(Calendar.MINUTE, 0);
        next.set(Calendar.SECOND, 5);

// If it's after 12:00 AM already we need to set the next execution to the next day
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
                    for(OfflinePlayer possiblyOfflinePlayer : getAllPlayers()){
                        System.out.println("got offline player " + possiblyOfflinePlayer.getUniqueId() );
                        JavaPlugin.getPlugin(MinecraftRoyale.class).getConfig().set("state.playerData." + possiblyOfflinePlayer.getUniqueId() + ".regenHealth", true);
                        JavaPlugin.getPlugin(MinecraftRoyale.class).saveConfig();
                        if(possiblyOfflinePlayer.isOnline() && possiblyOfflinePlayer instanceof Player){
                            Player player = (Player) possiblyOfflinePlayer;
                            boostPlayerHealth(player);
                        }
                    }
                }
            }.runTaskTimer(this, ticks, 24 * 60 * 60 * 20);
        } else {
            throw new RuntimeException("time machine broke");
        }
    }

    private void updateScoreBoard() {


    }

    private static Comparator<World> ageComparator = (w1, w2) -> {
        int w1Name = 0;
        try{
            w1Name = Integer.parseInt(w1.getName().substring(5));
        }catch(NumberFormatException ignored){}
        int w2Name = 0;
        try{
            w2Name = Integer.parseInt(w2.getName().substring(5));
        }catch(NumberFormatException ignored){}
        return w1Name - w2Name;
    };

    public static World getCurrentWorld() {
        int roundNumber = JavaPlugin.getPlugin(MinecraftRoyale.class).getConfig().getInt("state.currentRound");
//        Bukkit.getLogger().info("current roundNumber " + roundNumber);
        if(roundNumber == 0 && Bukkit.getWorld("world") != null){
            return Bukkit.getWorld("world");
        }else if(roundNumber >= 1 && roundNumber <= JavaPlugin.getPlugin(MinecraftRoyale.class).getConfig().getInt("gameSettings.numWorlds") && Bukkit.getWorld("world" + roundNumber) != null){
            return Bukkit.getWorld("world" + roundNumber);
        }else{
            Bukkit.getLogger().warning("Could not find round-based world");
            List<World> worlds = Bukkit.getWorlds();
            worlds.sort(ageComparator);
            World finalWorld = (World) worlds.stream().filter(w -> !w.getName().contains("nether") && !w.getName().contains("end")).toArray()[0];
            if(finalWorld != null) return finalWorld;
            return Bukkit.getWorld("world");
        }
    }

    public List<OfflinePlayer> getAllPlayers(){
        List<OfflinePlayer> list = new ArrayList<>();
        for(OfflinePlayer p : Bukkit.getOfflinePlayers()){
            Object theThing = this.getConfig().get("state.playerData." + p.getUniqueId().toString() + ".hasJoined");
            if(theThing != null){
//                this.getLogger().info("getAllPlayers: " + p.getName());
                list.add(p);
            }
        }
        return list;
    }

    public BukkitRunnable runner;

    public static long getGameSetting(String settingPath, String worldName){
        MinecraftRoyale plugin = getPlugin(MinecraftRoyale.class);
        if(plugin.getConfig().get("worlds." + worldName + ".gameSettings." + settingPath) != null){
//            plugin.getLogger().info(worldName + ": using world-specific settings for " + settingPath + " = " + plugin.getConfig().getLong("worlds." + worldName + ".gameSettings." + settingPath));
            return plugin.getConfig().getLong("worlds." + worldName + ".gameSettings." + settingPath);
        }
//        plugin.getLogger().info(worldName + ": using global settings for " + settingPath + " = " + plugin.getConfig().getLong("gameSettings." + settingPath));
        return plugin.getConfig().getLong("gameSettings." + settingPath);
    }

    @Override
    public void onEnable() {
        //            LootChest.installLootTables(Bukkit.getWorld("world"), null);
        Bukkit.dispatchCommand(getServer().getConsoleSender(), "minecraft:reload");
        //Bukkit.getLogger().addHandler(new LogHandler());
        //this.getLogger().getParent().addHandler(new LogHandler());
        handler = new LogHandler();
        handler.start();
        appender.logLine("Enabled!");

        saveDefaultConfig();
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
                updateScoreBoard();
            }
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                for(OfflinePlayer player : getAllPlayers()) {
//                    if(!team.hasEntry()) team.addEntry(p.getDisplayName());

                    Score score = pointsObjective.getScore(Objects.requireNonNull(player.getName()));
                    score.setScore(getConfig().getInt("state.playerData." + player.getUniqueId() + ".points"));

                    if(player instanceof Player && ((Player) player).getScoreboard() != board) ((Player) player).setScoreboard(board);
                }
            }

        }.runTaskTimer(this, 10, 10);

        // TODO Rewrite these with the new command system
        Objects.requireNonNull(this.getCommand("loadworld")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("resetconfig")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("mrtp")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("createworld")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("setupwborder")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("addloottables")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("addlootchest")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("addlootchests")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("airdrop")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("endround")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("randomtp")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("dopostworldgenstuff")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("setpoints")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("getpoints")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("unkill")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("getwborder")).setExecutor(new WorldCommandExecutor(this));

        // Set the command executor for all commands that have been implemented with the new system
        //for (String commandName : DynamicCommandExecutor.getInstance().getRegisteredCommandNames()) {
        //    Objects.requireNonNull(getCommand(commandName)).setExecutor(/--
        //    +DynamicCommandExecutor.getInstance());
        //}

        getServer().getPluginManager().registerEvents(new DeathListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerLoginListener(), this);

        this.getConfig().options().copyDefaults(true);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            getLogger().info("Saving config");
            saveConfig();
        }, 0, 20 * 30);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            JavaPlugin.getPlugin(MinecraftRoyale.class).getLogger().info("Checking airdrop...");
            Object[] players = Bukkit.getOnlinePlayers().toArray();
            if(players.length > 0)
                Airdrop.runAirdrop(((Player) (players[0])).getWorld());
        }, 0, 20 * 60 * 5);

        setupMidnight();



        new BukkitRunnable() {
            @Override
            public void run() {
                royaleWorlds = new RoyaleWorlds();

                MinecraftRoyale plugin = JavaPlugin.getPlugin(MinecraftRoyale.class);

                plugin.getLogger().info("Checking setting up round...");
                if(plugin.getConfig().getBoolean("state.isInProgress")) {
                    plugin.getLogger().info("Setting up round...");
                    World currentWorld = MinecraftRoyale.getCurrentWorld();
                    currentRound = new Round(plugin, currentWorld);
                    currentRound.checkStatus();
                    try{
                        if(runner != null){
                            plugin.getLogger().info("Cancelling runner...");
                            runner.cancel();
                        }
                    }catch(IllegalStateException e){
                        plugin.getLogger().info("Couldn't cancel runner... " + e.toString());
                    }
                    plugin.getLogger().info("Starting autosave timer #2");
                    runner = new BukkitRunnable() {
                        @Override
                        public void run() {
                            currentRound.autosaveStatus();
                        }
                    };
                    runner.runTaskTimer(plugin, 1, 20);
                }

                if(!getConfig().getBoolean("hasGeneratedWorlds")){
                    getLogger().info("Generating worlds");
                    for(int i = 1; i <= plugin.getConfig().getInt("gameSettings.numWorlds"); i ++) {
                        World w = Bukkit.getWorld("world" + i);
                        if (w == null){
                            if(Files.isDirectory(Paths.get(Bukkit.getWorldContainer().getPath(), "world" + i))){

                                getLogger().info("Loading world " + i);
                                // the world needs to be loaded but is still there
                                w = Bukkit.createWorld(new WorldCreator("world" + i));
                            }else{
                                // the world is absent entirely
                                try {
                                    getLogger().info("Creating world " + i);


                                    w = royaleWorlds.generateWorld(i, Bukkit.getConsoleSender());
                                    int num = plugin.getConfig().getInt("gameSettings.numLootChests");
                                    Bukkit.getLogger().info("adding " + plugin.getConfig().getInt("gameSettings.numLootChests") + " loot chests...");
                                    for (int i2 = 0; i2 < num; i2++) {
                                        getLogger().info("Spawning a new loot chest");
                                        LootChest lootChest = new LootChest(w);
                                        lootChest.place();
                                        Bukkit.getLogger().info(lootChest.getCommandResponse());
                                    }
                                } catch (ConfigException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if(w != null){
                            long size = getGameSetting("worldBorder.startDistance", "world" + i);
                            plugin.getLogger().info("for world " + i + " the wborder's size is " + w.getWorldBorder().getSize() + " compared to a start distance of " + size);
                            if(w.getWorldBorder().getSize() < size) w.getWorldBorder().setSize(size);
                        }
                    }
                    getLogger().info("Done generating worlds");
//                    getConfig().set("hasGeneratedWorlds", true);
                    saveConfig();
                }

            }
        }.runTaskLater(this, 2);

    }

    @Override
    public void onDisable() {
        if(handler != null) handler.interrupt();
        if(royaleWorlds != null && royaleWorlds.manager != null) royaleWorlds.manager.deleteBar();
        if (royaleWorlds != null) {
            royaleWorlds.manager = null;
        }
        for(Player p : Bukkit.getOnlinePlayers()){
            p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

        }
    }

    public static void setDevCommands(Player player, boolean state) {
        player.setMetadata("devCommandsEnabled", new FixedMetadataValue(JavaPlugin.getPlugin(MinecraftRoyale.class), state));

        if (state) {
            player.sendMessage("Development commands enabled.");
        } else {
            player.sendMessage("Development commands disabled.");
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean getDevCommands(CommandSender sender) {
        // Console should always have access to dev commands
        if (!(sender instanceof Player)) return true;

        if(sender.isOp()) return true;

        List<MetadataValue> metadataValues = ((Player) sender).getMetadata("devCommandsEnabled");
        for (MetadataValue metadataValue : metadataValues) {
            if (metadataValue.getOwningPlugin() == JavaPlugin.getPlugin(MinecraftRoyale.class)) {
                return metadataValue.asBoolean();
            }
        }
        return false;
    }
}
