package ch.tower.managers;

import ch.tower.Main;
import ch.tower.events.EndEvents;
import ch.tower.events.GameEvents;
import ch.tower.events.WaitEvents;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.logging.Handler;

public class GameManager {

    enum GameState
    {
        WAIT(WaitEvents.getInstance()),//
        GAME(GameEvents.getInstance()),//
        END(EndEvents.getInstance());

        private Listener listener;
        private GameState(Listener listener)
        {
            this.listener = listener;
        }
        public Listener getListener()
        {
            return listener;
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
        this.state = state;
        HandlerList.unregisterAll(EndEvents.getInstance());
        HandlerList.unregisterAll(GameEvents.getInstance());
        HandlerList.unregisterAll(WaitEvents.getInstance());
        Main.getInstance().getServer().getPluginManager().registerEvents(this.state.getListener(), Main.getInstance());
    }

}
