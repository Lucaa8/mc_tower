package ch.tower.events;

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

    }

    @Override
    public void onStateLeave()
    {

    }
}
