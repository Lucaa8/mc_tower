package ch.tower;

import ch.tower.events.GlobalEvents;
import ch.tower.managers.GameManager;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main extends JavaPlugin {

    private static Main instance;

    //maybe put game in static, so we can do Main.getManager() instead of Main.getInstance().getManager() ?
    private GameManager game;

    public void onEnable()
    {
        instance = this;
        if(getDataFolder().mkdirs())
        {
            System.out.println("Generating spawns.json file...");
            try {
                Files.copy(getClassLoader().getResource("spawns.json").openStream(), new File(getDataFolder(), "spawns.json").toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new GlobalEvents(), this);
        game = new GameManager();
    }

    public void onDisable()
    {
        game.stop();
    }

    public static Main getInstance()
    {
        return instance;
    }

    public GameManager getManager()
    {
        return game;
    }

}