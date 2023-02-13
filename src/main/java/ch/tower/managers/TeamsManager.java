package ch.tower.managers;

import ch.tower.Main;
import ch.tower.utils.Packets.SpigotPlayer;
import ch.tower.utils.Packets.TeamsPackets;
import net.minecraft.network.protocol.Packet;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;
import java.util.stream.Collectors;

public class TeamsManager implements Listener {

    private static final Map<String, _Team> teams = new HashMap<>();
    private static final TeamsManager listener = new TeamsManager();

    public enum PlayerTeam
    {
        BLUE,
        RED,
        SPECTATOR;

        private final _Team team;
        PlayerTeam()
        {
            this.team = teams.get(this.name().toLowerCase());
        }

        public Team getInfo()
        {
            return new Team(this.team);
        }

        public Location getSpawn()
        {
            return this.team.spawn;
        }

        public boolean containsPlayer(Player player)
        {
            if(player == null)
                return false;
            return team.hasEntry(player.getName());
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
            team.addEntries(player.getName());
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
            team.removeEntries(player.getName());
            return true;
        }
    }

    public static void unregisterTeams()
    {
        for(_Team team : teams.values())
        {
            team.sendDeletePacket(Bukkit.getOnlinePlayers().toArray(new Player[0]));
        }
        teams.clear();
        HandlerList.unregisterAll(listener);
    }

    public static void registerTeams()
    {
        Main.getInstance().getServer().getPluginManager().registerEvents(listener, Main.getInstance());
        //SpigotApi.getJSONApi().readerFromFile(GameManager.SPAWN_FILE).getJson("Game");
        registerTeam("blue",      "Blue team",  "Blue | ",      TeamsPackets.TeamColor.BLUE, 1, new Location(Bukkit.getWorld("Tower"), 0, 0, 0));
        registerTeam("red",       "Red team",   "Red | ",       TeamsPackets.TeamColor.RED,  2, new Location(Bukkit.getWorld("Tower"), 0, 0, 0));
        registerTeam("spectator", "Spectators", "Spectator | ", TeamsPackets.TeamColor.GRAY, 3, new Location(Bukkit.getWorld("Tower"), 0, 0, 0));
    }

