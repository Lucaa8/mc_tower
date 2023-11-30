package ch.tower.managers;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.luca008.SpigotApi.Api.NPCApi;
import ch.luca008.SpigotApi.Api.ReflectionApi;
import ch.luca008.SpigotApi.Packets.TeamsPackets;
import ch.luca008.SpigotApi.SpigotApi;
import ch.tower.Main;
import com.mojang.authlib.properties.Property;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.protocol.game.PacketPlayInUseEntity;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.scores.ScoreboardTeamBase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NPCManager {

    public interface NPCInteract
    {
        void click(NPCApi.NPC npc, Player player);
    }

    public static final File NPC_FILE = new File(Main.getInstance().getDataFolder(), "npc.json");

    private final Map<String, NPCApi.NPC> npc = new HashMap<>();
    private final Map<String, NPCInteract> callbacks = new HashMap<>();

    public NPCManager()
    {
        Bukkit.getServer().getPluginManager().registerEvents(new NPCListener(), Main.getInstance());
    }

    public static class NPCListener implements Listener
    {
        @EventHandler
        public void onJoinSendNPC(PlayerJoinEvent e)
        {
            Bukkit.getScheduler().runTaskLater(Main.getInstance(), ()->
            {
                SpigotApi.getMainApi().players().sendPacket(e.getPlayer(), TeamsPackets.createOrUpdateTeam("npc", TeamsPackets.Mode.CREATE, "NPC", false, false, ScoreboardTeamBase.EnumNameTagVisibility.a, ScoreboardTeamBase.EnumTeamPush.b, EnumChatFormat.o, "NPC | ", ""));
                for(NPCApi.NPC npc : Main.getInstance().getManager().getNpcManager().npc.values())
                {
                    send(e.getPlayer(), npc);
                }
            }, 5L);
        }

        @EventHandler
        public void onQuitUnregisterTeam(PlayerQuitEvent e)
        {
            SpigotApi.getMainApi().players().sendPacket(e.getPlayer(), TeamsPackets.deleteTeam("npc"));
            for(NPCApi.NPC npc : Main.getInstance().getManager().getNpcManager().npc.values())
            {
                npc.despawn(e.getPlayer());
            }
        }

        @EventHandler
        public void onJoinSniff(PlayerJoinEvent e)
        {
            SpigotApi.getMainApi().players().handlePacket(e.getPlayer(), (packet, event) ->{
                if(packet instanceof PacketPlayInUseEntity p)
                {
                    Object bEnumEntityUseAction = ReflectionApi.getField(packet, "b");
                    Object bInstance = ReflectionApi.invoke(bEnumEntityUseAction.getClass(), bEnumEntityUseAction, "a", new Class[0]);
                    Class<?> bEnum = ReflectionApi.getPrivateInnerClass(PacketPlayInUseEntity.class, "b");

                    Object ainteract = ReflectionApi.getEnumValue(bEnum, "INTERACT");
                    Object battack = ReflectionApi.getEnumValue(bEnum, "ATTACK");

                    if(bInstance == ainteract || bInstance == battack)
                    {
                        int id = (int) ReflectionApi.getField(p, "a");
                        NPCApi.NPC npc = Main.getInstance().getManager().getNpcManager().getNPC(id);
                        if(npc != null)
                        {
                            NPCInteract interact = Main.getInstance().getManager().getNpcManager().callbacks.get(npc.name);
                            if(interact != null)
                            {
                                Bukkit.getScheduler().runTask(Main.getInstance(), ()->interact.click(npc, Bukkit.getPlayer(e.getPlayer().getName())));
                            }
                            //Avoid the server handling this packet as this entity doesnt exists on the server (client-side npc)
                            event.setCancelled(true);
                        }
                    }
                }
            });
        }
    }

    @Nullable
    public NPCApi.NPC getNPC(String name)
    {
        return this.npc.get(name);
    }

    @Nullable
    public NPCApi.NPC getNPC(int entityId)
    {
        for(NPCApi.NPC npc : this.npc.values())
        {
            if(npc.bukkitId == entityId)
                return npc;
        }
        return null;
    }

    public NPCApi.NPC registerNPC(String uniqueName, Property skin, Location spawn, NPCApi.Directions facing, NPCInteract onClick)
    {
        if(!this.npc.containsKey(uniqueName))
        {
            EntityPlayer entity = SpigotApi.getNpcApi().createEntity(spawn.getWorld(), uniqueName, skin);
            NPCApi.NPC npc = new NPCApi.NPC(entity, spawn, facing, true);
            SpigotApi.getMainApi().players().sendPacket(Bukkit.getOnlinePlayers(), TeamsPackets.updateEntities("npc", TeamsPackets.Mode.ADD_ENTITY, npc.name));
            this.npc.put(uniqueName, npc);
            this.callbacks.put(uniqueName, onClick);
        }
        return this.npc.get(uniqueName);
    }

    public boolean unregisterNPC(String uniqueName)
    {
        this.callbacks.remove(uniqueName);
        NPCApi.NPC npc = this.npc.remove(uniqueName);
        if(npc != null)
        {
            npc.despawn();
            return true;
        }
        return false;
    }

    public void unregisterAll()
    {
        for(NPCApi.NPC npc : new ArrayList<>(this.npc.values()))
        {
            unregisterNPC(npc.name);
        }
        SpigotApi.getMainApi().players().sendPacket(Bukkit.getOnlinePlayers(), TeamsPackets.deleteTeam("npc"));
    }

    private static void send(Player player, NPCApi.NPC npc)
    {
        npc.spawn(player);
        SpigotApi.getMainApi().players().sendPacket(player, TeamsPackets.updateEntities("npc", TeamsPackets.Mode.ADD_ENTITY, npc.name));
    }

    public void load(){
        JSONApi.JSONReader r = SpigotApi.getJSONApi().readerFromFile(NPC_FILE);
        JSONArray jarr = r.getArray("NPC");
        for(Object o : jarr)
        {
            JSONApi.JSONReader rNpc = SpigotApi.getJSONApi().getReader((JSONObject) o);
            registerNPC(rNpc.getString("Name"),
                    propFromJson(rNpc.getJson("Textures")),
                    locFromJson(rNpc),
                    NPCApi.Directions.valueOf(rNpc.getString("Facing")),
                    Main.getInstance().getManager().getShopManager()::openShop);
        }
    }

    private Property propFromJson(JSONApi.JSONReader j)
    {
        if(!j.c("value") || !j.c("signature"))
            return null;
        return new Property("textures", j.getString("value"), j.getString("signature"));
    }

    private Location locFromJson(JSONApi.JSONReader j)
    {
        if(j.c("X") && j.c("Y") && j.c("Z") && j.c("Facing"))
            return new Location(Main.getInstance().getManager().getWorldManager().getTowerWorld(), j.getDouble("X"), j.getDouble("Y"), j.getDouble("Z"));
        return null;
    }

}
