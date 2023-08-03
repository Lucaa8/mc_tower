package ch.tower;

import ch.tower.events.GlobalEvents;
import ch.tower.items.ArmorEquipment;
import ch.tower.managers.*;
import ch.tower.shop.categoryMenus.ToolsMenu;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
        if(!copyStreamToFile("config.json", GameManager.CONFIG_FILE) ||
           !copyStreamToFile("spawns.json", WorldManager.SPAWN_FILE) ||
           !copyStreamToFile("npc.json", NPCManager.NPC_FILE) ||
           !copyStreamToFile("scoreboards.json", ScoreboardManager.SCOREBOARD_FILE) ||
           !copyStreamToFile("shop.json", ShopMenuManager.SHOP_FILE) ||
           !copyStreamToFile("default_items.json", ToolsMenu.DEFAULT_TOOLS_FILE) ||
           !copyStreamToFile("armor.json", ToolsMenu.ARMOR_FILE))
        {
            System.err.println("Cannot generate all required resources...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        //just to execute the static code block.
        new ArmorEquipment();
        //DO NOT ADD ANYTHING BEFORE THIS LINE
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new GlobalEvents(), this);
        game = new GameManager();

    }

    public void onDisable()
    {
        //maybe null if onEnable failed and want to disable the plugin
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

    private boolean copyStreamToFile(String resourceName, File resourceFile)
    {
        if(!resourceFile.exists())
        {
            System.out.println("Generating "+ resourceName +" file...");
            try {
                Files.copy(getClassLoader().getResource(resourceName).openStream(), resourceFile.toPath());
            } catch (IOException e) {
                System.err.println("Cannot generate " + resourceName + " file...");
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
}