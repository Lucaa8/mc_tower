package ch.tower.managers;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.tower.Main;
import ch.tower.TowerPlayer;
import ch.tower.events.EndEvents;
import ch.tower.events.GameEvents;
import ch.tower.events.StateEvents;
import ch.tower.events.WaitEvents;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.io.File;

public class GameManager {

    public static final File CONFIG_FILE = new File(Main.getInstance().getDataFolder(), "config.json");
    public static final File MESSAGES_FILE = new File(Main.getInstance().getDataFolder(), "plugin_messages.json");

    public enum GameState
    {
        WAIT(WaitEvents.getInstance()),//
        GAME(GameEvents.getInstance()),//
        END(EndEvents.getInstance());

        private final StateEvents stateInstance;
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

    private final WorldManager worldManager;
    private final ScoreboardManager scoreboardManager;
    private final NPCManager npcManager;
    private final ShopMenuManager shopManager;

    private static final JSONApi.JSONReader configInfos = SpigotApi.getJSONApi().readerFromFile(CONFIG_FILE);
    private static final JSONApi.JSONReader messages = SpigotApi.getJSONApi().readerFromFile(MESSAGES_FILE);

    public enum ConfigField
    {
        MAX_PLAYERS, MIN_PLAYERS, TIMER_DURATION_WAIT, TIMER_DURATION_GAME, TIMER_DURATION_END, GOAL_POINTS, LAST_ATTACKER_TIMER, ABANDON_AFTER;
        public int get(){return configInfos.getInt(name());}
    }

    public static String getMessage(String key, String...replacements)
    {
        if(!messages.c(key))
            return "Unknown message";
        String msg = messages.getString(key);
        for(int i=0;i<replacements.length;i++)
        {
            msg = msg.replace(String.format("{%d}", i), replacements[i]);
        }
        return msg;
    }

    public GameManager()
    {
        worldManager = new WorldManager();
        if(worldManager.load())
        {
            this.setState(GameState.WAIT);
            TeamsManager.registerTeams();
            scoreboardManager = new ScoreboardManager();
            npcManager = new NPCManager();
            shopManager = new ShopMenuManager();
        }
        else
        {
            //if not set to something, the final field crying ouin ouin
            scoreboardManager = null;
            npcManager = null;
            shopManager = null;
            System.err.println("Something went wrong while loading the maps.");
            Main.getInstance().getServer().getPluginManager().disablePlugin(Main.getInstance());
        }
    }

    //The order of operations here is important, do not move lines up and down or do not insert code between lines if you are not sure what you are doing.
    public void stop()
    {
        npcManager.unregisterAll();
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
        Bukkit.shutdown();
        //Bukkit.spigot().restart();
    }

    public void abandon(TowerPlayer player)
    {
        TeamsManager.PlayerTeam team = player.getAbandoningTeam();
        if(team != null)
        {
            Bukkit.broadcastMessage(GameManager.getMessage("MSG_GAME_ABANDON_2", team.getColorCode()+player.asOfflinePlayer().getName()));
            //TODO give advantage to this team?
        }
        TowerPlayer.removePlayer(player);
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

    public ScoreboardManager getScoreboardManager()
    {
        return scoreboardManager;
    }

    public WorldManager getWorldManager(){return worldManager;}

    public NPCManager getNpcManager()
    {
        return npcManager;
    }

    public ShopMenuManager getShopManager()
    {
        return shopManager;
    }

}
