package tk.minecraftroyale;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.Objects;

public class ClearInventory {
    public static void clearInventory(Player player){
        emptyInventory(player);
//        resetAdvancements(player);
        clearStatusEffects(player);
        resetHealth(player);
        clearEnderchest(player);
        resetHunger(player);
        resetXP(player);
    }

    private static void emptyInventory(Player player){
        player.getInventory().clear();
    }

//    private static void resetAdvancements(Player player){
//        // this is currently not possible!
//    }

    private static void clearStatusEffects(Player player){
        for (PotionEffect effect : player.getActivePotionEffects())
            player.removePotionEffect(effect.getType());
    }

    private static void resetHealth(Player player){
        double maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();
        player.setHealth(maxHealth);
    }

    private static void clearEnderchest(Player player){
        player.getEnderChest().clear();
    }

    private static void resetXP(Player player){
        player.setTotalExperience(0);
    }

    private static void resetHunger(Player player){
        player.setFoodLevel(20);
    }
}
