package ch.tower.managers;

import ch.luca008.SpigotApi.Api.TeamAPI;
import ch.luca008.SpigotApi.Packets.PacketsUtils;
import ch.luca008.SpigotApi.Packets.TeamsPackets;
import ch.luca008.SpigotApi.SpigotApi;
import ch.luca008.SpigotApi.Utils.Logger;
import ch.tower.Main;
import ch.tower.managers.WorldManager.WorldZone;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
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
        private int points;
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
            else if(gs == GameManager.GameState.GAME || gs == GameManager.GameState.END)
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

        public int addPointAndGet()
        {
            return ++points;
        }

        public int getPoints()
        {
            return points;
        }

    }

    public static void unregisterTeams()
    {
        for(TeamInfo team : teams.values())
        {
            SpigotApi.getTeamApi().unregisterTeam(team.apiTeam());
        }
        teams.clear();
        SpigotApi.getTeamApi().unregisterTeam("npc");
    }

    public static void registerTeams(World towerWorld)
    {
        registerTeam(towerWorld, "blue",      "Blue",       "Blue | ",     PacketsUtils.ChatColor.BLUE, 1);
        registerTeam(towerWorld, "red",       "Red",        "Red | ",      PacketsUtils.ChatColor.RED,  2);
        registerTeam(towerWorld, "spectator", "Spectator", "Spectator | ", PacketsUtils.ChatColor.GRAY, 3);
        TeamAPI.Team npcTeam = new TeamAPI.TeamBuilder("npc")
                .setDisplayName("NPC")
                .setPrefix("NPC | ")
                .setColor(PacketsUtils.ChatColor.YELLOW)
                .setCollisions(TeamsPackets.Collisions.ALWAYS) //So players cant hide inside NPCs
                .create();
        if(!SpigotApi.getTeamApi().registerTeam(npcTeam))
        {
            Logger.error("THE API FAILED TO REGISTER THE npc TEAM. MAYBE CONSIDER DISABLING THE PLUGIN BECAUSE HES NOT IN A STABLE STATE.", TeamsManager.class.getName());
        }
    }

    private static void registerTeam(World towerWorld, String uniqueName, String displayName, String prefix, PacketsUtils.ChatColor color, int sortOrder)
    {
        TeamAPI.Team apiTeam = new TeamAPI.TeamBuilder(uniqueName)
                .setDisplayName(displayName)
                .setPrefix(prefix)
                .setColor(color)
                .setSortOrder(sortOrder)
                .setCollisions(TeamsPackets.Collisions.NEVER)
                .create();
        if(SpigotApi.getTeamApi().registerTeam(apiTeam)){
            WorldZone teamPool = null;
            WorldZone spawnProtection = null;
            if(!displayName.equals("Spectator"))
            {
                teamPool = WorldZone.readFromFile(WorldManager.POOL_FILE, displayName, towerWorld);
                spawnProtection = WorldZone.readFromFile(WorldManager.SPAWN_FILE, "Game.SpawnsProtection."+displayName, towerWorld);
            }
            TeamInfo team = new TeamInfo(apiTeam, WorldManager.readLocation("Game."+displayName), WorldManager.readLocation("Lobby."+displayName), teamPool, spawnProtection);
            teams.put(uniqueName, team);
        } else {
            Logger.error("THE API FAILED TO REGISTER THE TEAM " + uniqueName + ". MAYBE CONSIDER DISABLING THE PLUGIN BECAUSE HES NOT IN A STABLE STATE.", TeamsManager.class.getName());
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

    public record TeamInfo(TeamAPI.Team apiTeam, Location spawn, Location lobbySpawn, @Nullable WorldZone pool, @Nullable WorldZone spawnProtection) {

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
