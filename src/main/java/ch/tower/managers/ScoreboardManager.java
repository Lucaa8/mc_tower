package ch.tower.managers;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.tower.Main;
import ch.tower.utils.Scoreboard.Board;
import ch.tower.utils.Scoreboard.PlayerBoard;
import ch.tower.utils.Scoreboard.ScoreboardLine;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class ScoreboardManager
{
    public static final File SCOREBOARD_FILE = new File(Main.getInstance().getDataFolder(), "scoreboards.json");
    private final ArrayList<Board> scoreboards = new ArrayList<>();
    private final ArrayList<PlayerBoard> players = new ArrayList<>();

    public enum BoardField {

        PLAYER_COUNT,
        MAX_PLAYER_COUNT,
        TEAM,
        TIMER,
        POINTS_RED,
        POINTS_BLUE,
        MAX_POINTS,
        KILLS,
        DEATHS,
        MONEY,
        MVP_KILLS,
        MVP_KILLS_COUNT,
        MVP_DEATHS,
        MVP_DEATHS_COUNT;

        public void update(Player player, Object value)
        {
            Main.getInstance().getManager().getScoreboardManager().updateLine(player, "{"+name()+"}", value);
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
            ScoreboardLine.LinesBuilder linesBuilder = new ScoreboardLine.LinesBuilder();
            for(int i=0;i<lines.size();i++)
            {
                linesBuilder.add(i, (String) lines.get(i));
            }
            registerScoreboard(sb, r_sb.getString("Title"), linesBuilder.getLines());
        }
    }

    public Board getScoreboard(String uniqueName)
    {
        if(uniqueName==null)return null;
        for(Board s : scoreboards)
        {
            if(s.getName().equals(uniqueName))
            {
                return s;
            }
        }
        return null;
    }

    public boolean doesScoreboardExist(String name)
    {
        return getScoreboard(name)!=null;
    }

    private void registerScoreboard(String name, String title, ArrayList<ScoreboardLine> lines)
    {
        if(!doesScoreboardExist(name))
        {
            scoreboards.add(new Board(name, title, lines));
        }
    }

    private void unregisterScoreboard(String scoreboard)
    {
        if(scoreboard!=null&&doesScoreboardExist(scoreboard))
        {
            Board b = getScoreboard(scoreboard);
            for(PlayerBoard pb : players)
            {
                Board sb = pb.getParentBoard();
                if(sb!=null&&sb.getName().equals(b.getName()))
                {
                    pb.setParentBoard(null);
                }
            }
            scoreboards.remove(b);
        }
    }

    public void unregister(){
        for(Board s : new ArrayList<>(scoreboards))
        {
            unregisterScoreboard(s.getName());
        }
        //to be safe
        scoreboards.clear();
    }

    public PlayerBoard getBoard(Player player)
    {
        for(PlayerBoard pb : players)
        {
            if(pb.getPlayer() == player)
            {
                return pb;
            }
        }
        return null;
    }

    /**
     * Show or hide the specified scoreboard.
     * @param player The player on which the action takes place
     * @param board The new scoreboard to display. <p>If the scoreboard already displayed is the same then nothing happen. <p>You can pass null if you want to hide any current active scoreboard.
     */
    public void setBoard(Player player, String board)
    {
        if(board != null && !doesScoreboardExist(board))
            return;
        PlayerBoard pb = getBoard(player);
        if(pb == null)
        {
            players.add(new PlayerBoard(player, board));
        }
        else
        {
            if(!pb.getParentBoard().getName().equals(board))
                pb.setParentBoard(board);
        }
    }

    private void updateLine(Player player, String field, Object value)
    {
        PlayerBoard pb = getBoard(player);
        if(pb != null)
        {
            pb.updateLines(Map.of(field, value));
        }
    }

}
