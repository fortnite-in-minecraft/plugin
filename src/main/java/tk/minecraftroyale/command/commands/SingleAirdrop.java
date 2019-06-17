package tk.minecraftroyale.command.commands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tk.minecraftroyale.command.Command;
import tk.minecraftroyale.loot.Airdrop;

@Command(
        value = "airdrop",
        playerOnly = true,
        development = true
)
public class SingleAirdrop extends CommandBase {

    @Override
    public boolea run(CommandSender sender, String[] args) {
        try {
            Player player = (Player) sender;
            Airdrop drop;

            if (args.length == 0) {
                drop = new Airdrop(player.getWorld());
            } else if (args.length == 2) {
                drop = new Airdrop(new Location(player.getWorld(),
                        Integer.parseInt(args[0]),
                        player.getWorld().getHighestBlockYAt(Integer.parseInt(args[0]), Integer.parseInt(args[1])),
                        Integer.parseInt(args[1])));
            } else {
                sender.sendMessage("Error: bad number of arguments");
                return false;
            }

            drop.place();
            sender.sendMessage("Created airdrop at " +
                    drop.getLocation().getBlockX() + ", " +
                    drop.getLocation().getBlockY() + ", " +
                    drop.getLocation().getBlockZ());
            return true;

        } catch (NumberFormatException e) {
            sender.sendMessage("Error: invalid coordinates.");
            return false;
        }
    }
}
