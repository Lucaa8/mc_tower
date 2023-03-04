package ch.tower.utils.NPC;

import ch.luca008.SpigotApi.Api.ReflectionApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.tower.utils.Packets.EntityPackets;
import ch.tower.utils.Packets.TeamsPackets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.level.EnumGamemode;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.util.UUID;

public class NPCCreator {

    public enum Directions
    {
        OTHER(0, 0, 0), NORTH(228, 180, 0), EAST(-138, -90, 0), WEST(138, 90, 0), SOUTH(0, 0, 0);

        private final int bodyYaw;
        private final int headYaw;
        private final int pitch;

        Directions(int bodyYaw, int headYaw, int pitch)
        {
            this.bodyYaw = bodyYaw;
            this.headYaw = headYaw;
            this.pitch = pitch;
        }

        public int getBodyYaw(){return bodyYaw;}
        public int getHeadYaw(){return headYaw;}
        public int getPitch(){return pitch;}

    }

    public interface NPCInteract
    {
        void click(NPC npc, Player player);
    }

    public static class NPC
    {
        private final EntityPlayer ep;
        private final UUID uuid;
        private final int id;
        private final String name;
        private Location loc;

        public NPC(EntityPlayer ep)
        {
            this.ep = ep;
            this.uuid = ep.fy().getId();
            this.id = ep.getBukkitEntity().getEntityId();
            this.name = ep.fy().getName();
            this.loc = new Location(ep.W().getWorld(),
                    (double) getField(Entity.class, ep, "t"),
                    (double) getField(Entity.class, ep, "u"),
                    (double) getField(Entity.class, ep, "v"),
                    (float) getField(Entity.class, ep, "aA"),
                    (float) getField(Entity.class, ep, "aB"));
        }

        public UUID getUuid() {
            return uuid;
        }

        public int getId() {
            return id;
        }

        public String getName()
        {
            return name;
        }

        public void setLocation(Location loc)
        {
            throw new NotImplementedException("Not implemented yet. B0t.");
        }

        public Location getLocation()
        {
            return loc;
        }

        public Packet<?>[] asPacket()
        {
            if(ep != null)
            {
                return new Packet[]
                        {
                                EntityPackets.addNPC(ep),
                                TeamsPackets.updateEntities("npc", TeamsPackets.Mode.ADD_ENTITY, this.name),
                                EntityPackets.spawnNPC(ep, this.getLocation(), this.getLocation().getYaw(), this.getLocation().getPitch()),
                                EntityPackets.headRotation(ep, (float) getField(EntityLiving.class, ep, "aZ")),
                                EntityPackets.removeNPC(ep)
                        };
            }
            return new Packet[0];
        }

    }

    public static EntityPlayer createNPC(World world, String name, Property skin)
    {
        GameProfile profile = new GameProfile(UUID.randomUUID(), name);
        if(skin != null){
            profile.getProperties().put("textures", skin);
        }
        ReflectionApi api = SpigotApi.getReflectionApi();
        MinecraftServer server = (net.minecraft.server.MinecraftServer)api.invoke(api.spigot().getOBCClass(null,"CraftServer"), Bukkit.getServer(), "getServer", new Class<?>[0]);
        WorldServer _world = (net.minecraft.server.level.WorldServer)api.invoke(api.spigot().getOBCClass(null,"CraftWorld"), world, "getHandle",new Class<?>[0]);
        EntityPlayer ep = new EntityPlayer(server, _world, profile, null);
        ep.listName = IChatBaseComponent.a(name);
        ep.e = 1;
        ep.d.a(EnumGamemode.b);
        return ep;
    }

    private static Object getField(Class<?> clazz, Object instance, String field)
    {
        Object value = null;
        try {
            Field f = clazz.getDeclaredField(field);
            f.setAccessible(true);
            value = f.get(instance);
            f.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Unknown field: " + field);
        }
        return value;
    }

}
