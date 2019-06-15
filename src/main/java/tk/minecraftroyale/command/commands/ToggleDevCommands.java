package tk.minecraftroyale.command.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tk.minecraftroyale.MinecraftRoyale;
import tk.minecraftroyale.command.Command;

@Command("toggledevcommands")
public class ToggleDevCommands extends CommandBase {

    @Override
    public boolean run(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Error: must be a player");
            return true;
        }

        MinecraftRoyale.setDevCommands((Player) sender, !MinecraftRoyale.getDevCommands(sender));
        return true;
    }
}
