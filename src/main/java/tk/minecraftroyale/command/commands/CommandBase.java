package tk.minecraftroyale.command.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import tk.minecraftroyale.MinecraftRoyale;
import tk.minecraftroyale.command.Command;

import javax.annotation.Nonnull;

/**
 * Base class for classes representing commands. When making a new command, be sure to annotate it with
 * {@link Command}.
 */
public abstract class CommandBase {
    protected static MinecraftRoyale plugin = JavaPlugin.getPlugin(MinecraftRoyale.class);

    public CommandBase() {}

    /**
     * The actual logic for a command. The parameters for the command run and its label are omitted because the command
     * name is meant to be specified via an annotation.
     * @param sender Whatever sent the command.
     * @param args The arguments that were sent.
     * @return Whether or not the command was valid. If {@code false} is returned, the usage string for the command
     * will be sent to the user.
     */
    public abstract boolean run(@Nonnull CommandSender sender, @Nonnull String[] args);
}
