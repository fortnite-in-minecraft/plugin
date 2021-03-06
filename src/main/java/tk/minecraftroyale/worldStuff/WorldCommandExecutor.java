package tk.minecraftroyale.worldStuff;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import tk.minecraftroyale.ClearInventory;
import tk.minecraftroyale.MinecraftRoyale;
import tk.minecraftroyale.exceptions.ConfigException;
import tk.minecraftroyale.loot.Airdrop;
import tk.minecraftroyale.loot.LootChest;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class WorldCommandExecutor implements CommandExecutor {
    private final MinecraftRoyale minecraftRoyale;

    public WorldCommandExecutor(MinecraftRoyale minecraftRoyale) {
        this.minecraftRoyale = minecraftRoyale;
    }

    private String getPlayer(String nameOrUuid){
        String uuid = null;
        for(OfflinePlayer p : minecraftRoyale.getAllPlayers()){
            if(Objects.equals(p.getName(), nameOrUuid) || p.getUniqueId().toString().equals(nameOrUuid)){
                uuid = p.getUniqueId().toString();
            }
        }
        return uuid;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String label, @Nonnull String[] args) {

        JavaPlugin plugin = JavaPlugin.getPlugin(MinecraftRoyale.class);
        if (cmd.getName().equalsIgnoreCase("airdrop")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Error: must be run by a player");
                return true;
            }
            if (!MinecraftRoyale.getDevCommands(sender)) {
                sender.sendMessage("You do not have development commands enabled. Please use /toggledevcommands to enable them.");
                return true;
            }
            try {
                Player player = (Player) sender;
                sender.sendMessage(args.length + "");
                Airdrop drop;
                if (args.length == 0) {
                    drop = new Airdrop(((Player) sender).getWorld());
                } else if (args.length == 2) {
                    drop = new Airdrop(new Location(player.getWorld(),
                            Integer.parseInt(args[0]),
                            player.getWorld().getHighestBlockYAt(Integer.parseInt(args[0]), Integer.parseInt(args[1])),
                            Integer.parseInt(args[1])));
                } else {
                    return false;
                }

                drop.place();
                return true;
            } catch (NumberFormatException e) {
                sender.sendMessage("Error: invalid coordinates");
            }
        } else if (cmd.getName().equalsIgnoreCase("randomtp")) {
            if (!MinecraftRoyale.getDevCommands(sender)) {
                sender.sendMessage("You do not have development commands enabled. Please use /toggledevcommands to enable them.");
                return true;
            }
            if (!(sender instanceof Player)) return false;
            Player player = ((Player) sender).getPlayer();
            if(player != null) RoyaleWorlds.randomlyTeleportPlayer(player, MinecraftRoyale.getCurrentWorld());
            return true;
        } else if (cmd.getName().equalsIgnoreCase("getpoints")) {
            if (!MinecraftRoyale.getDevCommands(sender)) {
                sender.sendMessage("You do not have development commands enabled. Please use /toggledevcommands to enable them.");
                return true;
            }

            String uuid;
            if (args.length == 0 && sender instanceof Player) {
                uuid = ((Player) sender).getUniqueId().toString();
            } else if (args.length == 1) {
                uuid = getPlayer(args[0]);
                if (uuid == null) {
                    sender.sendMessage("Unknown player");
                    return false;
                }
            } else {
                return false;
            }

            sender.sendMessage(uuid + " has " + plugin.getConfig().getInt("state.playerData." + uuid + ".points") + " points");
            return true;
        } else if (cmd.getName().equalsIgnoreCase("unkill")) {
            if (!MinecraftRoyale.getDevCommands(sender)) {
                sender.sendMessage("You do not have development commands enabled. Please use /toggledevcommands to enable them.");
                return true;
            }

            String uuid;
            if (args.length == 1) {
                uuid = getPlayer(args[0]);
                if (uuid == null) {
                    sender.sendMessage("Unknown player");
                    return false;
                }
            } else {
                return false;
            }

            if (Bukkit.getPlayer(uuid) != null && Objects.requireNonNull(Bukkit.getPlayer(uuid)).isOnline()) {
                Objects.requireNonNull(Bukkit.getPlayer(uuid)).kickPlayer("Rejoin to respawn");
            }

            plugin.getConfig().set("state.playerData." + uuid + ".isDead", false);
            sender.sendMessage(uuid + " can now join the game");
            return true;
        } else if (cmd.getName().equalsIgnoreCase("setpoints")) {
            if (!MinecraftRoyale.getDevCommands(sender)) {
                sender.sendMessage("You do not have development commands enabled. Please use /toggledevcommands to enable them.");
                return true;
            }

            String uuid;
            int num;
            if (args.length == 1 && sender instanceof Player) {
                uuid = ((Player) sender).getUniqueId().toString();
                num = Integer.parseInt(args[0]);
            } else if (args.length == 2) {
                try {
                    num = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    return false;
                }
                uuid = getPlayer(args[1]);

                if (uuid == null) {
                    sender.sendMessage("Unknown player");
                    return false;
                }
            } else {
                return false;
            }
            plugin.getConfig().set("state.playerData." + uuid + ".points", num);
            sender.sendMessage("set points to " + num + " of " + uuid);
            return true;
        } else if (cmd.getName().equalsIgnoreCase("resetconfig")) {
            if (!MinecraftRoyale.getDevCommands(sender)) {
                sender.sendMessage("You do not have development commands enabled. Please use /toggledevcommands to enable them.");
                return true;
            }
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            //noinspection ResultOfMethodCallIgnored
            configFile.delete();
            plugin.saveDefaultConfig();
            plugin.reloadConfig();
            return true;
        } else if (cmd.getName().equalsIgnoreCase("dopostworldgenstuff")) {
            if (!MinecraftRoyale.getDevCommands(sender)) {
                sender.sendMessage("You do not have development commands enabled. Please use /toggledevcommands to enable them.");
                return true;
            }
            if (MinecraftRoyale.getCurrentWorld() == null) {
                return false;
            } else {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    plugin.getConfig().set("state.playerData." + player.getUniqueId().toString() + ".isDead", false);
                    plugin.getConfig().set("state.playerData." + player.getUniqueId().toString() + ".hasJoined", false);
                    ClearInventory.clearInventory(player);
                }

                int newRoundNum = plugin.getConfig().getInt("state.currentRound") + 1;
                plugin.getLogger().info("newRoundNum: " + newRoundNum);
                World w = Bukkit.getWorld("world" + newRoundNum);
                if (w == null) {
                    plugin.getLogger().warning("Could not find world: world" + newRoundNum);
                    sender.sendMessage(ChatColor.RED + "Could not find the next world! Is the world corrupted?");
                } else {
                    MinecraftRoyale.appender.startGame();
                    minecraftRoyale.royaleWorlds.doPostWorldGenStuff(w, Math.max(plugin.getConfig().getInt("state.currentRound"), 1));
                }
                return true;
            }
        } else if (cmd.getName().equalsIgnoreCase("endround")) {
            if (!MinecraftRoyale.getDevCommands(sender)) {
                sender.sendMessage("You do not have development commands enabled. Please use /toggledevcommands to enable them.");
                return true;
            }
            if (MinecraftRoyale.currentRound != null) MinecraftRoyale.currentRound.endRound();
            else plugin.getLogger().info("null!");
            return true;
        } else if (cmd.getName().equalsIgnoreCase("addlootchest")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Error: must be run by a player");
                return true;
            }

            if (!MinecraftRoyale.getDevCommands(sender)) {
                sender.sendMessage("You do not have development commands enabled. Please use /toggledevcommands to enable them.");
                return true;
            }

            try {
                if (args.length == 0) {
                    LootChest lootChest = new LootChest(((Player) sender).getWorld());
                    lootChest.place();
                    sender.sendMessage(lootChest.getCommandResponse());
                    return true;
                } else {
                    return false;
                }
            } catch (Error e) {
                e.printStackTrace();
            }
        } else if (cmd.getName().equalsIgnoreCase("addlootchests")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Error: must be run by a player");
                return true;
            }

            if (!MinecraftRoyale.getDevCommands(sender)) {
                sender.sendMessage("You do not have development commands enabled. Please use /toggledevcommands to enable them.");
                return true;
            }

            try {
                if (args.length == 1) {
                    try {
                        int num = Integer.parseInt(args[0]);
                        sender.sendMessage("adding " + num + " loot chests...");
                        for (int i = 0; i < num; i++) {
                            LootChest lootChest = new LootChest(((Player) sender).getWorld());
                            lootChest.place();
                            sender.sendMessage(lootChest.getCommandResponse());
                        }
                        return true;
                    } catch (NumberFormatException e) {
                        sender.sendMessage("Error: not a number");
                    }
                } else {
                    return false;
                }
            } catch (Error e) {
                e.printStackTrace();
            }
        } else if (cmd.getName().equalsIgnoreCase("installloottables")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Error: must be ran by a player");
                return true;
            }

            if (!MinecraftRoyale.getDevCommands(sender)) {
                sender.sendMessage("You do not have development commands enabled. Please use /toggledevcommands to enable them.");
                return true;
            }

            try {
                LootChest.installLootTables(((Player) sender).getWorld(), sender);
            } catch (IOException e) {
                e.printStackTrace();
                sender.sendMessage("Internal I/O error");
                return true;
            }
            sender.sendMessage("installed loot tables");
            return true;
        } else if (cmd.getName().equalsIgnoreCase("setupwborder")) {
            if (sender instanceof Player && !MinecraftRoyale.getDevCommands(sender)) {
                sender.sendMessage("You do not have development commands enabled. Please use /toggledevcommands to enable them.");
                return true;
            }

            try {
                if (args.length > 0) {
                    minecraftRoyale.royaleWorlds.setUpWorldBorder(Integer.parseInt(args[0]));
                } else {
                    if (sender instanceof Player) {
                        minecraftRoyale.royaleWorlds.setUpWorldBorder(((Player) sender).getWorld());
                    } else {
                        return false;
                    }
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("Error: not a number");
            } catch (IllegalArgumentException e) {
                sender.sendMessage("Error: not found");
            }
            return true;
        } else if (cmd.getName().equalsIgnoreCase("loadworld")) {
            if (args.length != 1) return false;
            else if (sender instanceof Player) {
                if (!MinecraftRoyale.getDevCommands(sender)) {
                    sender.sendMessage("You do not have development commands enabled. Please use /toggledevcommands to enable them.");
                    return true;
                }
            }

            try {
                World w = minecraftRoyale.royaleWorlds.getWorld(Integer.parseInt(args[0]));
                if (w == null) {
                    sender.sendMessage("Error: A world for that round does not exist. You must first create it with /createworld.");
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("Error: not a number");
            }
        } else if (cmd.getName().equalsIgnoreCase("mrtp")) {

            if (!MinecraftRoyale.getDevCommands(sender)) {
                sender.sendMessage("You do not have development commands enabled. Please use /toggledevcommands to enable them.");
                return true;
            }

            if (args.length != 1) return false;
            else if (!(sender instanceof Player)) {
                sender.sendMessage("Error: must be a player");
                return true;
            }

            Location worldSpawn;

            try {
                int worldNum = Integer.parseInt(args[0]);

                if (worldNum < 1 || worldNum > plugin.getConfig().getInt("gameSettings.numWorlds"))
                    throw new NumberFormatException(); // Transfers control to the catch block

                World world = minecraftRoyale.royaleWorlds.getWorld(worldNum);

                if (world == null) {
                    sender.sendMessage("Error: A world for that round does not exist. You must first create it with /createworld.");
                    return true;
                }

                worldSpawn = world.getSpawnLocation();

            } catch (NumberFormatException e) {
                World w = Bukkit.getWorld(args[0]);
                if (w == null) {
                    sender.sendMessage("Error: world \"" + args[0] + "\" not found");
                    return true;
                }

                worldSpawn = w.getSpawnLocation();
            }

            ((Player) sender).teleport(worldSpawn);
            return true;
        } else if (cmd.getName().equalsIgnoreCase("createworld")) {
            if (args.length != 1) return false;

            if (sender instanceof Player && !MinecraftRoyale.getDevCommands(sender)) {
                sender.sendMessage(ChatColor.RED + "You do not have development commands enabled. Please use /toggledevcommands to enable them.");
                return true;
            }

            try {
                int worldNum = Integer.parseInt(args[0]);
                if (worldNum < 1 || worldNum > plugin.getConfig().getInt("gameSettings.numWorlds")) throw new NumberFormatException();

                try {
                    minecraftRoyale.royaleWorlds.generateWorld(worldNum, sender);
                    return true;
                } catch (ConfigException e) {
                    sender.sendMessage(e.getMessage());
                    minecraftRoyale.getLogger().severe(e.getMessage());
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("Error: invalid round");
            }
        } else if (cmd.getName().equalsIgnoreCase("getwborder")) {
            sender.sendMessage(ChatColor.GREEN + "The world border is " + ((int) MinecraftRoyale.getCurrentWorld().getWorldBorder().getSize()) + " blocks wide");
            return true;
        }
        return false;
    }
}
