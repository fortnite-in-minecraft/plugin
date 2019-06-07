package tk.minecraftroyale;

import com.google.gson.JsonPrimitive;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class JSONFileAppender {
    public JSONFileAppender(){

    }

    public void logLine(String line){
        // i'm sorry
        String str = "[\"logLine\", {\"line\": " + new JsonPrimitive(line) + "}]";
        process(str);
    }
    public void playerKill(String playerName, String playerUid, String killerName, String killerUid, String cause){
        // on ja
        String str = "[\"playerKill\", {player: {user: " + new JsonPrimitive(playerName) + ", uid: " + new JsonPrimitive(playerUid) + "}, killer: {user: " + new JsonPrimitive(killerName) + ", uid: " + new JsonPrimitive(killerUid) + "}, cause: " + new JsonPrimitive(cause) + "}]";
        process(str);
    }
    public void playerDeath(String playerName, String playerUid, String cause){
        // forgive me ja
        String str = "[\"playerDeath\", {player: {user: " + new JsonPrimitive(playerName) + ", uid: " + new JsonPrimitive(playerUid) + "}, cause: " + new JsonPrimitive(cause) + "}]";
        process(str);
    }

    public void pointChange(String playerName, String playerUid, int oldPointVal, int newPointVal, String cause) {
        pointChange(playerName, playerUid, oldPointVal, newPointVal, cause, "");
    }

    public void pointChange(String playerName, String playerUid, int oldPointVal, int newPointVal, String cause, String bonus){
        String str = "[\"pointChange\",  {player: {user: " + new JsonPrimitive(playerName) + ", uid: " + new JsonPrimitive(playerUid) + "}, oldPointValue: " + new JsonPrimitive(oldPointVal) + ", newPointValue: " + new JsonPrimitive(newPointVal) + ", cause: {type: " + new JsonPrimitive(cause) + ", bonus: " + new JsonPrimitive(bonus) + "}}]";
        process(str);
    }

    public void anticheatOffense(String playerName, String playerUid, String desc){
        String str = "[\"anticheatOffense\", {player: {user: " + new JsonPrimitive(playerName) + ", uid: " + new JsonPrimitive(playerUid) + "}, cause: " + new JsonPrimitive(desc) + "}]";
        process(str);
    }

    private void process(String str){
        System.out.println(str);
        String filePath = JavaPlugin.getPlugin(MinecraftRoyale.class).getConfig().getString("jsonlog");
        if(filePath == null) filePath = "jsonlog.txt";
        File file = new File(filePath);
        try {
            FileWriter fr = new FileWriter(file, true);
            fr.write(str.trim() + "\n");
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
