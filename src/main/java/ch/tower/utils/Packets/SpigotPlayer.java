package ch.tower.utils.Packets;

import ch.luca008.SpigotApi.Api.ReflectionApi;
import ch.luca008.SpigotApi.SpigotApi;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.EntityPlayer;
import org.bukkit.entity.Player;

import java.util.Collection;

public class SpigotPlayer {

    private final static ReflectionApi r = SpigotApi.getReflectionApi();

    public static Object getConnection(Player player) {
        Class<?> craftplayer = r.spigot().getOBCClass("entity", "CraftPlayer");
        EntityPlayer ep = (EntityPlayer) SpigotApi.getReflectionApi().invoke(craftplayer, player, "getHandle", new Class[0], new Object[0]);
        return ep.b;
    }

    public static void sendPacket(Collection<? extends Player> collection, Packet<?> packet) {
        collection.forEach(player -> sendPacket(player, packet));
    }

    public static void sendPackets(Collection<? extends Player> collection, Packet<?>[] packets) {
        collection.forEach(player -> sendPackets(player, packets));
    }

    public static void sendPacket(Player player, Packet<?> packet) {
        NetworkManager nm = (NetworkManager) r.getField(getConnection(player), "b");
        //not working for versions < 1.18
        nm.a(packet);
    }

    public static void sendPackets(Player player, Packet<?>[] packets) {
        for (Packet<?> packet : packets) {
            sendPacket(player, packet);
        }
    }

}


