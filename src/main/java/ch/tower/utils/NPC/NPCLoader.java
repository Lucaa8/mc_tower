package ch.tower.utils.NPC;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.tower.Main;
import com.mojang.authlib.properties.Property;
import org.bukkit.Location;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;

public class NPCLoader {

    public static final File NPC_FILE = new File(Main.getInstance().getDataFolder(), "npc.json");

    public static void load()
    {
        JSONApi.JSONReader r = SpigotApi.getJSONApi().readerFromFile(NPC_FILE);
        JSONArray jarr = r.getArray("NPC");
        for(Object o : jarr)
        {
            JSONApi.JSONReader rNpc = SpigotApi.getJSONApi().getReader((JSONObject) o);
            Main.getInstance().getManager().getNpcManager().registerNPC(rNpc.getString("Name"),
                                   propFromJson(rNpc.getJson("Textures")),
                                   locFromJson(rNpc),
                                   NPCCreator.Directions.valueOf(rNpc.getString("Facing")),
                                   Main.getInstance().getManager().getShopManager()::openShop);
        }
    }

    private static Property propFromJson(JSONApi.JSONReader j)
    {
        if(!j.c("value") || !j.c("signature"))
            return null;
        return new Property("textures", j.getString("value"), j.getString("signature"));
    }

    private static Location locFromJson(JSONApi.JSONReader j)
    {
        if(j.c("X") && j.c("Y") && j.c("Z") && j.c("Facing"))
            return new Location(Main.getInstance().getManager().getWorldManager().getTowerWorld(), j.getDouble("X"), j.getDouble("Y"), j.getDouble("Z"));
        return null;
    }

}
