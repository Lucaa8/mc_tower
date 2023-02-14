package ch.tower.managers;

import ch.tower.Main;
import ch.tower.events.GameEvents;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class GameManager {

    enum GameState
    {
        WAIT, GAME, END;
    }

    private GameState state;

    private final WorldManager worldManager;

    public GameManager()
    {
        worldManager = new WorldManager();
        if(worldManager.load())
        {
            this.state = GameState.WAIT;
            TeamsManager.registerTeams();
        }
        else
        {
            System.err.println("Something went wrong while loading the maps.");
            Main.getInstance().getServer().getPluginManager().disablePlugin(Main.getInstance());
        }
    }

    public void stop()
    {
        //Needed to be sure no players are still in the loaded maps that the server needs to delete.
        for(Player player : Bukkit.getOnlinePlayers())
        {
            player.kickPlayer("§cThe server will restart soon.");
        }
        TeamsManager.unregisterTeams();
        worldManager.unload();
    }

    public GameState getState()
    {
        return state;
    }

    // TODO: 14/02/2023 Maëlys
    public void setState(GameState state)
    {
        this.state = state;
        //HandlerList.unregisterAll();
        if(state == GameState.GAME)
        {
            Main.getInstance().getServer().getPluginManager().registerEvents(new GameEvents(), Main.getInstance());
        }
        //maybe register WaitEvents if state == GameState.WAIT, etc...
    }

}
