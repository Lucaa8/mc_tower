package ch.tower.managers;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.tower.Main;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;

import java.io.File;
import java.io.IOException;

public class WorldManager {

    public static final File SPAWN_FILE = new File(Main.getInstance().getDataFolder(), "spawns.json");

    private World spawn;
    private World tower;

    /**
     * Loads the Spawn map and the Tower map from the specified fields into spawns.json
     * @return true if both (spawn and tower) map got loaded successfully. Otherwise, false.
     */
    public boolean load()
    {
        JSONApi.JSONReader r = SpigotApi.getJSONApi().readerFromFile(SPAWN_FILE);
        File worlds = Main.getInstance().getServer().getWorldContainer();

        spawn = create(r.getJson("Lobby").getString("Map"));
        if(spawn == null)
        {
            return false;
        }

        File towerCopyMap = new File(worlds, r.getJson("Game").getString("Map_Copy"));
        if(!towerCopyMap.exists())
        {
            System.err.println("The original tower map specified under the 'Game.Map_Copy' field doesn't exist.");
            return false;
        }

        File towerMap = new File(worlds, r.getJson("Game").getString("Map"));
        //Delete old map if already exists.
        if(towerMap.exists())
        {
            try {
                FileUtils.deleteDirectory(towerMap);
            } catch (IOException e) {
                System.err.println("Old game world still exist but cannot be deleted.");
                e.printStackTrace();
                return false;
            }
        }
        try{
            FileUtils.copyDirectory(towerCopyMap, towerMap);
        }catch(IOException e){
            System.err.println("Can't copy original tower map...");
            return false;
        }

        tower = create(towerMap.getName());
        return tower != null;
    }

    public void unload()
    {
        if(spawn != null)
        {
            Bukkit.unloadWorld(spawn, false);
        }
        if(tower != null)
        {
            Bukkit.unloadWorld(tower, false);
            try {
                FileUtils.deleteDirectory(tower.getWorldFolder());
                System.out.println("Successfully unloaded and deleted game world!");
            } catch (IOException e) {
                System.err.println("Troubles while deleting game world folder. Maybe delete it manually.");
                e.printStackTrace();
            }
        }

    }

    private World create(String worldName)
    {
        World w = Bukkit.createWorld(WorldCreator.name(worldName));
        if(w!=null){
            w.setAutoSave(false);
            w.setTime(0);
            w.setClearWeatherDuration(100); //5 sec
            w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            w.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            w.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            w.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true); //useful for tower map
            System.out.println("Loaded world " + w.getName() + "!");
        }
        else
        {
            System.err.println("Failed to load world " + worldName + "!");
        }
        return w;
    }

    /**
     * Reads the file spawns.json to find a team's spawn in some game states.
     * @param key A field to access in the spawns.json. If you want the spawn position of the blue team during the game state: Game.Blue
     * @return The asked spawn location if found. Otherwise, returns x=0.5, y=60, z=0.5 in the first loaded bukkit world (not safe).
     */
    public static Location readLocation(String key){
        if(key.contains("."))
        {
            JSONApi.JSONReader r = SpigotApi.getJSONApi().readerFromFile(SPAWN_FILE);
            String[] keys = key.split("\\.");
            World world = Bukkit.getWorld(r.getJson(keys[0]).getString("Map"));
            if(world != null)
            {
                //maybe key = Game.Blue so we get the reader at Game.Spawns.Blue and we have x, y, z, etc...
                r = r.getJson(keys[0]).getJson("Spawns").getJson(keys[1]);
                if(r.c("x")&&r.c("y")&&r.c("z")){
                    Location l = new Location(world, r.getDouble("x"), r.getDouble("y"), r.getDouble("z"));
                    if(r.c("pitch")){
                        l.setPitch((float)r.getDouble("pitch"));
                    }
                    if(r.c("yaw")){
                        l.setYaw((float)r.getDouble("yaw"));
                    }
                    return l;
                }
            }
            else
            {
                System.err.println("The field at " + keys[0] + ".Map contains an unknown world.");
            }
        }
        return new Location(Bukkit.getWorlds().get(0), 0.5, 60, 0.5);
    }

}
