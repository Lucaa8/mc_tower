package ch.tower.events;

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

    }

    @Override
    public void onStateLeave()
    {

    }
}
