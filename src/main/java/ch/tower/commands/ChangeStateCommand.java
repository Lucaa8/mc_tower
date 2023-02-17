package ch.tower.commands;

import ch.tower.Main;
import ch.tower.managers.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import static ch.tower.managers.GameManager.GameState.*;

public class ChangeStateCommand implements CommandExecutor
{
    //Command "ChangeState"
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args)
    {
        GameManager.GameState nextState;
        switch (Main.getInstance().getManager().getState())
        {
            case WAIT -> nextState = GAME;
            case GAME -> nextState = END;
            default -> nextState = WAIT;
        }
        Main.getInstance().getManager().setState(nextState);
        return true;
    }
}
