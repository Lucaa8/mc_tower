package ch.tower.managers;

import org.bukkit.Bukkit;

public class GameManager {

    enum GameState
    {
        WAIT, GAME, END;
    }

    private GameState state;

    public GameManager()
    {
        this.state = GameState.WAIT;
        //TeamsManager.registerTeams();
    }

    public void stop()
    {
        //TeamsManager.unregisterTeams();
    }

    public GameState getState()
    {
        return state;
    }

    public void setState(GameState state)
    {
        this.state = state;
        //maybe register WaitEvents if state == GameState.WAIT, etc...
    }

}
