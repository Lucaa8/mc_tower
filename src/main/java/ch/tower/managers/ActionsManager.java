package ch.tower.managers;

import ch.tower.Main;
import ch.tower.listeners.GameKillEvent;
import ch.tower.listeners.GamePointEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.io.File;

public class ActionsManager implements Listener {

    public static final File ACTIONS_FILE = new File(Main.getInstance().getDataFolder(), "actions.json");

    public ActionsManager()
    {
        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
    }

    @EventHandler
    public void onKill(GameKillEvent e)
    {

    }

    @EventHandler
    public void onPoint(GamePointEvent e)
    {

    }

}
