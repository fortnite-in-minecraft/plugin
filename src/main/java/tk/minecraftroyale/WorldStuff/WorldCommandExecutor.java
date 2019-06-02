package tk.minecraftroyale.WorldStuff;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tk.minecraftroyale.Exceptions.ConfigException;
import tk.minecraftroyale.MinecraftRoyale;

import javax.annotation.Nonnull;
import java.io.FileNotFoundException;
import java.util.Objects;

public class WorldCommandExecutor implements CommandExecutor {
    private final MinecraftRoyale minecraftRoyale;

    public WorldCommandExecutor(MinecraftRoyale minecraftRoyale) {
        this.minecraftRoyale = minecraftRoyale;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String label, @Nonnull String[] args) {
        if (cmd.getName().equalsIgnoreCase("loadworld")) {
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
                } catch (FileNotFoundException|ConfigException e) {
                    sender.sendMessage(e.getMessage());
                    minecraftRoyale.getLogger().severe(e.getMessage());
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("Error: invalid round");
            }
        }

        return false;
    }
}
