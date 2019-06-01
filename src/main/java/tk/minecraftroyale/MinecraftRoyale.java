package tk.minecraftroyale;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import tk.minecraftroyale.Listeners.DeathListener;
import tk.minecraftroyale.WorldStuff.RoyaleWorlds;
import tk.minecraftroyale.WorldStuff.WorldCommandExecutor;

import javax.security.auth.login.LoginException;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;


public class MinecraftRoyale extends JavaPlugin {

    public RoyaleWorlds royaleWorlds;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        royaleWorlds = new RoyaleWorlds(this);

        for (World w : Bukkit.getWorlds()) {
            for (Player p : w.getPlayers()) {
                setDevCommands(p, false);
            }
        }

        Objects.requireNonNull(this.getCommand("loadworld")).setExecutor(new WorldCommandExecutor(this));
        Objects.requireNonNull(this.getCommand("mrtp")).setExecutor(new WorldCommandExecutor(this));
        getServer().getPluginManager().registerEvents(new DeathListener(), this);

    }

    @Override
    public void onDisable() {}

    private void setDevCommands(Player player, boolean state) {
        player.setMetadata("devCommandsEnabled", new FixedMetadataValue(this, state));

        if (state) {
            player.sendMessage("Development commands enabled.");
        } else {
            player.sendMessage("Development commands disabled.");
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean getDevCommands(Player player) {
        List<MetadataValue> vals = player.getMetadata("devCommandsEnabled");
        for (MetadataValue val : vals) {
            if (val.getOwningPlugin() == this) {
                return val.asBoolean();
            }
        }
        return false;
    }


    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String label, @Nonnull String[] args) {
        if (cmd.getName().equalsIgnoreCase("toggledevcommands")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Error: must be a player");
                return true;
            }

            setDevCommands((Player) sender, !getDevCommands((Player) sender));
            return true;
        }

        return false;
    }

}
