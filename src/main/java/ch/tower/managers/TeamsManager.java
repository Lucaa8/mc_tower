package ch.tower.managers;

import ch.luca008.SpigotApi.APIPlayer;
import ch.luca008.SpigotApi.Api.TeamAPI;
import ch.luca008.SpigotApi.Packets.ScoreboardTeam;
import ch.luca008.SpigotApi.SpigotApi;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class TeamsManager {

    // TODO: 13/02/2023 find spawn location and put them into a spawn.json file
    private static final Location BLUE_SPAWN = new Location(Bukkit.getWorld("Tower"), 0, 0, 0);
    private static final Location RED_SPAWN = new Location(Bukkit.getWorld("Tower"), 0, 0, 0);
    private static final Location SPEC_SPAWN = new Location(Bukkit.getWorld("Tower"), 0, 0, 0);

    public enum PlayerTeam
    {
        BLUE(BLUE_SPAWN),
        RED(RED_SPAWN),
        SPECTATOR(SPEC_SPAWN);



        private final TeamAPI.Team team;
        private final Location spawn;
        PlayerTeam(Location spawn)
        {
            this.team = SpigotApi.getTeamApi().getTeam(this.name().toLowerCase());
            this.spawn = spawn;
        }

        public Team getInfo()
        {
            return new Team(this.team);
        }

        public Location getSpawn()
        {
            return this.spawn;
        }

        public boolean containsPlayer(Player player)
        {
            APIPlayer apiPlayer = SpigotApi.getPlayer(player.getUniqueId());
            return apiPlayer != null && apiPlayer.getTeam() == this.team;
        }

        /**
         * Try to add the given player to this team.
         * @param player The bukkit player
         * @return true if the player was added, otherwise false. (Maybe false if the player was already in this team)
         */
        public boolean addPlayer(Player player)
        {
            if(containsPlayer(player))
                return false;
            APIPlayer apiPlayer = SpigotApi.getPlayer(player.getUniqueId());
            if(apiPlayer == null)
                return false;
            //force: true means that the player will be moved from his current team if he had one.
            return apiPlayer.setTeam(this.team.getUniqueName(), true);
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
            APIPlayer apiPlayer = SpigotApi.getPlayer(player.getUniqueId());
            if(apiPlayer == null)
                return false;
            //bruh I forgot to implement setTeam(none) to remove a player from his team ^^
            // TODO: 13/02/2023 implements method to remove a player from his team in spigotapi
            return SpigotApi.getTeamApi().removePlayer(this.team.getUniqueName()); //tmp
        }
    }

    public static void registerTeams()
    {
        registerTeam("blue", "Blue team", ScoreboardTeam.TeamColor.BLUE, 1);
        registerTeam("red", "Red team", ScoreboardTeam.TeamColor.RED, 2);
        registerTeam("spectator", "Spectators", ScoreboardTeam.TeamColor.GRAY, 3);
    }

    private static void registerTeam(String uniqueName, String displayName, ScoreboardTeam.TeamColor color, int sortOrder)
    {
        SpigotApi.getTeamApi().registerTeam(
                new TeamAPI.TeamBuilder(uniqueName)
                        .setDisplayName(displayName)
                        .setCollisions(ScoreboardTeam.TeamPush.NEVER)
                        .setFriendlyFire(false)
                        .setColor(color)
                        .setSortOrder(sortOrder)
                        .create());
    }

    public static void unregisterTeams()
    {
        TeamAPI teams = SpigotApi.getTeamApi();
        for(PlayerTeam team : PlayerTeam.values())
        {
            teams.unregisterTeam(team.team);
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

    public static class Team {

        private final TeamAPI.Team team;

        private Team(TeamAPI.Team team)
        {
            this.team = team;
        }

        public String getName()
        {
            return team.getDisplayName();
        }

        public String getPrefix()
        {
            return team.getPrefix();
        }

        public String getSuffix()
        {
            return team.getSuffix();
        }

        public ScoreboardTeam.TeamColor getColor()
        {
            return team.getColor();
        }

        public List<Player> getPlayers()
        {
            return team.getEntries().stream().filter(Objects::nonNull).map(Bukkit::getPlayer).filter(Objects::nonNull).toList();
        }
    }

}
