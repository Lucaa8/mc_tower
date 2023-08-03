package ch.tower.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class GlobalEvents implements Listener {

    //Test event
    @EventHandler
    public void helloNewPlayer(PlayerJoinEvent e)
    {
        e.getPlayer().sendMessage("Welcome. The Tower Plugin is in function.");
    }

}
