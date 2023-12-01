package ch.tower.events;

import ch.tower.Main;
import org.bukkit.Bukkit;

public class EndEvents implements StateEvents
{
    private static EndEvents instance = null;
    private EndEvents(){}

    public static synchronized EndEvents getInstance()
    {
        if(instance == null)
        {
            instance = new EndEvents();
        }
        return instance;
    }

    //TODO Cancel LoginEvent (Has scoreboards dont update placeholders in this state), mercii!

    @Override
    public void onStateBegin()
    {
        Bukkit.getOnlinePlayers().forEach(Main.getInstance().getManager().getScoreboardManager()::updateBoard);
        Bukkit.broadcastMessage("§cThe server will restart in 30 seconds. You will be kicked.");
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> Main.getInstance().getManager().stop(), 30*20L); //TODO restart with script?
        //maybe save scores into database, etc..
    }

    @Override
    public void onStateLeave()
    {

    }
}
