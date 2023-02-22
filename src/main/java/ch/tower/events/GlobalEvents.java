package ch.tower.events;

import ch.luca008.SpigotApi.SpigotApi;
import ch.tower.Main;
import ch.tower.utils.NPC.NPCCreator;
import ch.tower.utils.Packets.EntityPackets;
import ch.tower.utils.Packets.SpigotPlayer;
import ch.tower.utils.Scoreboard.PlayerBoard;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
        final Location spawnNpc = new Location(Bukkit.getWorld("Spawn"), 6, 50, 0);
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), ()->
                new NPCCreator().registerNPC("testNPC", "Shop", "Lord_Tigrou", spawnNpc),
                20L);
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
