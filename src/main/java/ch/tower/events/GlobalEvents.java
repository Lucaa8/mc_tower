package ch.tower.events;

import ch.tower.Main;
import ch.tower.utils.Scoreboard.PlayerBoard;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GlobalEvents implements Listener {

    //Test event
    @EventHandler
    public void helloNewPlayer(PlayerJoinEvent e)
    {
        e.getPlayer().sendMessage("Welcome. The Tower Plugin is in function.");
    }

    @EventHandler
    public void destroyBoardOnQuit(PlayerQuitEvent e)
    {
        PlayerBoard pb = Main.getInstance().getManager().getScoreboardManager().getBoard(e.getPlayer());
        if(pb != null)
        {
            pb.setParentBoard(null);
        }
    }

}
