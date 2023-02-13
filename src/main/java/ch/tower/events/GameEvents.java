package ch.tower.events;

import org.bukkit.event.Listener;

public class GameEvents implements Listener
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


}
