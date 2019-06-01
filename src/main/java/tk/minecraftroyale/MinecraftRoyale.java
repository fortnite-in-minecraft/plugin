package tk.minecraftroyale;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import tk.minecraftroyale.Listeners.DeathListener;
import tk.minecraftroyale.WorldStuff.RoyaleWorlds;
import tk.minecraftroyale.WorldStuff.WorldCommandExecutor;

import javax.security.auth.login.LoginException;
import java.util.logging.Logger;


public class MinecraftRoyale extends JavaPlugin {

    public RoyaleWorlds royaleWorlds;
    public Logger logger = getLogger();

    @Override
    public void onEnable() {
        getLogger().info("Enabling");
        try {
            Discord.main();
        } catch (LoginException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        saveDefaultConfig();
        royaleWorlds = new RoyaleWorlds(this);

        this.getCommand("loadworld").setExecutor(new WorldCommandExecutor(this));
        getServer().getPluginManager().registerEvents(new DeathListener(), this);

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
