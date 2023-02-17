package ch.tower.managers;

import ch.tower.Main;
import ch.tower.events.EndEvents;
import ch.tower.events.GameEvents;
import ch.tower.events.StateEvents;
import ch.tower.events.WaitEvents;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.logging.Handler;

public class GameManager {

    public enum GameState
    {
        WAIT(WaitEvents.getInstance()),//
        GAME(GameEvents.getInstance()),//
        END(EndEvents.getInstance());

        private StateEvents stateInstance;
        private GameState(StateEvents stateInstance)
        {
            this.stateInstance = stateInstance;
        }
        public StateEvents getStateInstance()
        {
            return stateInstance;
        }
    }

    private GameState state;

    public GameManager()
    {
        this.setState(GameState.WAIT);
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
        if(this.state != null) this.state.getStateInstance().onStateLeave();
        this.state = state;
        HandlerList.unregisterAll(EndEvents.getInstance());
        HandlerList.unregisterAll(GameEvents.getInstance());
        HandlerList.unregisterAll(WaitEvents.getInstance());
        this.state.getStateInstance().onStateBegin();
        Main.getInstance().getServer().getPluginManager().registerEvents(this.state.getStateInstance(), Main.getInstance());
    }

}
