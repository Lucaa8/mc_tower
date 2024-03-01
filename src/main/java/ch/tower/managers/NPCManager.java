package ch.tower.managers;

import ch.luca008.SpigotApi.Api.Events.NPCEvent;
import ch.luca008.SpigotApi.Api.JSONApi;
import ch.luca008.SpigotApi.Api.NPCApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.luca008.SpigotApi.Utils.ApiProperty;
import ch.luca008.SpigotApi.Utils.Logger;
import ch.tower.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;

public class NPCManager {

    public static final File NPC_FILE = new File(Main.getInstance().getDataFolder(), "npc.json");

    public NPCManager()
    {
        Bukkit.getServer().getPluginManager().registerEvents(new NPCListener(), Main.getInstance());
    }

    public static class NPCListener implements Listener
    {

        @EventHandler
        public void onNpcAction(NPCEvent e)
        {
            if(!SpigotApi.getTeamApi().isTeamRegistered("npc"))
            {
                Logger.error("Cannot spawn the NPC " + e.getNpc().getName() + " because the NPC team does not exist.", NPCListener.class.getName());
                return;
            }

            if(e.getType() == NPCEvent.NpcEventType.SPAWN)
            {
                Bukkit.getScheduler().runTaskLater(
                        Main.getInstance(),
                        ()-> SpigotApi.getMainApi().packets().teams().getAddEntityTeamPacket("npc", e.getNpc().getName()).send(e.getAffectedPlayers()),
                        10L //wait a little to be sure the NPC has been added
                );
            }
            else //DESPAWN
            {
                SpigotApi.getMainApi().packets().teams().getRemoveEntityTeamPacket("npc", e.getNpc().getName()).send(e.getAffectedPlayers());
            }
        }

    }

    public void load(){
        JSONApi.JSONReader r = SpigotApi.getJSONApi().readerFromFile(NPC_FILE);
        JSONArray jarr = r.getArray("NPC");
        for(Object o : jarr)
        {
            JSONApi.JSONReader rNpc = SpigotApi.getJSONApi().getReader((JSONObject) o);
            Location loc = locFromJson(rNpc);
            if(loc == null)
            {
                Logger.warn("Cannot spawn NPC with name " + rNpc.getString("Name") + " because his location was null.", NPCManager.class.getName());
                continue;
            }
            NPCApi.NPC npc = new NPCApi.NPC(null,
                    rNpc.getString("Name"),
                    propFromJson(rNpc.getJson("Textures")),
                    loc,
                    10.0,
                    true,
                    Main.getInstance().getManager().getShopManager()::openShop);
            SpigotApi.getNpcApi().registerNPC(npc);
        }
    }

    private ApiProperty propFromJson(JSONApi.JSONReader j)
    {
        if(!j.c("value") || !j.c("signature"))
            return null;
        return new ApiProperty("textures", j.getString("value"), j.getString("signature"));
    }

    private Location locFromJson(JSONApi.JSONReader j)
    {
        if(j.c("X") && j.c("Y") && j.c("Z") && j.c("Facing"))
            return new Location(Main.getInstance().getManager().getWorldManager().getTowerWorld(), j.getDouble("X"), j.getDouble("Y"), j.getDouble("Z"));
        return null;
    }

}
