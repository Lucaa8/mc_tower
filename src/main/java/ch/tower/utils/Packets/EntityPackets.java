package ch.tower.utils.Packets;

import ch.luca008.SpigotApi.Api.ReflectionApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.luca008.SpigotApi.Utils.WebRequest;
import ch.tower.managers.WorldManager;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
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

    public static PacketPlayOutNamedEntitySpawn spawnNPC(EntityPlayer entity, Location location, float yaw, float pitch)
    {
        PacketPlayOutNamedEntitySpawn packet = new PacketPlayOutNamedEntitySpawn(entity);
        ReflectionApi a = SpigotApi.getReflectionApi();
        a.setField(packet, "c", location.getBlockX()+0.5f);
        a.setField(packet, "d", location.getBlockY()*1.0f);
        a.setField(packet, "e", location.getBlockZ()+0.5f);
        a.setField(packet, "f", (byte)((int)(yaw*256.0F/360.0F)));
        a.setField(packet, "g", (byte)((int)(pitch*256.0F/360.0F)));
        return packet;
    }

    public static PacketPlayOutPlayerInfo removeNPC(EntityPlayer entity)
    {
        return new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.e, entity);
    }

    public static PacketPlayOutEntityDestroy destroyNPC(EntityPlayer entity)
    {
        return new PacketPlayOutEntityDestroy(entity.getBukkitEntity().getEntityId());
    }

    public static PacketPlayOutEntityHeadRotation headRotation(EntityPlayer entity, float yaw)
    {
        return new PacketPlayOutEntityHeadRotation(entity, (byte)((yaw%360)*256/360));
    }

    /**
     * Add the second layer to this entity's skin
     * @param entity The entity to update on the client
     * @param skinMask See index 17 on <a href="https://wiki.vg/Entity_metadata#Player">Player</a>
     * @return A packet which update this entity on the client
     */
    public static PacketPlayOutEntityMetadata updateSkin(EntityPlayer entity, byte skinMask)
    {
        DataWatcher d = entity.ai();
        d.b(new DataWatcherObject<>(17, DataWatcherRegistry.a), skinMask);
        return new PacketPlayOutEntityMetadata(entity.getBukkitEntity().getEntityId(), d, false, true);
    }

    public static PacketPlayOutEntity.PacketPlayOutEntityLook moveEntity(EntityPlayer entity, int yaw, int pitch)
    {
        return new PacketPlayOutEntity.PacketPlayOutEntityLook(entity.getBukkitEntity().getEntityId(), (byte)((int)(yaw*256.0F/360.0F)), (byte)((int)(pitch*256.0F/360.0F)), true);
    }

}
