package ch.tower.utils.Packets;

import ch.luca008.SpigotApi.Api.ReflectionApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.luca008.SpigotApi.Utils.WebRequest;
import ch.tower.managers.WorldManager;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutEntityHeadRotation;
import net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.EnumGamemode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.UUID;

public class EntityPackets
{

    public static PacketPlayOutPlayerInfo addNPC(EntityPlayer entity)
    {
        return new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, entity);
    }

    public static PacketPlayOutNamedEntitySpawn spawnNPC(EntityPlayer entity, Location location)
    {
        PacketPlayOutNamedEntitySpawn packet = new PacketPlayOutNamedEntitySpawn(entity);
        ReflectionApi a = SpigotApi.getReflectionApi();
        a.setField(packet, "c", location.getBlockX()+0.5f);
        a.setField(packet, "d", location.getBlockY()*1.0f);
        a.setField(packet, "e", location.getBlockZ()+0.5f);
        a.setField(packet, "f", (byte)90);
        a.setField(packet, "g", (byte)0);
        return packet;
    }

    public static PacketPlayOutPlayerInfo removeNPC(EntityPlayer entity)
    {
        return new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.e, entity);
    }

    public static PacketPlayOutEntityHeadRotation headRotation(EntityPlayer entity, int yaw)
    {
        return new PacketPlayOutEntityHeadRotation(entity, (byte) yaw);
    }

}
