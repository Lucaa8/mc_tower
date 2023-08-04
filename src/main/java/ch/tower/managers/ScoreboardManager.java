package ch.tower.managers;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.luca008.SpigotApi.Api.ScoreboardAPI;
import ch.luca008.SpigotApi.SpigotApi;
import ch.tower.Main;
import ch.tower.TowerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.json.simple.JSONArray;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

public class ScoreboardManager
{
    private final ScoreboardListener slistener = new ScoreboardListener();
    public static class ScoreboardListener implements Listener
    {
        @EventHandler(priority = EventPriority.LOWEST)
        public void onJoinSetBoard(PlayerJoinEvent e)
        {
            Main.getInstance().getManager().getScoreboardManager().updateBoard(e.getPlayer());
        }

        @EventHandler
        public void onQuitNullifyBoard(PlayerQuitEvent e){
            SpigotApi.getScoreboardApi().setScoreboard(e.getPlayer(), null);
        }
    }

    public static final File SCOREBOARD_FILE = new File(Main.getInstance().getDataFolder(), "scoreboards.json");

    public enum BoardField
    {
        PLAYER_COUNT,
        MAX_PLAYER_COUNT,
        TEAM,
        TIMER,
        POINTS_RED,
        POINTS_BLUE,
        MAX_POINTS,
        POINTS, //Unique player points
        KILLS,
        ASSISTS,
        DEATHS,
        MONEY,
        MVP_KILLS,
        MVP_KILLS_COUNT,
        MVP_DEATHS,
        MVP_DEATHS_COUNT;

        public String toFormat()
        {
            return String.format("{%s}", name());
        }
        public void update(Player player, String value)
        {
            ScoreboardAPI.PlayerScoreboard board = SpigotApi.getScoreboardApi().getScoreboard(player);
            if(board != null){
                board.setPlaceholder(name(), value);
            }
        }

    }

    //This class is used to keep consistency between bulk placeholders update on join/change board and single placeholder update on event
    public static class PlaceholderHelper {

        public static class PlayerHelper {

            private final TowerPlayer player;

            private PlayerHelper(@Nonnull TowerPlayer player){
                this.player = player;
            }

            public String getTeam(){
                return PlaceholderHelper.getTeamName(player.getTeam());
            }

            public String getPoints(){
                return String.valueOf(player.getPoints());
            }

            public String getKills(){
                return String.valueOf(player.getKills());
            }

            public String getAssists(){
                return String.valueOf(player.getAssists());
            }

            public String getDeaths(){
                return String.valueOf(player.getDeaths());
            }

            public String getMoney(){
                return String.valueOf(player.getMoney())+"$";
            }

        }

        @Nonnull
        public static PlayerHelper getPlayerHelper(@Nonnull TowerPlayer player){
            return new PlayerHelper(player);
        }

        //TODO return red team points
        public static String getRedPoints(){
            return "0";
        }

        //TODO return blue team points
        public static String getBluePoints(){
            return "0";
        }

        public static String getGoalPoints(){
            return String.valueOf(GameManager.ConfigField.GOAL_POINTS.get());
        }

        public static String getTotalPlayerCount(){
            return String.valueOf(Bukkit.getOnlinePlayers().size());
        }

        public static String getMaxPlayerCount(){
            return String.valueOf(GameManager.ConfigField.MAX_PLAYERS.get());
        }

        public static String getWaitTimer(){
            return String.valueOf(GameManager.ConfigField.TIMER_DURATION_WAIT.get());
        }

        public static String getGameTimer(){
            return String.valueOf(GameManager.ConfigField.TIMER_DURATION_GAME.get());
        }

        @Nonnull
        public static String getTeamName(@Nullable TeamsManager.PlayerTeam team){
            return team == null ? "§aNone" : (team.getColorCode()+team.getInfo().apiTeam().getDisplayName());
        }

    }

