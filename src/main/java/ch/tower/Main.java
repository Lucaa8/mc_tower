package ch.tower;

import ch.luca008.SpigotApi.Api.FileApi;
import ch.tower.managers.GameManager;
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