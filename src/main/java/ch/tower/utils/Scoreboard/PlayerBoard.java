package ch.tower.utils.Scoreboard;

import ch.tower.Main;
import ch.tower.utils.Packets.ScoreboardPackets;
import ch.tower.utils.Packets.SpigotPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class PlayerBoard
{

    private final Player player;
    private String parentBoard;
    private String playerTitle;
    private ArrayList<ScoreboardLine> playerLines = new ArrayList<>();

    public PlayerBoard(Player player, String parentBoard)
    {
        this.player = player;
        setParentBoard(parentBoard);
    }

    public Player getPlayer()
    {
        return player;
    }

    public void setParentBoard(String parentBoard)
    {
        Board currentBoard = getParentBoard();
        Board newBoard = Main.getInstance().getManager().getScoreboardManager().getScoreboard(parentBoard);
        if(currentBoard!=null||newBoard!=null)
        {
            if(currentBoard!=null)
            {
                SpigotPlayer.sendPacket(player, ScoreboardPackets.displayObjective(null));
                SpigotPlayer.sendPacket(player, ScoreboardPackets.objective(currentBoard.getName(), null, ScoreboardPackets.Mode.REMOVE));
                this.parentBoard = null;
                playerTitle = null;
                playerLines = new ArrayList<>();
            }
            if(newBoard!=null)
            {
                this.parentBoard = newBoard.getName();
                playerTitle = newBoard.getTitle();
                playerLines = cloneLines(newBoard.getLines());
                SpigotPlayer.sendPacket(player, ScoreboardPackets.objective(this.parentBoard, playerTitle, ScoreboardPackets.Mode.ADD));
                SpigotPlayer.sendPacket(player, ScoreboardPackets.displayObjective(this.parentBoard));
                ArrayList<ScoreboardLine> reverse = new ArrayList<>(playerLines);
                Collections.reverse(reverse);
                for(int i=reverse.size()-1;i>=0;i--)
                {
                    SpigotPlayer.sendPacket(player, ScoreboardPackets.score(this.parentBoard, reverse.get(i).getText(), ScoreboardPackets.Action.CHANGE, i));
                }
            }
        }
    }

    public Board getParentBoard()
    {
        if(parentBoard==null||parentBoard.isEmpty())return null;
        return Main.getInstance().getManager().getScoreboardManager().getScoreboard(parentBoard);
    }

    public boolean isParentBoardValid()
    {
        return getParentBoard()!=null;
    }

    public String getCurrentTitle()
    {
        return playerTitle;
    }

    public void setCurrentTitle(String title)
    {
        if(isParentBoardValid())
        {
            playerTitle = title;
            SpigotPlayer.sendPacket(player, ScoreboardPackets.objective(parentBoard, title, ScoreboardPackets.Mode.CHANGE));
        }
    }

    public void updateLines(Map<String, Object> toReplace)
    {
        Board parent = getParentBoard();
        if(parent!=null&&!playerLines.isEmpty())
        {
            for(ScoreboardLine line : parent.getLines())
            {
                String txt = line.getText();
                boolean asChanged = false;
                for(Map.Entry<String, Object> entry : toReplace.entrySet())
                {
                    if(txt.contains(entry.getKey()))
                    {
                        asChanged = true;
                        txt = txt.replace(entry.getKey(), ""+entry.getValue());
                    }
                }
                ScoreboardLine pLine = getLine(line.getLine());
                if(pLine!=null&&!pLine.getText().equals(txt)&&asChanged)
                {
                    SpigotPlayer.sendPacket(player, ScoreboardPackets.score(parentBoard, pLine.getText(), ScoreboardPackets.Action.REMOVE, 0));
                    pLine.setText(txt);
                    SpigotPlayer.sendPacket(player, ScoreboardPackets.score(parentBoard, pLine.getText(), ScoreboardPackets.Action.CHANGE, playerLines.size()-pLine.getLine()));
                }
            }
        }
    }

    public ScoreboardLine getLine(int line)
    {
        for(ScoreboardLine l : playerLines)
        {
            if(l.getLine()==line)
            {
                return l;
            }
        }
        return null;
    }

    private ArrayList<ScoreboardLine> cloneLines(ArrayList<ScoreboardLine> parentLines)
    {
        ScoreboardLine.LinesBuilder builder = new ScoreboardLine.LinesBuilder();
        for(ScoreboardLine line : parentLines)
        {
            builder.add(line.getLine(), line.getText());
        }
        return builder.getLines();
    }

}
