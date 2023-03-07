package ch.tower.managers;

import ch.luca008.SpigotApi.Api.ReflectionApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.tower.Main;
import ch.tower.utils.NPC.NPCCreator;
import ch.tower.utils.Packets.EntityPackets;
import ch.tower.utils.Packets.SpigotPlayer;
import ch.tower.utils.Packets.TeamsPackets;
import com.mojang.authlib.properties.Property;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayInUseEntity;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NPCManager {

    private final Map<String, EntityPlayer> npc = new HashMap<>();
    private final Map<String, NPCCreator.NPCInteract> callbacks = new HashMap<>();

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
                SpigotPlayer.sendPacket(e.getPlayer(), TeamsPackets.createOrUpdateTeam("npc", TeamsPackets.Mode.CREATE, "NPC", false, false, TeamsPackets.NameTagVisibility.ALWAYS, TeamsPackets.TeamPush.NEVER, TeamsPackets.TeamColor.YELLOW, "NPC | ", ""));
                for(EntityPlayer ep : Main.getInstance().getManager().getNpcManager().npc.values())
                {
                    send(e.getPlayer(), new NPCCreator.NPC(ep).asPacket());
                }
            }, 5L);
        }

        @EventHandler
        public void onQuitUnregisterTeam(PlayerQuitEvent e)
        {
            SpigotPlayer.sendPacket(e.getPlayer(), TeamsPackets.deleteTeam("npc"));
        }

        @EventHandler
        public void onJoinSniff(PlayerJoinEvent e)
        {
            ReflectionApi r = SpigotApi.getReflectionApi();
            Class<?> craftplayer = r.spigot().getOBCClass("entity", "CraftPlayer");
            EntityPlayer ep = (EntityPlayer) r.invoke(craftplayer, e.getPlayer(), "getHandle", new Class[0]);
            final String player = e.getPlayer().getName();
            ep.b.b.m.pipeline().addBefore("packet_handler", e.getPlayer().getName(), new ChannelDuplexHandler(){
                public void channelRead(ChannelHandlerContext channelHandlerContext, Object packet) throws Exception {
                    if(packet instanceof PacketPlayInUseEntity p)
                    {
                        if(p.getActionType() == PacketPlayInUseEntity.b.b)
                        {
                            NPCCreator.NPC npc = Main.getInstance().getManager().getNpcManager().getNPC(p.getEntityId());
                            if(npc != null)
                            {
                                NPCCreator.NPCInteract interact = Main.getInstance().getManager().getNpcManager().callbacks.get(npc.getName());
                                if(interact != null)
                                {
                                    interact.click(npc, Bukkit.getPlayer(player));
                                }
                                return;
                            }
                        }
                    }
                    super.channelRead(channelHandlerContext, packet);
                }
            });
        }

        @EventHandler
        public void onQuitUnsniff(PlayerQuitEvent e)
        {
            ReflectionApi r = SpigotApi.getReflectionApi();
            Class<?> craftplayer = r.spigot().getOBCClass("entity", "CraftPlayer");
            EntityPlayer ep = (EntityPlayer) r.invoke(craftplayer, e.getPlayer(), "getHandle", new Class[0]);
            Channel channel = ep.b.b.m;
            EventLoop loop = channel.eventLoop();
            loop.submit(() -> {
                channel.pipeline().remove(e.getPlayer().getName());
                return null;
            });
        }
    }

    public NPCCreator.NPC getNPC(String name)
    {
        EntityPlayer ep = this.npc.get(name);
        if(ep != null)
        {
            return new NPCCreator.NPC(ep);
        }
        return null;
    }

    public NPCCreator.NPC getNPC(int entityid)
    {
        for(EntityPlayer ep : this.npc.values())
        {
            if(ep.getBukkitEntity().getEntityId() == entityid)
                return new NPCCreator.NPC(ep);
        }
        return null;
    }

    public NPCCreator.NPC registerNPC(String uniqueName, Property skin, Location spawn, NPCCreator.Directions facing, NPCCreator.NPCInteract onClick)
    {
        if(!this.npc.containsKey(uniqueName))
        {
            EntityPlayer npc = NPCCreator.createNPC(spawn.getWorld(), uniqueName, skin);
            setField(Entity.class, npc, "t", spawn.getX());
            setField(Entity.class, npc, "u", spawn.getY());
            setField(Entity.class, npc, "v", spawn.getZ());
            setField(Entity.class, npc, "aA", facing == NPCCreator.Directions.OTHER ? spawn.getYaw() : facing.getBodyYaw());
            setField(Entity.class, npc, "aB", facing == NPCCreator.Directions.OTHER ? spawn.getPitch() : facing.getPitch());
            setField(EntityLiving.class, npc, "aZ", facing.getHeadYaw());
            this.npc.put(uniqueName, npc);
            this.callbacks.put(uniqueName, onClick);
            Packet<?>[] packets = new NPCCreator.NPC(npc).asPacket();
            for(Player player : Bukkit.getOnlinePlayers())
            {
                send(player, packets);
            }
        }
        return new NPCCreator.NPC(this.npc.get(uniqueName));
    }

    public boolean unregisterNPC(String uniqueName)
    {
        this.callbacks.remove(uniqueName);
        EntityPlayer ep = this.npc.remove(uniqueName);
        if(ep != null)
        {
            PacketPlayOutEntityDestroy packet = EntityPackets.destroyNPC(ep);
            for(Player p : Bukkit.getOnlinePlayers())
            {
                SpigotPlayer.sendPacket(p, packet);
            }
            return true;
        }
        return false;
    }

    public void unregisterAll()
    {
        for(EntityPlayer ep : new ArrayList<>(this.npc.values()))
        {
            unregisterNPC(ep.fy().getName());
        }
        for(Player p : Bukkit.getOnlinePlayers())
        {
            SpigotPlayer.sendPacket(p, TeamsPackets.deleteTeam("npc"));
        }
    }

    private static void send(Player player, Packet<?>[] packets)
    {
        for (int i = 0; i < packets.length-1; i++)
        {
            SpigotPlayer.sendPacket(player, packets[i]);
        }
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), ()->SpigotPlayer.sendPacket(player, packets[packets.length-1]), 5L);
    }

    private static void setField(Class<?> clazz, Object instance, String field, Object value)
    {
        try {
            Field f = clazz.getDeclaredField(field);
            f.setAccessible(true);
            f.set(instance, value);
            f.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Unknown field: " + field);
        }
    }

}
