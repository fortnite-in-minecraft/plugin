package tk.minecraftroyale;

import org.bukkit.plugin.java.JavaPlugin;
import tk.minecraftroyale.WorldStuff.RoyaleWorlds;

public class Plugin extends JavaPlugin {

    private RoyaleWorlds royaleWorlds;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        royaleWorlds = new RoyaleWorlds(this);
    }

    @Override
    public void onDisable() {}


}
