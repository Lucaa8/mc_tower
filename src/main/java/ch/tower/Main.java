package ch.tower;

import ch.tower.commands.ChangeStateCommand;
import ch.tower.events.GlobalEvents;
import ch.tower.managers.GameManager;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;

    //maybe put game in static, so we can do Main.getManager() instead of Main.getInstance().getManager() ?
    private GameManager game;

    public void onEnable()
    {
        //registering commands:
        this.getCommand("changeState").setExecutor(new ChangeStateCommand());

        instance = this;
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