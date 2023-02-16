package ch.tower.utils.Packets;

import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardDisplayObjective;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardObjective;
import net.minecraft.network.protocol.game.PacketPlayOutScoreboardScore;
import net.minecraft.server.ScoreboardServer;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardObjective;
import net.minecraft.world.scores.criteria.IScoreboardCriteria;

public class ScoreboardPackets {

    public enum Mode {
        ADD(PacketPlayOutScoreboardObjective.a),
        REMOVE(PacketPlayOutScoreboardObjective.b),
        CHANGE(PacketPlayOutScoreboardObjective.c);

        private final int method;
        Mode(int method)
        {
            this.method = method;
        }
    }

    public enum Action
    {
        CHANGE(ScoreboardServer.Action.a),
        REMOVE(ScoreboardServer.Action.b);

        private final ScoreboardServer.Action action;
        Action(ScoreboardServer.Action action)
        {
            this.action = action;
        }
    }

    /**
     * Create, update or delete a client-side objective.
     * @param uniqueName a unique identifier for this objective. Will be used later on to display the objective and add scores to it.
     * @param displayName a title for this scoreboard. Maybe null if mode == Mode.REMOVE
     * @param mode a mode to tell the client what to do with this packet. See {@link Mode}
     * @return The requested packet
     */
    public static PacketPlayOutScoreboardObjective objective(String uniqueName, String displayName, Mode mode)
    {
        ScoreboardObjective obj = new ScoreboardObjective(new Scoreboard(), uniqueName, IScoreboardCriteria.a, IChatBaseComponent.a(displayName), IScoreboardCriteria.EnumScoreboardHealthDisplay.a);
        return new PacketPlayOutScoreboardObjective(obj, mode.method);
    }

    /**
     * Display or hide an existing objective.
     * @param uniqueName The unique objective name (not displayname)
     * @return The requested packet
     */
    public static PacketPlayOutScoreboardDisplayObjective displayObjective(String uniqueName)
    {
        ScoreboardObjective obj = null;
        if(uniqueName!=null)
            obj = new ScoreboardObjective(new Scoreboard(), uniqueName, IScoreboardCriteria.a, IChatBaseComponent.a(""), IScoreboardCriteria.EnumScoreboardHealthDisplay.a);
        return new PacketPlayOutScoreboardDisplayObjective(1, obj);
    }

    /**
     * Add or remove a record into an existing objective.
     * @param objectiveName16 The parent objective that will get this score line. Null when removing (Action.REMOVE)
     * @param lineText40 The text to display or remove
     * @param mode ADD this line or REMOVE it? See {@link Action}
     * @param score Basically, the line order
     * @return The requested packet
     */
    public static PacketPlayOutScoreboardScore score(String objectiveName16, String lineText40, Action mode, int score)
    {
        return new PacketPlayOutScoreboardScore(mode.action, objectiveName16, lineText40, score);
    }

}
