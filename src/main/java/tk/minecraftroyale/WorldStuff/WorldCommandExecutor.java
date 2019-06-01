package tk.minecraftroyale.WorldStuff;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tk.minecraftroyale.Exceptions.ConfigException;
import tk.minecraftroyale.Plugin;

import javax.annotation.Nonnull;
import java.io.FileNotFoundException;
import java.util.Objects;

public class WorldCommandExecutor implements CommandExecutor {
    private Plugin plugin;

    public WorldCommandExecutor(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String label, @Nonnull String[] args) {
        if (cmd.getName().equalsIgnoreCase("loadworld")) {
            if (args.length != 1) return false;
            else if (sender instanceof Player) {
                if (!plugin.getDevCommands((Player) sender)) {
                    sender.sendMessage("You do not have development commands enabled. Please use /toggledevcommands to enable them.");
                    return false;
                }
            }

            try {
                World w = plugin.royaleWorlds.getWorld(Integer.parseInt(args[0]));
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
                return false;
            }

            Location worldSpawn;

            try {
                int worldNum = Integer.parseInt(args[0]);

                try {
                    worldSpawn = Objects.requireNonNull(plugin.royaleWorlds.getWorld(worldNum)).getSpawnLocation();
                    ((Player) sender).teleport(worldSpawn);
                    return true;
                } catch (FileNotFoundException e) {
                    sender.sendMessage("Error: unable to locate world");
                } catch (ConfigException e) {
                    sender.sendMessage("Error: bad config option at path: " + e.getPath());
                }

            } catch (NumberFormatException e) {

            }
        }

        return false;
    }
}
