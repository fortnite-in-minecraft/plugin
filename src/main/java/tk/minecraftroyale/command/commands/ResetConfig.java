package tk.minecraftroyale.command.commands;

import org.bukkit.command.CommandSender;
import tk.minecraftroyale.command.Command;

import java.io.File;

@Command(value = "resetconfig", development = true)
public class ResetConfig extends CommandBase {

    @Override
    public boolean run(CommandSender sender, String[] args) {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        //noinspection ResultOfMethodCallIgnored
        configFile.delete();
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        return true;
    }
}
