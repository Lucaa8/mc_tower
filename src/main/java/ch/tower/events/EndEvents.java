package ch.tower.events;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.Listener;

public class EndEvents implements StateEvents
{
    private static EndEvents instance = null;
    private EndEvents(){}

    public static synchronized EndEvents getInstance()
    {
        if(instance == null)
        {
            instance = new EndEvents();
        }
        return instance;
    }

    @Override
    public void onStateBegin()
    {
        Bukkit.broadcast("Game Over. GG!", Server.BROADCAST_CHANNEL_USERS);
    }

    @Override
    public void onStateLeave()
    {

    }
}
