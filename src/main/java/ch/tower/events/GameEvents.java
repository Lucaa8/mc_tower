package ch.tower.events;

import ch.tower.TowerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Server;

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
        TowerPlayer.registerPlayers();
        Bukkit.broadcast("The game begin. GL HF", Server.BROADCAST_CHANNEL_USERS);
    }

    @Override
    public void onStateLeave()
    {

    }
}
