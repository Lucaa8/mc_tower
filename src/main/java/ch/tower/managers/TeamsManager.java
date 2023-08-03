package ch.tower.managers;

import ch.luca008.SpigotApi.Api.TeamAPI;
import ch.luca008.SpigotApi.SpigotApi;
import ch.tower.Main;
import net.minecraft.EnumChatFormat;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TeamsManager {

    private static final Map<String, TeamInfo> teams = new HashMap<>();

    public enum PlayerTeam
    {
        BLUE("§9"),
        RED("§c"),
        SPECTATOR("§7");

        private final TeamInfo team;
        private final String code;
        PlayerTeam(String code)
        {
            this.team = teams.get(this.name().toLowerCase());
            this.code = code;
        }

        public TeamInfo getInfo()
        {
            return team;
        }

        public String getColorCode()
        {
            return code;
        }

        public Location getSpawn()
        {
            GameManager.GameState gs = Main.getInstance().getManager().getState();
            if(gs == GameManager.GameState.WAIT)
            {
                return this.team.lobbySpawn;
            }
            else if(gs == GameManager.GameState.GAME)
            {
                return this.team.spawn;
            }
            //a random place just in case, but in other states than wait and game nobody can either die or join the server.
            return new Location(Bukkit.getWorld("world"), 0.5, 100, 0);
        }

        public boolean containsPlayer(Player player)
        {
            if(player == null)
                return false;
            return team.apiTeam().hasEntry(player.getName());
        }

        /**
         * Try to add the given player to this team.
         * @param player The bukkit player
         * @return true if the player was added, otherwise false. (Maybe false if the player was already in this team)
         */
        public boolean addPlayer(Player player)
        {
            PlayerTeam currentTeam = getPlayerTeam(player);
            if(currentTeam == this)
                return false;
            if(currentTeam != null)
            {
                currentTeam.removePlayer(player);
            }
            team.apiTeam().addEntries(player.getName());
            ScoreboardManager.BoardField.TEAM.update(player, ScoreboardManager.PlaceholderHelper.getTeamName(this));
            return true;
        }

        /**
         * Try to remove the given player of this team.
         * @param player The bukkit player
         * @return true if the player was removed, otherwise false. (Maybe false if the player wasn't in this team)
         */
        public boolean removePlayer(Player player)
        {
            if(!containsPlayer(player))
                return false;
            team.apiTeam().removeEntries(player.getName());
            ScoreboardManager.BoardField.TEAM.update(player, ScoreboardManager.PlaceholderHelper.getTeamName(null));
            return true;
        }
    }

    public static void unregisterTeams()
    {
        for(TeamInfo team : teams.values())
        {
            SpigotApi.getTeamApi().unregisterTeam(team.apiTeam());
        }
        teams.clear();
    }

    public static void registerTeams()
    {
        registerTeam("blue",      "Blue",       "Blue | ",      EnumChatFormat.j, 1);
        registerTeam("red",       "Red",        "Red | ",       EnumChatFormat.m, 2);
        registerTeam("spectator", "Spectators", "Spectator | ", EnumChatFormat.h, 3);
    }

    private static void registerTeam(String uniqueName, String displayName, String prefix, EnumChatFormat color, int sortOrder)
    {
        String teamName = uniqueName.substring(0,1).toUpperCase() + uniqueName.substring(1).toLowerCase();
        TeamAPI.Team apiTeam = new TeamAPI.TeamBuilder(uniqueName)
                .setDisplayName(displayName)
                .setPrefix(prefix)
                .setColor(color)
                .setSortOrder(sortOrder)
                .setFriendlyFire(false)
                .setSeeInvisibleFriends(true)
                .create();
        if(SpigotApi.getTeamApi().registerTeam(apiTeam)){
            TeamInfo team = new TeamInfo(apiTeam, WorldManager.readLocation("Game."+teamName), WorldManager.readLocation("Lobby."+teamName));
            teams.put(uniqueName, team);
        } else {
            System.err.println("THE API FAILED TO REGISTER THE TEAM " + uniqueName + ". MAYBE CONSIDER DISABLING THE PLUGIN BECAUSE HES NOT IN A STABLE STATE.");
        }
    }

    /**
     * Returns the current player's team (BLUE, RED, SPECTATOR)
     * @param player the bukkit player
     * @return The current player's team. Maybe null if the player doesn't have a current team (He didn't choose a team yet)
     */
    public static PlayerTeam getPlayerTeam(Player player)
    {
        for(PlayerTeam team : PlayerTeam.values())
        {
            if(team.containsPlayer(player))
                return team;
        }
        return null;
    }

    public record TeamInfo(TeamAPI.Team apiTeam, Location spawn, Location lobbySpawn) {

        public List<Player> getPlayers()
        {
            return apiTeam.getEntries().stream().filter(Objects::nonNull).map(Bukkit::getPlayer).filter(Objects::nonNull).toList();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TeamInfo team)) return false;
            return apiTeam.equals(team.apiTeam);
        }

        @Override
        public int hashCode() {
            return Objects.hash(apiTeam);
        }

    }

}
