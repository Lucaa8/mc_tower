package ch.tower.events;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.Listener;

public class GameEvents implements StateEvents
{
    private static GameEvents instance = null;
    private GameEvents(){}

    public static synchronized GameEvents getInstance()
    {
        if(instance == null)
        {
            instance = new GameEvents();
        }
        return instance;
    }

    @Override
    public void onStateBegin()
    {
        Bukkit.broadcast("The game begin. GL HF", Server.BROADCAST_CHANNEL_USERS);
    }

    @Override
    public void onStateLeave()
    {

    }
}
