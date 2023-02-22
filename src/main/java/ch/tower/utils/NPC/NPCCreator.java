package ch.tower.utils.NPC;

import ch.luca008.SpigotApi.Api.ReflectionApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.luca008.SpigotApi.Utils.WebRequest;
import ch.tower.utils.Packets.EntityPackets;
import ch.tower.utils.Packets.SpigotPlayer;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutNamedEntitySpawn;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.EnumGamemode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NPCCreator {

    private final Map<String, EntityPlayer> npc = new HashMap<>();

    private EntityPlayer createNPC(World world, String name, String skinPseudoOrUuid, String displayName)
    {
        GameProfile profile = new GameProfile(UUID.randomUUID(), name);
        if(skinPseudoOrUuid!=null&&skinPseudoOrUuid.length()>0){
            Property p = WebRequest.getSkin(skinPseudoOrUuid);
            if(p!=null){
                profile.getProperties().put("textures", p);
            }
        }
        ReflectionApi api = SpigotApi.getReflectionApi();
        MinecraftServer server = (net.minecraft.server.MinecraftServer)api.invoke(api.spigot().getOBCClass(null,"CraftServer"), Bukkit.getServer(), "getServer", new Class<?>[0]);
        WorldServer _world = (net.minecraft.server.level.WorldServer)api.invoke(api.spigot().getOBCClass(null,"CraftWorld"), world, "getHandle",new Class<?>[0]);
        EntityPlayer ep = new EntityPlayer(server, _world, profile, null);
        ep.listName = IChatBaseComponent.a(displayName);
        ep.e = 1;
        ep.d.a(EnumGamemode.b);
        return ep;
    }

    public boolean registerNPC(String uniqueName, String displayName, String skin, Location spawn)
    {
        if(this.npc.containsKey(uniqueName))
            return false;
        EntityPlayer npc = createNPC(spawn.getWorld(), uniqueName, skin, displayName);
        this.npc.put(uniqueName, npc);
        PacketPlayOutPlayerInfo packet1 = EntityPackets.addNPC(npc);
        PacketPlayOutNamedEntitySpawn packet2 = EntityPackets.spawnNPC(npc, spawn);
        for(Player player : spawn.getWorld().getPlayers())
        {
            SpigotPlayer.sendPacket(player, packet1);
            SpigotPlayer.sendPacket(player, packet2);
        }
        return true;
    }

}
