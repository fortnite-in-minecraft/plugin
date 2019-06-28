package tk.minecraftroyale;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class BossbarManager {
    private BossBar theBar;
    public BossbarManager(int round, String message) {

        Bukkit.getLogger().info("adding bar " + round);
        theBar = Bukkit.createBossBar("Round #" + round + ": " + message, BarColor.RED, BarStyle.SOLID);
        theBar.setVisible(true);
        for(Player p : Bukkit.getOnlinePlayers()){
            addPlayer(p);
        }
    }

    public void deleteBar(){
        Bukkit.getLogger().info("deleting bar");
        if(theBar != null) theBar.removeAll();
        theBar = null;
    }

    public void addPlayer(Player player){
        if(theBar != null && !theBar.getPlayers().contains(player)){
            theBar.addPlayer(player);
//            Bukkit.getLogger().info("adding player to bossbar");
        }
//        Bukkit.getLogger().info("adding player to bossbar " + (theBar != null) + " " + !theBar.getPlayers().contains(player));
    }

    public void setProgress(double progress){
        Bukkit.getLogger().info("setting progress to " + progress);
        theBar.setProgress(Math.max(Math.min(progress, 1), 0));
    }
}
