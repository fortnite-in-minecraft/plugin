package tk.minecraftroyale.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;

/**
 * {@link FunctionalInterface} for specifying arbitrary logic for a command.
 */
@FunctionalInterface
public interface CommandLogic {
    boolean run(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args);
}
