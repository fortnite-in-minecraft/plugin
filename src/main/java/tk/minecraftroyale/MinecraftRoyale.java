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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Logger;


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
        this.getCommand("loadworld").setExecutor(new WorldCommandExecutor(this));
        this.getCommand("mrtp").setExecutor(new WorldCommandExecutor(this));
        getServer().getPluginManager().registerEvents(new DeathListener(), this);

    }

    @Override
    public void onDisable() {}

    public void setDevCommands(Player player, boolean state) {
        player.setMetadata("devCommandsEnabled", new FixedMetadataValue(this, state));

        if (state) {
            player.sendMessage("Development commands enabled.");
        } else {
            player.sendMessage("Development commands disabled.");
        }
    }

    public boolean getDevCommands(Player player) {
        List<MetadataValue> vals = player.getMetadata("devCommandsEnabled");
        for (MetadataValue val : vals) {
            if (val.getOwningPlugin() == this) {
                return val.asBoolean();
            }
        }
        return false;
    }


//    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String label, @Nonnull String[] args) {
//        if (cmd.getName().equalsIgnoreCase("toggledevcommands")) {
//
//        }
//    }

}
