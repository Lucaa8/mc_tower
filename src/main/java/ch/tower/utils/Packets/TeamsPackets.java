package ch.tower.utils.Packets;

import ch.luca008.SpigotApi.Api.ReflectionApi;
import ch.luca008.SpigotApi.SpigotApi;
import net.minecraft.EnumChatFormat;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardTeam;
import java.util.*;

public class TeamsPackets
{

    public enum Mode
    {
        CREATE(0),
        DELETE(1),
        UPDATE(2),
        ADD_ENTITY(3),
        REMOVE_ENTITY(4);

        private final int mode;

        Mode(int mode)
        {
            this.mode = mode;
        }

        public int getMode()
        {
            return this.mode;
        }
    }


    public enum NameTagVisibility
    {
        ALWAYS("always"),
        NEVER("never"),
        HIDE_FOR_OTHER_TEAMS("hideForOtherTeams"),
        HIDE_FOR_OWN_TEAM("hideForOwnTeam");

        private final String e;

        NameTagVisibility(String type)
        {
            this.e = type;
        }
    }

    public enum TeamPush
    {
        ALWAYS("always"),
        NEVER("never"),
        PUSH_OTHER_TEAMS("pushOtherTeams"),
        PUSH_OWN_TEAM("pushOwnTeam");

        private final String e;

        TeamPush(String type)
        {
            this.e = type;
        }
    }

    public enum TeamColor
    {
        BLACK("BLACK", '0'),
        DARK_BLUE("DARK_BLUE", '1'),
        DARK_GREEN("DARK_GREEN", '2'),
        DARK_AQUA("DARK_AQUA", '3'),
        DARK_RED("DARK_RED", '4'),
        DARK_PURPLE("DARK_PURPLE", '5'),
        GOLD("GOLD", '6'),
        GRAY("GRAY", '7'),
        DARK_GRAY("DARK_GRAY", '8'),
        BLUE("BLUE", '9'),
        GREEN("GREEN", 'a'),
        AQUA("AQUA", 'b'),
        RED("RED", 'c'),
        LIGHT_PURPLE("LIGHT_PURPLE", 'd'),
        YELLOW("YELLOW", 'e'),
        WHITE("WHITE", 'f'),
        OBFUSCATED("OBFUSCATED", 'k'),
        BOLD("BOLD", 'l'),
        STRIKETHROUGH("STRIKETHROUGH", 'm'),
        UNDERLINE("UNDERLINE", 'n'),
        ITALIC("ITALIC", 'o'),
        RESET("RESET", 'r');

        private final String name;
        private final char code;

        TeamColor(String name, char code)
        {
            this.name = name;
            this.code = code;
        }

        public static TeamColor fromCode(char code)
        {
            for (TeamColor c : values())
            {
                if (code == c.code)
                    return c;
            }
            return RESET;
        }

        public char getCode()
        {
            return this.code;
        }
    }

    private static PacketPlayOutScoreboardTeam packet(String uniqueName,
                                                      Mode mode,
                                                      String displayName,
                                                      boolean friendlyFire,
                                                      boolean seeInvisibleFriendly,
                                                      NameTagVisibility nameTagVisibility,
                                                      TeamPush teamPush,
                                                      TeamColor teamColor,
                                                      String prefix,
                                                      String suffix,
                                                      String...entities)
    {
        ReflectionApi r = SpigotApi.getReflectionApi();
        PacketPlayOutScoreboardTeam.b team = null;
        if(mode == Mode.CREATE || mode == Mode.UPDATE)
        {
            team = new PacketPlayOutScoreboardTeam.b(new ScoreboardTeam(new Scoreboard(), ""));
            r.setField(team, "a", IChatBaseComponent.a(displayName));
            r.setField(team, "b", IChatBaseComponent.a(prefix));
            r.setField(team, "c", IChatBaseComponent.a(suffix));
            r.setField(team, "d", nameTagVisibility.e);
            r.setField(team, "e", teamPush.e);
            r.setField(team, "f", EnumChatFormat.valueOf(teamColor.name));
            int g = 0;
            if (friendlyFire)
                g |= 0x1;
            if (seeInvisibleFriendly)
                g |= 0x2;
            r.setField(team, "g", g);
        }
        return (PacketPlayOutScoreboardTeam)r.newInstance(PacketPlayOutScoreboardTeam.class, new Class[]{String.class, int.class, Optional.class, Collection.class}, uniqueName, mode.mode, Optional.ofNullable(team), List.of(entities));
    }

    public static PacketPlayOutScoreboardTeam createOrUpdateTeam(String uniqueName,
                                                     Mode mode,
                                                     String displayName,
                                                     boolean friendlyFire,
                                                     boolean seeInvisibleFriendly,
                                                     NameTagVisibility nameTagVisibility,
                                                     TeamPush teamPush,
                                                     TeamColor teamColor,
                                                     String prefix,
                                                     String suffix,
                                                     String...entities)
    {
        return packet(uniqueName, (mode == Mode.CREATE || mode == Mode.UPDATE) ? mode : Mode.CREATE, displayName, friendlyFire, seeInvisibleFriendly, nameTagVisibility, teamPush, teamColor, prefix, suffix, entities);
    }

    public static PacketPlayOutScoreboardTeam deleteTeam(String uniqueName)
    {
        return packet(uniqueName, Mode.DELETE, "", false, false, null, null, null, "", "");
    }

    public static PacketPlayOutScoreboardTeam updateEntities(String uniqueName, Mode mode, String...entities)
    {
        if(mode == Mode.ADD_ENTITY || mode == Mode.REMOVE_ENTITY)
            return packet(uniqueName, mode, "", false, false, null, null, null, "", "", entities);
        return null;
    }

}