    private static void registerTeam(String uniqueName, String displayName, String prefix, TeamsPackets.TeamColor color, int sortOrder, Location spawn)
    {
        _Team team = new _Team(uniqueName,
                         displayName,
                         sortOrder,
                         prefix,
                         "",
                         color,
                         TeamsPackets.NameTagVisibility.ALWAYS,
                         TeamsPackets.TeamPush.NEVER,
                         false,
                         true,
                         new ArrayList<>(),
                         spawn);
        teams.put(uniqueName, team);
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

    @EventHandler
    public void onJoinRegisterTeams(PlayerJoinEvent e)
    {
        Player p = e.getPlayer();
        for(_Team team : teams.values())
        {
            team.sendCreatePacket(p);
        }
    }

    @EventHandler
    public void onLeaveUnregisterTeams(PlayerQuitEvent e)
    {
        Player p = e.getPlayer();
        for(_Team team : teams.values())
        {
            team.sendDeletePacket(p);
        }
    }

    public static class Team {

        private final _Team team;

        private Team(_Team team)
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

        public TeamsPackets.TeamColor getColor()
        {
            return team.getColor();
        }

        public List<Player> getPlayers()
        {
            return team.getEntries().stream().filter(Objects::nonNull).map(Bukkit::getPlayer).filter(Objects::nonNull).toList();
        }
    }

    private static class _Team
    {
        private final String uniqueName;

        private final String displayName;

        private final int sortOrder;

        private String prefix;

        private String suffix;

        private TeamsPackets.TeamColor color;

        private final TeamsPackets.NameTagVisibility nameTagVisibility;

        private final TeamsPackets.TeamPush collisions;

        private final boolean friendlyFire;

        private final boolean seeInvisibleFriends;

        private final ArrayList<String> entries;

        private final Location spawn;

        private _Team(String uniqueName, String displayName, int sortOrder, String prefix, String suffix, TeamsPackets.TeamColor color, TeamsPackets.NameTagVisibility nameTagVisibility, TeamsPackets.TeamPush collisions, boolean friendlyFire, boolean seeInvisibleFriends, ArrayList<String> entries, Location spawn)
        {
            this.uniqueName = uniqueName;
            this.displayName = displayName;
            this.sortOrder = sortOrder;
            this.prefix = prefix;
            this.suffix = suffix;
            this.color = color;
            this.nameTagVisibility = nameTagVisibility;
            this.collisions = collisions;
            this.friendlyFire = friendlyFire;
            this.seeInvisibleFriends = seeInvisibleFriends;
            this.entries = entries;
            this.spawn = spawn;
        }

        public String getReelUniqueName()
        {
            if(hasSortOrder())return String.format("%02d", getSortOrder())+getUniqueName();
            return getUniqueName();
        }

        public String getUniqueName()
        {
            return uniqueName;
        }

        public String getDisplayName()
        {
            return displayName;
        }

        public boolean hasSortOrder()
        {
            return sortOrder>=0;
        }

        public int getSortOrder()
        {
            return sortOrder;
        }

        public String getPrefix()
        {
            return prefix;
        }

        public String getSuffix()
        {
            return suffix;
        }

        public TeamsPackets.TeamColor getColor()
        {
            return color;
        }

        public TeamsPackets.NameTagVisibility getNameTagVisibility()
        {
            return nameTagVisibility;
        }

        public TeamsPackets.TeamPush getCollisions()
        {
            return collisions;
        }

        public boolean isFriendlyFireAllowed()
        {
            return friendlyFire;
        }

        public boolean canSeeInvisibleFriends()
        {
            return seeInvisibleFriends;
        }

        public boolean hasEntry(String entry)
        {
            return getEntries().contains(entry);
        }

        public ArrayList<String> getEntries()
        {
            return entries;
        }

        public Location getSpawn()
        {
            return spawn;
        }

        public void setPrefix(String prefix, boolean sendUpdate)
        {
            this.prefix = prefix;
            if(sendUpdate)
            {
                update();
            }
        }

        public void setSuffix(String suffix, boolean sendUpdate)
        {
            this.suffix = suffix;
            if(sendUpdate)
            {
                update();
            }
        }

        public void setColor(TeamsPackets.TeamColor color, boolean sendUpdate)
        {
            this.color = color;
            if(sendUpdate)
            {
                update();
            }
        }

        public void addEntries(String...entries)
        {
            List<String> toAddList = new ArrayList<>();
            for(String entry : entries)
            {
                if(!hasEntry(entry))
                {
                    this.entries.add(entry);
                    toAddList.add(entry);
                }
            }
            if(!toAddList.isEmpty())
            {
                updateEntries(TeamsPackets.Mode.ADD_ENTITY, toAddList);
            }
        }

        public void removeEntries(String...entries)
        {
            List<String> toRemoveList = new ArrayList<>();
            for(String entry : entries)
            {
                if(hasEntry(entry))
                {
                    this.entries.remove(entry);
                    toRemoveList.add(entry);
                }
            }
            if(!toRemoveList.isEmpty())
            {
                updateEntries(TeamsPackets.Mode.REMOVE_ENTITY, toRemoveList);
            }
        }

        private void update()
        {
            Packet<?> updatePacket = TeamsPackets.createOrUpdateTeam(getReelUniqueName(), TeamsPackets.Mode.UPDATE, getDisplayName(), isFriendlyFireAllowed(), canSeeInvisibleFriends(), getNameTagVisibility(), getCollisions(), getColor(), getPrefix(), getSuffix());
            SpigotPlayer.sendPacket(Bukkit.getOnlinePlayers(), updatePacket);
        }

        private void updateEntries(TeamsPackets.Mode action, List<String> entries)
        {
            Packet<?> updatePacket = TeamsPackets.updateEntities(getReelUniqueName(), action, entries.toArray(new String[0]));
            if(updatePacket!=null){
                SpigotPlayer.sendPacket(Bukkit.getOnlinePlayers(), updatePacket);
            }
        }

        public void sendCreatePacket(Player...players)
        {
            Collection<Player> pls = Arrays.stream(players).collect(Collectors.toList());
            Packet<?> createPacket = TeamsPackets.createOrUpdateTeam(getReelUniqueName(), TeamsPackets.Mode.CREATE, getDisplayName(), isFriendlyFireAllowed(), canSeeInvisibleFriends(), getNameTagVisibility(), getCollisions(), getColor(), getPrefix(), getSuffix(), entries.toArray(new String[0]));
            SpigotPlayer.sendPacket(pls, createPacket);
        }

        public void sendDeletePacket(Player...players)
        {
            Collection<Player> pls = Arrays.stream(players).collect(Collectors.toList());
            Packet<?> deletePacket = TeamsPackets.deleteTeam(getReelUniqueName());
            SpigotPlayer.sendPacket(pls, deletePacket);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (!(o instanceof _Team team)) return false;
            return uniqueName.equals(team.uniqueName);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(uniqueName);
        }
    }

}
