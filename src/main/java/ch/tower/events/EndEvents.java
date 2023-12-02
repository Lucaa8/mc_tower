package ch.tower.events;

import ch.tower.Main;
import ch.tower.managers.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;

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
        Bukkit.getOnlinePlayers().stream()
                .peek(Main.getInstance().getManager().getScoreboardManager()::updateBoard)
                .peek(p->p.setGameMode(GameMode.CREATIVE)).close();
        int timer = GameManager.ConfigField.TIMER_DURATION_END.get();
        Bukkit.broadcastMessage("§cThe server will restart in "+timer+" seconds. You will be kicked.");
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> Main.getInstance().getManager().stop(), timer*20L); //TODO restart with script?
        //maybe save scores into database, etc..
    }

    @Override
    public void onStateLeave()
    {

    }
}
