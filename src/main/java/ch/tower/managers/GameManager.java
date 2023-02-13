package ch.tower.managers;

import ch.tower.Main;
import ch.tower.events.GameEvents;

public class GameManager {

    //public static final File SPAWN_FILE = new File(Main.getInstance().getDataFolder(), "spawns.json");

    enum GameState
    {
        WAIT, GAME, END;
    }

    private GameState state;

    public GameManager()
    {
        this.state = GameState.WAIT;
        TeamsManager.registerTeams();
    }

    public void stop()
    {
        TeamsManager.unregisterTeams();
    }

    public GameState getState()
    {
        return state;
    }

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
