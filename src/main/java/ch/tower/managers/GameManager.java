package ch.tower.managers;

import ch.tower.Main;
import ch.tower.events.EndEvents;
import ch.tower.events.GameEvents;
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

    private final WorldManager worldManager;
    private final ScoreboardManager scoreboardManager;

    public GameManager()
    {
        worldManager = new WorldManager();
        if(worldManager.load())
        {
            this.state = GameState.WAIT;
            TeamsManager.registerTeams();
            scoreboardManager = new ScoreboardManager();
        }
        else
        {
            //if not set to something, the final field crying ouin ouin
            scoreboardManager = null;
            System.err.println("Something went wrong while loading the maps.");
            Main.getInstance().getServer().getPluginManager().disablePlugin(Main.getInstance());
        }
    }

    //The order of operations here is important, do not move lines up and down or do not insert code between lines if you are not sure what you are doing.
    public void stop()
    {
        //Needs to be done before we kick the players. That's because we need to tell the client to explicitly destroy the current active scoreboard before leaving the server.
        //If not destroyed, it can cause errors on proxies like BungeeCord (Switching Spigot servers while Scoreboard are synchronized on the Bungee)
        scoreboardManager.unregister();
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

    public void setState(GameState state)
    {
        this.state = state;
        HandlerList.unregisterAll(EndEvents.getInstance());
        HandlerList.unregisterAll(GameEvents.getInstance());
        HandlerList.unregisterAll(WaitEvents.getInstance());
        Main.getInstance().getServer().getPluginManager().registerEvents(this.state.getListener(), Main.getInstance());
    }

    public ScoreboardManager getScoreboardManager()
    {
        return scoreboardManager;
    }

}
