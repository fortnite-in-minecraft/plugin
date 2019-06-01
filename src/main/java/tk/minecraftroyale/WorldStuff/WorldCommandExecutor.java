package tk.minecraftroyale.WorldStuff;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tk.minecraftroyale.Exceptions.ConfigException;
import tk.minecraftroyale.MinecraftRoyale;

import java.io.FileNotFoundException;

public class WorldCommandExecutor implements CommandExecutor {
    private MinecraftRoyale minecraftRoyale;

    public WorldCommandExecutor(MinecraftRoyale minecraftRoyale) {
        this.minecraftRoyale = minecraftRoyale;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("loadworld")) {
            if (args.length != 1) return false;

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
                return false;
            }

            Location worldSpawn;

            try {
                int worldNum = Integer.parseInt(args[0]);

                try {
                    worldSpawn = minecraftRoyale.royaleWorlds.getWorld(worldNum).getSpawnLocation();
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
