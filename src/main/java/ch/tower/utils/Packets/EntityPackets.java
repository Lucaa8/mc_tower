package ch.tower.utils.Packets;

import ch.luca008.SpigotApi.Api.ReflectionApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.luca008.SpigotApi.Utils.WebRequest;
import ch.tower.managers.WorldManager;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutPlayerInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.EnumGamemode;
import org.bukkit.Bukkit;

import java.util.UUID;

public class EntityPackets
{

    public PacketPlayOutPlayerInfo addNPC(String name, String skinPseudoOrUuid, String displayName)
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
        WorldServer world = (net.minecraft.server.level.WorldServer)api.invoke(api.spigot().getOBCClass(null,"CraftWorld"), WorldManager.readLocation("Game.Spectator").getWorld(), "getHandle",new Class<?>[0]);
        EntityPlayer ep = new EntityPlayer(server, world, profile, null);
        ep.listName = IChatBaseComponent.a(displayName);
        ep.e = 1;
        ep.d.a(EnumGamemode.b);
        return new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, ep);
    }

}
