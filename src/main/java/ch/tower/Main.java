package ch.tower;

import ch.tower.commands.ChangeStateCommand;
import ch.tower.events.GlobalEvents;
import ch.tower.managers.GameManager;
import ch.tower.managers.ScoreboardManager;
import ch.tower.managers.WorldManager;
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
        //registering commands:
        this.getCommand("changeState").setExecutor(new ChangeStateCommand());

        instance = this;
        if(!getDataFolder().mkdirs() && !getDataFolder().exists())
        {
            System.err.println("Cannot create Tower plugin folder...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if(!copyStreamToFile("spawns.json", WorldManager.SPAWN_FILE) || !copyStreamToFile("scoreboards.json", ScoreboardManager.SCOREBOARD_FILE))
        {
            System.err.println("Cannot generate all required resources...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new GlobalEvents(), this);
        game = new GameManager();
    }

    public void onDisable()
    {
        //maybe null if onEnable failed and want to disable the plugin
        if(game != null)
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

    private boolean copyStreamToFile(String resourceName, File resourceFile)
    {
        if(!resourceFile.exists())
        {
            System.out.println("Generating "+ resourceName +" file...");
            try {
                Files.copy(getClassLoader().getResource(resourceName).openStream(), resourceFile.toPath());
            } catch (IOException e) {
                System.err.println("Cannot generate " + resourceName + " file...");
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
}