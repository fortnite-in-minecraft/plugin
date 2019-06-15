package tk.minecraftroyale.command.commands;


import org.bukkit.command.CommandSender;
import tk.minecraftroyale.command.Command;

@Command(value = "test", development = true)
public class TestCommand extends CommandBase {

    @Override
    public boolean run(CommandSender sender, String[] args) {
        sender.sendMessage("Hello World!");
        return true;
    }
}
