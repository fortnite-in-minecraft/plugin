package tk.minecraftroyale;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import tk.minecraftroyale.WorldStuff.RoyaleWorlds;
import tk.minecraftroyale.WorldStuff.WorldCommandExecutor;

import java.util.Hashtable;

public class Plugin extends JavaPlugin {

    public RoyaleWorlds royaleWorlds;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        royaleWorlds = new RoyaleWorlds(this);

        this.getCommand("loadworld").setExecutor(new WorldCommandExecutor(this));
    }

    @Override
    public void onDisable() {}

    private void initDevCommandMetadata() {
        for (World w : Bukkit.getWorlds()) {
            for (Player p : w.getPlayers()) {
                p.setMetadata("devCommandsEnabled", new FixedMetadataValue(this, false));
            }
        }
    }

    public void setDevCommands(Player player, boolean state) {
        player.setMetadata("devCommandsEnabled", new FixedMetadataValue(this, state));

        if (state) {
            player.sendMessage("Development commands enabled.");
        } else {
            player.sendMessage("Development commands disabled.");
        }
    }


}
