package ch.tower.managers;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.tower.Main;
import ch.tower.events.GameEvents;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;

public class GameManager {

    public static final File CONFIG_FILE = new File(Main.getInstance().getDataFolder(), "config.json");

    public enum GameState
    {
        WAIT, GAME, END;
    }

    private GameState state;

    private final WorldManager worldManager;
    private final ScoreboardManager scoreboardManager;

    private final JSONApi.JSONReader configInfos;

    public enum ConfigField
    {
        MAX_PLAYERS, MIN_PLAYERS, TIMER_DURATION_WAIT, TIMER_DURATION_GAME, GOAL_POINTS;
        public int get(){return Main.getInstance().getManager().configInfos.getInt(name());}
    }

    public GameManager()
    {
        worldManager = new WorldManager();
        if(worldManager.load())
        {
            this.state = GameState.WAIT;
            configInfos = SpigotApi.getJSONApi().readerFromFile(CONFIG_FILE);
            TeamsManager.registerTeams();
            scoreboardManager = new ScoreboardManager();
        }
        else
        {
            //if not set to something, the final field crying ouin ouin
            scoreboardManager = null;
            configInfos = null;
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

    public ScoreboardManager getScoreboardManager()
    {
        return scoreboardManager;
    }

}
