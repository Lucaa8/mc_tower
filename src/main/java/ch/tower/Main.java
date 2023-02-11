package ch.tower;

import ch.tower.events.GlobalEvents;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;

    //maybe put game in static, so we can do Main.getManager() instead of Main.getInstance().getManager() ?
    private GameManager game;

    public void onEnable()
    {
        instance = this;
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new GlobalEvents(), this);
        game = new GameManager();
    }

    public void onDisable()
    {
        //maybe call game.stop() if something is needed before disabling
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