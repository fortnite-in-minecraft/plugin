package tk.minecraftroyale.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.reflections.Reflections;
import tk.minecraftroyale.MinecraftRoyale;
import tk.minecraftroyale.command.commands.CommandBase;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;

/**
 * Singleton {@link CommandExecutor} which dynamically loads commands from {@code tk.minecraftroyale.command.commands}.
 * Commands are discovered at runtime by looking through {@code tk.minecraftroyale.command.commands} and treating all
 * classes that extend {@link CommandBase} and have the {@link Command} annotation as commands.
 */
public class DynamicCommandExecutor implements CommandExecutor {

    // Singleton instance
    private static DynamicCommandExecutor instance;

    // Map of commands to their annotations, which tells us the name of each command as well as whether or not a
    // command is a development command.
    private static Map<CommandBase, tk.minecraftroyale.command.Command> commands;

    public static DynamicCommandExecutor getInstance() {
        if (instance == null)
            instance = new DynamicCommandExecutor();

        return instance;
    }

    /**
     * Populates the list of commands. Commands need to be annotated with {@link Command} because the annotation
     * contains command metadata like the name of the command and whether or not it is a development command.
     */
    private DynamicCommandExecutor() {
        Reflections reflections = new Reflections("tk.minecraftroyale.command.commands");
        Bukkit.getLogger().info("Loading commands");

        Set<Class<? extends CommandBase>> commandSubClasses = reflections.getSubTypesOf(CommandBase.class);
        commands = new HashMap<>();

        for (Class<? extends CommandBase> subClass: commandSubClasses) {
            tk.minecraftroyale.command.Command commandAnnotation = subClass.getAnnotation(tk.minecraftroyale.command.Command.class);

            // If it's null, the class doesn't have the annotation and we should skip it.
            if (commandAnnotation != null) {
                try {
                    CommandBase command = subClass.getConstructor().newInstance();
                    commands.put(command, commandAnnotation);
                } catch (NoSuchMethodException|IllegalArgumentException e) {
                    Bukkit.getLogger().severe("Command " + subClass.getCanonicalName() + " has no default constructor. It will not be loaded.");
                    Bukkit.getLogger().severe(e.toString());
                } catch (InstantiationException e) {
                    Bukkit.getLogger().warning("Command" + subClass.getCanonicalName() + " is abstract. It will be skipped.");
                    Bukkit.getLogger().warning("If this is the intended behavior, consider removing the Command annotation so it will not be checked.");
                } catch (IllegalAccessException e) {
                    Bukkit.getLogger().severe("Command" + subClass.getCanonicalName() + " has a default constructor, but it is not accessible. It will not be loaded.");
                    Bukkit.getLogger().severe(e.toString());
                } catch (InvocationTargetException e) {
                    Bukkit.getLogger().severe("Command" + subClass.getCanonicalName() + "'s constructor threw an exception:");
                    Bukkit.getLogger().severe(e.toString());
                }
            }

        }
    }

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String label, @Nonnull String[] args) {
        for (CommandBase command : commands.keySet()) {

            // If the command name matches
            if (cmd.getName().equalsIgnoreCase(commands.get(command).value())) {
                if (commands.get(command).development() && !MinecraftRoyale.getDevCommands(sender)) {
                    sender.sendMessage("You do not have development commands enabled. Please use /toggledevcommands to enable them.");
                    return true;
                }

                return command.run(sender, args);
            }
        }

        return false;
    }

    public List<String> getRegisteredCommandNames() {
        ArrayList<String> names = new ArrayList<>();
        for (CommandBase commandBase : commands.keySet()) {
            names.add(commands.get(commandBase).value());
        }

        return names;
    }
}
