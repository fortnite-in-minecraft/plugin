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
                }
            }

            try {
                World w = minecraftRoyale.royaleWorlds.getWorld(Integer.parseInt(args[0]));
                if (w == null) {
                    sender.sendMessage("Error: invalid round " + args[0]);
                }
            } catch (FileNotFoundException e) {
                sender.sendMessage("Error: unable to locate world");
            } catch (ConfigException e) {
                sender.sendMessage("Error: bad config option at path: " + e.getPath());
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

                try {
                    worldSpawn = Objects.requireNonNull(minecraftRoyale.royaleWorlds.getWorld(worldNum)).getSpawnLocation();
                } catch (FileNotFoundException e) {
                    sender.sendMessage("Error: unable to locate world. This is a bug.");
                    return true;
                } catch (ConfigException e) {
                    sender.sendMessage("Error: bad config option at path: " + e.getPath());
                    return true;
                }

            } catch (NumberFormatException e) {
                World w = Bukkit.getWorld(args[0]);
                if (w == null) {
                    sender.sendMessage("Error: world \"" + args[0] +  "\" not found");
                    return true;
                }

                worldSpawn = w.getSpawnLocation();
            }

            ((Player) sender).teleport(worldSpawn);
        }

        return false;
    }
}
