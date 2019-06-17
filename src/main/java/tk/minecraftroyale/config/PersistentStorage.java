package tk.minecraftroyale.config;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import tk.minecraftroyale.MinecraftRoyale;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class PersistentStorage {
    private static File playerDataFolder = new File(JavaPlugin.getPlugin(MinecraftRoyale.class).getDataFolder(), "state.playerData");
    private static HashMap<UUID, FileConfiguration> loadedConfigs = new HashMap<>();
    private static HashMap<UUID, File> configFiles = new HashMap<>();

    private static void loadConfigFile(UUID uuid) {
        File file = new File(playerDataFolder, uuid.toString() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        loadedConfigs.put(uuid, config);
        configFiles.put(uuid, file);
    }

    private static void saveConfigFile(UUID uuid) {
        if (loadedConfigs.get(uuid) == null) loadConfigFile(uuid);

        try {
            loadedConfigs.get(uuid).save(configFiles.get(uuid));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Object getString(OfflinePlayer player, String path) {
        loadConfigFile(player.getUniqueId());
        return loadedConfigs.get(player.getUniqueId()).get(path);
    }

    public static void set(OfflinePlayer player, String path, Object value) {
        loadConfigFile(player.getUniqueId());
        loadedConfigs.get(player.getUniqueId()).set(path, value);
    }
}
