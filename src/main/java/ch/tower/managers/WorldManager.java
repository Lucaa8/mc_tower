package ch.tower.managers;

import ch.luca008.SpigotApi.Api.JSONApi;
import ch.luca008.SpigotApi.SpigotApi;
import ch.tower.Main;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

public class WorldManager {

    public static final File SPAWN_FILE = new File(Main.getInstance().getDataFolder(), "spawns.json");
    public static final File POOL_FILE = new File(Main.getInstance().getDataFolder(), "pools.json");

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
            w.setTime(12000);
            w.setClearWeatherDuration(100); //5 sec
            w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            w.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
            w.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            w.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true); //useful for tower map
            w.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
            w.setGameRule(GameRule.DISABLE_RAIDS, true);
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

    public World getSpawnWorld()
    {
        return spawn;
    }

    public World getTowerWorld()
    {
        return tower;
    }

    public record WorldZone(World world, double[] x, double[] y, double[] z) {

        private static double[][] locations(JSONApi.JSONReader json)
        {
            return new double[][]{
                    {json.getDouble("X1"), json.getDouble("X2")},
                    {json.getDouble("Y1"), json.getDouble("Y2")},
                    {json.getDouble("Z1"), json.getDouble("Z2")}
            };
        }


        //If the key is null or empty it means we return the base JSON Object of the file and we do not go down
        private static JSONApi.JSONReader jsonFromKey(File file, @Nullable String key)
        {
            JSONApi.JSONReader r = SpigotApi.getJSONApi().readerFromFile(file);
            if(key == null || key.equals(""))
            {
                return r;
            }
            if(key.contains("."))
            {
                for(String k : key.split("\\."))
                {
                    r = r.getJson(k);
                }
            } else {
                r = r.getJson(key);
            }
            return r;
        }

        /**
         * Used for pools and spawns protection, these worldzones are a 3d space inside the specified world. It contains a x-min to x-max, y-min to y-max and finally a z-min to z-max.<p>
         * You can check with {@link WorldZone#isInside(Location)} is a specified location is inside this worldzone or not. <p>
         * Your locationsKey param can be a multi-key, e.g Your key will be "Protection.Red" if your JSON looks like {"Protection": {"Red": {"X1": 90.5, ...}}} <p>
         * Your key can also be null if your worldzone is the main JSON Object of the specified file.
         * @param file The JSON file which contains the positions
         * @param locationsKey The key to the X1,X2,Y1,Y2,Z1,Z2 double coordinates of the 3d space. Its a JSON Object with the previous given keys. X1 being x-min and X2 being x-max, and so on...
         * @param world The Bukkit world in which you want {@link WorldZone#isInside(Location)} returns true if the location is inside the given coordinates.
         * @return A WorldZone object (which is globally a 3d space inside the specified world)
         */
        public static WorldZone readFromFile(File file, @Nullable String locationsKey, World world)
        {
            double[][] locations = locations(jsonFromKey(file, locationsKey));
            return new WorldZone(world, locations[0], locations[1], locations[2]);
        }

        public boolean isInside(Location location)
        {
            if(world != location.getWorld())
                return false;

            double locX = location.getX();
            double locY = location.getY();
            double locZ = location.getZ();

            return locX > x[0] && locX < x[1] && locZ > z[0] && locZ < z[1] && locY > y[0] && locY < y[1];
        }

    }

}
