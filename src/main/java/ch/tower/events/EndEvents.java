package ch.tower.events;

import ch.tower.Main;
import ch.tower.managers.GameManager;
import ch.tower.managers.ScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

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
        ScoreboardManager sb = Main.getInstance().getManager().getScoreboardManager();
        for(Player p : Bukkit.getOnlinePlayers())
        {
            sb.updateBoard(p);
            p.setGameMode(GameMode.CREATIVE);
        }
        int timer = GameManager.ConfigField.TIMER_DURATION_END.get();
        Bukkit.broadcastMessage(GameManager.getMessage("MSG_END_RESTART", String.valueOf(timer)));
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> Main.getInstance().getManager().stop(), timer*20L); //TODO restart with script?
        //maybe save scores into database, etc..
    }

    @Override
    public void onStateLeave()
    {

    }
}
