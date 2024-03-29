package ch.tower;

import ch.luca008.SpigotApi.Api.FileApi;
import ch.luca008.SpigotApi.Utils.Logger;
import ch.tower.items.WeaponStatistics;
import ch.tower.managers.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class Main extends JavaPlugin {

    private static Main instance;

    private GameManager game;

    public void onEnable()
    {
        instance = this;
        if(!getDataFolder().mkdirs() && !getDataFolder().exists())
        {
            System.err.println("Cannot create Tower plugin folder...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if(!generateFiles())
        {
            System.err.println("Cannot generate all required resources...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if(!checkAndSetArrowsDespawnRate())
        {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getCommand("tower").setExecutor(new TowerCommand());
        //DO NOT ADD ANYTHING BEFORE THIS LINE
        game = new GameManager();
    }

    public void onDisable()
    {
        //maybe null if onEnable failed and wants to disable the plugin
        if(game != null)
            game.stop();
    }

    public static Main getInstance()
    {
        return instance;
    }

    public GameManager getManager()
    {
        return game;
    }

    /*
    This is needed because the plugin stores EVERY arrow shot during the game.
    I need it for weapon statistics
    (because I cannot get the bow itemstack in the EntityDamageByEntityEvent, so I need to link the arrow in the damage event and the bow who shot the arrow from the EntityShootBowEvent)
    The EntityShootBowEvent registers every arrow shot, even the arrows which missed the target.
    The storage will clear any "invalid" (despawned) arrow every x seconds. So to keep a light storage the despawn-rate of arrows need to be short (like 30 sec)
     */
    private boolean checkAndSetArrowsDespawnRate()
    {
        YamlConfiguration spigot = getServer().spigot().getConfig();

        ConfigurationSection worldSettings = spigot.getConfigurationSection("world-settings");
        if(worldSettings == null)
        {
            Logger.error("Cannot find the `world-settings` section in your spigot.yml file, delete the file and restart the server.", Main.class.getName());
            return false;
        }

        ConfigurationSection defaultWorld = worldSettings.getConfigurationSection("default");
        if(defaultWorld == null)
        {
            Logger.error("Cannot find the `world-settings.default` section in your spigot.yml file, delete the file and restart the server.", Main.class.getName());
            return false;
        }

        int desiredRate = WeaponStatistics.ShootListener.ARROW_DESPAWN_RATE_SECONDS;
        int currentRate = defaultWorld.getInt("arrow-despawn-rate") / 20; //return 0 if does not exist

        if(currentRate == desiredRate)
        {
            Logger.info("Arrow despawn rate is set to " + desiredRate + " seconds! Do not change this setting.", Main.class.getName());
            return true;
        }

        Logger.warn("The setting `world-settings.arrow-despawn-rate` will be changed to " + desiredRate + " seconds. (current: " + (currentRate) + ")", Main.class.getName());

        defaultWorld.set("arrow-despawn-rate", desiredRate*20);
        worldSettings.set("default", defaultWorld);
        spigot.set("world-settings", worldSettings);

        try {
            spigot.save(new File("spigot.yml"));
        } catch (IOException e) {
            Logger.error("The configuration cannot be saved inside the spigot.yml file.", Main.class.getName());
            return false;
        }

        Logger.info("The setting has been changed, the server will be stopped, please restart it.", Main.class.getName());
        Bukkit.shutdown();
        return true;
    }

    private boolean exportInternFile(String resourceName, File resourceFile)
    {
        boolean generated = true;
        if(!resourceFile.exists())
        {
            try {
                System.out.println("Generating "+ resourceName +" file...");
                generated = FileApi.exportFile(Main.class, resourceName, resourceFile);
            } catch (IOException e) {
                System.err.println("Cannot generate " + resourceName + " file...");
                e.printStackTrace();
                generated = false;
            }
        }
        return generated;
    }

    private boolean generateFiles()
    {
        boolean ok = true;
        for(String resource : FileApi.listFiles(Main.class).stream().filter(f -> f.startsWith("config/") && f.endsWith(".json")).toList())
        {
            if(!exportInternFile(resource, new File(Main.getInstance().getDataFolder(), resource.split("/")[1])))
            {
                ok = false;
                break;
            }
        }
        return ok;
    }

}