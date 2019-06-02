package tk.minecraftroyale.WorldStuff;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tk.minecraftroyale.Exceptions.ConfigException;
import tk.minecraftroyale.Loot.AddALootChest;
import tk.minecraftroyale.Loot.Airdrop;
import tk.minecraftroyale.Loot.InstallLootTables;
import tk.minecraftroyale.MinecraftRoyale;

import javax.annotation.Nonnull;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

public class WorldCommandExecutor implements CommandExecutor {
    private final MinecraftRoyale minecraftRoyale;

    public WorldCommandExecutor(MinecraftRoyale minecraftRoyale) {
        this.minecraftRoyale = minecraftRoyale;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String label, @Nonnull String[] args) {
        if (cmd.getName().equalsIgnoreCase("airdrop")) {
            if(!(sender instanceof Player)){
                sender.sendMessage("Error: must be run by a player");
                return true;
            }
            try {
                if (args.length == 0) {
                    Airdrop.airdrop(((Player) sender).getWorld());
                    return true;
                } else if (args.length == 2) {
                    Airdrop.airdrop(((Player) sender).getWorld(), Integer.parseInt(args[0]), Integer.parseInt(args[1]));
                    return true;
                }else{
                    return false;
                }
            }catch(Error e){
                e.printStackTrace();
            }
        }else if (cmd.getName().equalsIgnoreCase("addlootchest")) {
            if(!(sender instanceof Player)){
                sender.sendMessage("Error: must be run by a player");
                return true;
            }
            try {
                if (args.length == 0) {
                    int[] results = AddALootChest.addALootChest(((Player) sender).getWorld());
                    sender.sendMessage("added loot chest at " + results[0] + ", " + results[1]);
                    return true;
                }else{
                    return false;
                }
            }catch(Error e){
                e.printStackTrace();
            }
        }else if (cmd.getName().equalsIgnoreCase("addlootchests")) {
            if(!(sender instanceof Player)){
                sender.sendMessage("Error: must be run by a player");
                return true;
            }
            try {
                if (args.length == 1) {
                    try {
                        int num = Integer.parseInt(args[0]);
                        sender.sendMessage("adding " + num + " loot chests...");
                        for(int i = 0 ; i < num ; i++) {
                            int[] results = AddALootChest.addALootChest(((Player) sender).getWorld());
                            sender.sendMessage("added loot chest at " + results[0] + ", " + results[1]);
                        }
                        return true;
                    } catch (NumberFormatException e) {
                        sender.sendMessage("Error: not a number");
                    }
                }else{
                    return false;
                }
            }catch(Error e){
                e.printStackTrace();
            }
        }else if (cmd.getName().equalsIgnoreCase("addloottables")) {
             if(!(sender instanceof Player)){
                 sender.sendMessage("Error: must be ran by a player");
                 return true;
             }
             try {
                 (new InstallLootTables()).installLootTables(((Player) sender).getWorld(), sender);
             }catch(IOException e){
                 e.printStackTrace();
                 sender.sendMessage("Internal I/O error");
                 return true;
             }
             sender.sendMessage("added loot tables");
             return true;
        }else if(cmd.getName().equalsIgnoreCase("setupwborder")) {
            try {
                minecraftRoyale.royaleWorlds.setUpWorldBorder(Integer.parseInt(args[0]));
            } catch (NumberFormatException e) {
                sender.sendMessage("Error: not a number");
            } catch(IllegalArgumentException e){
                sender.sendMessage("Error: not found");
            }
            return true;
        }else if (cmd.getName().equalsIgnoreCase("loadworld")) {
            if (args.length != 1) return false;
            else if (sender instanceof Player) {
                if (!minecraftRoyale.getDevCommands((Player) sender)) {
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
            if (args.length != 1) return false;
            else if (!(sender instanceof Player)) {
                sender.sendMessage("Error: must be a player");
                return true;
            }

            Location worldSpawn;

            try {
                int worldNum = Integer.parseInt(args[0]);

                if (worldNum < 1 || worldNum > 7) throw new NumberFormatException(); // Transfers control to the catch block

                World world = minecraftRoyale.royaleWorlds.getWorld(worldNum);

                if (world == null) {
                    sender.sendMessage("Error: A world for that round does not exist. You must first create it with /createworld.");
                    return true;
                }

                worldSpawn = world.getSpawnLocation();

            } catch (NumberFormatException e) {
                World w = Bukkit.getWorld(args[0]);
                if (w == null) {
                    sender.sendMessage("Error: world \"" + args[0] +  "\" not found");
                    return true;
                }

                worldSpawn = w.getSpawnLocation();
            }

            ((Player) sender).teleport(worldSpawn);
            return true;
        } else if (cmd.getName().equalsIgnoreCase("createworld")) {
            if (args.length != 1) return false;

            String worldName;

            try {
                int worldNum = Integer.parseInt(args[0]);
                if (worldNum < 1 || worldNum > 7) throw new NumberFormatException();

                try {
                    minecraftRoyale.royaleWorlds.generateWorld(worldNum, sender);
                } catch (IllegalArgumentException e) {
                    sender.sendMessage("Something went wrong. This is a bug.");
                    minecraftRoyale.getLogger().severe(e.getStackTrace().toString());
                } catch (FileNotFoundException e) {
                    sender.sendMessage(e.getMessage());
                    minecraftRoyale.getLogger().severe(e.getMessage());
                } catch (ConfigException e) {
                    sender.sendMessage("Error: invalid config option at path: " + e.getPath());
                    minecraftRoyale.getLogger().severe("invalid config option at path: " + e.getPath());
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("Error: invalid round");
            }
        }

        return false;
    }
}
