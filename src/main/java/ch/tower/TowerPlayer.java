package ch.tower;

import ch.tower.managers.ScoreboardManager;
import ch.tower.managers.TeamsManager;
import ch.tower.managers.TeamsManager.PlayerTeam;
import ch.tower.utils.Scoreboard.PlayerBoard;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class TowerPlayer
{
    private static final ArrayList<TowerPlayer> players = new ArrayList<>();

    //Can register only once a game. When state changes from wait to game.
    public static boolean registerPlayers()
    {
        if(players.size()!=0)
            return false;
        for(Player player : Bukkit.getOnlinePlayers())
        {
            PlayerTeam pt = TeamsManager.getPlayerTeam(player);
            if(pt == PlayerTeam.RED || pt == PlayerTeam.BLUE)
            {
                players.add(new TowerPlayer(player));
            }
            else
            {
                //The player isn't in a team when the game is starting: we kick it and then he can rejoin a little bit later as a spectator (gamestate GAME)
                Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(), "§4Come back later as a spectator.", new Date(System.currentTimeMillis()+10000), "Server");
                player.kickPlayer("§4Come back later as a spectator.");
            }
        }
        return true;
    }

    public static TowerPlayer getPlayer(OfflinePlayer player)
    {
        if(player == null)
            return null;
        for(TowerPlayer tp : players)
        {
            if(tp.player.getUniqueId().equals(player.getUniqueId()))
                return tp;
        }
        return null;
    }


    private final OfflinePlayer player;
    private int points = 0;
    private int kills = 0;
    private int assists = 0;
    private int deaths = 0;
    private double money = 0d;

    private TowerPlayer(Player player)
    {
        this.player = Bukkit.getOfflinePlayer(player.getUniqueId());
    }

    public OfflinePlayer asOfflinePlayer()
    {
        return player;
    }

    public Player asPlayer()
    {
        return Bukkit.getPlayer(player.getUniqueId());
    }

    public int addPoint()
    {
        ScoreboardManager.BoardField.POINTS.update(asPlayer(), ++points);
        return points;
    }

    public int getPoints()
    {
        return points;
    }

    public int addKill()
    {
        ScoreboardManager.BoardField.KILLS.update(asPlayer(), ++kills);
        return kills;
    }

    public int getKills()
    {
        return kills;
    }

    public int addAssist()
    {
        ScoreboardManager.BoardField.ASSISTS.update(asPlayer(), ++assists);
        return assists;
    }

    public int getAssists()
    {
        return assists;
    }

    public int addDeath()
    {
        ScoreboardManager.BoardField.DEATHS.update(asPlayer(), ++deaths);
        return deaths;
    }

    public int getDeaths()
    {
        return deaths;
    }

    public double getMoney()
    {
        return money;
    }

    /**
     * Checks if a player has enough money to buy something
     * @param price the cost of the item he wants to buy
     * @return 0d if he <strong>CAN</strong> buy this item. <p>If anything else than 0d then it's the amount of money he needs to get before he can afford this item. <p>Example: I have 8$ and I want to buy a 10$ Sword then {@link #canBuy(double)} returns me 2.0$
     */
    public double canBuy(double price)
    {
        double delta = money - price;
        if(delta < 0)
        {
            return delta*-1;
        }
        return 0d;
    }

    /**
     * Adds the specified money amount to this player's bank account
     * @param money The amount of money to add
     * @return The new total amount of this player's bank account <strong>after</strong> the money got added.
     */
    public double giveMoney(double money)
    {
        this.money += money;
        ScoreboardManager.BoardField.MONEY.update(asPlayer(), this.money);
        return money;
    }

    /**
     * Removes the specified money amount to this player's bank account. Also check if he can afford the price.
     * @param money The amount of money to remove
     * @return The new total amount of this player's bank account <strong>after</strong> the money got removed. <p>If he cant afford the price then returns -1d
     */
    public double takeMoney(double money)
    {
        if(canBuy(money)!=0d)
        {
            return -1d;
        }
        this.money -= money;
        ScoreboardManager.BoardField.MONEY.update(asPlayer(), this.money);
        return money;
    }

    public PlayerBoard getScoreboard()
    {
        return Main.getInstance().getManager().getScoreboardManager().getBoard(asPlayer());
    }

    public PlayerTeam getTeam()
    {
        return TeamsManager.getPlayerTeam(asPlayer());
    }

    public void updateBoard()
    {
        getScoreboard().updateLines(
                Map.of(
                    ScoreboardManager.BoardField.POINTS.toFormat(), points,
                    ScoreboardManager.BoardField.KILLS.toFormat(), kills,
                    ScoreboardManager.BoardField.ASSISTS.toFormat(), assists,
                    ScoreboardManager.BoardField.DEATHS.toFormat(), deaths,
                    ScoreboardManager.BoardField.MONEY.toFormat(), money
                )
        );
    }
}
