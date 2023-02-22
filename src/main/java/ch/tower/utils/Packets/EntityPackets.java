package ch.tower.utils.Packets;

import ch.luca008.SpigotApi.Api.ReflectionApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.luca008.SpigotApi.Utils.WebRequest;
import ch.tower.managers.WorldManager;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
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
        entity.o(location.getYaw());
        entity.p(location.getPitch());
        PacketPlayOutNamedEntitySpawn packet = new PacketPlayOutNamedEntitySpawn(entity);
        ReflectionApi a = SpigotApi.getReflectionApi();
        a.setField(packet, "c", location.getX());
        a.setField(packet, "d", location.getY());
        a.setField(packet, "e", location.getZ());
        return packet;
    }

}