    public ScoreboardManager()
    {
        JSONApi.JSONReader r = SpigotApi.getJSONApi().readerFromFile(SCOREBOARD_FILE);
        for(Object sb_key : r.asJson().keySet())
        {
            String sb = (String)sb_key;
            JSONApi.JSONReader r_sb = r.getJson(sb);
            JSONArray lines = r_sb.getArray("Lines");
            ScoreboardAPI.LinesBuilder linesBuilder = new ScoreboardAPI.LinesBuilder();
            for(int i=0;i<lines.size();i++)
            {
                linesBuilder.add(i, (String) lines.get(i));
            }
            SpigotApi.getScoreboardApi().registerScoreboard(sb, r_sb.getString("Title"), linesBuilder.getLines());
        }
        Bukkit.getServer().getPluginManager().registerEvents(slistener, Main.getInstance());
        registerPlayers();
    }

    private void registerPlayers(){
        Bukkit.getOnlinePlayers().forEach(this::updateBoard);
    }

    public void unregister(){
        ScoreboardAPI api = SpigotApi.getScoreboardApi();
        Bukkit.getOnlinePlayers().forEach(p->api.setScoreboard(p, null));
        HandlerList.unregisterAll(slistener);
    }

    public void updateBoard(Player player){
        String board = shouldHaveScoreboard(player);
        Main m = Main.getInstance();
        Bukkit.getScheduler().runTaskLater(m, ()->updatePH(SpigotApi.getScoreboardApi().setScoreboard(player, board)),1L);
    }

    private String shouldHaveScoreboard(Player player){
        Main m = Main.getInstance();
        String sb = "SPECTATOR";
        if(m.getManager().getState() == GameManager.GameState.WAIT)
        {
            sb = GameManager.GameState.WAIT.name();
        }
        else
        {
            TowerPlayer tp = TowerPlayer.getPlayer(player);
            if(tp!=null)
            {
                sb = m.getManager().getState().name();
            }
        }
        return sb;
    }

    //Bulk updating scoreboard on set or on change (i.e when the wait board is switched to game board, or when a player rejoin during the game)
    private void updatePH(@Nullable ScoreboardAPI.PlayerScoreboard board){
        if(board == null || !board.isParentBoardValid()){
            return;
        }

        String name = board.getParentBoard().getName();

        if(name.equals(GameManager.GameState.WAIT.name())){
            board.setPlaceholder(BoardField.TEAM.name(), PlaceholderHelper.getTeamName(TeamsManager.getPlayerTeam(board.getPlayer())));
            board.setPlaceholder(BoardField.PLAYER_COUNT.name(), PlaceholderHelper.getTotalPlayerCount());
            board.setPlaceholder(BoardField.MAX_PLAYER_COUNT.name(), PlaceholderHelper.getMaxPlayerCount());
            //We set the maximum (in case the timer hasnt started yet). If the timer has started, WaitEvents will update it the next second.
            board.setPlaceholder(BoardField.TIMER.name(), PlaceholderHelper.getWaitTimer());
        } else if(name.equals(GameManager.GameState.GAME.name())){
            TowerPlayer tp = TowerPlayer.getPlayer(board.getPlayer());
            if(tp != null){
                PlaceholderHelper.PlayerHelper playerInfos = tp.boardHelder;
                board.setPlaceholder(BoardField.TEAM.name(), playerInfos.getTeam());
                board.setPlaceholder(BoardField.KILLS.name(), playerInfos.getKills());
                board.setPlaceholder(BoardField.ASSISTS.name(), playerInfos.getAssists());
                board.setPlaceholder(BoardField.POINTS.name(), playerInfos.getPoints());
                board.setPlaceholder(BoardField.DEATHS.name(), playerInfos.getDeaths());
                board.setPlaceholder(BoardField.MONEY.name(), playerInfos.getMoney());
            }
            board.setPlaceholder(BoardField.POINTS_BLUE.name(), PlaceholderHelper.getBluePoints());
            board.setPlaceholder(BoardField.POINTS_RED.name(), PlaceholderHelper.getRedPoints());
            board.setPlaceholder(BoardField.MAX_POINTS.name(), PlaceholderHelper.getGoalPoints());
            //We set the maximum by default. But GameEvents will update it the next second.
            board.setPlaceholder(BoardField.TIMER.name(), PlaceholderHelper.getGameTimer());
        }
        //LoginEvent is cancelled in the end state so ne need to bulk update scoreboard placeholder.

    }

}
