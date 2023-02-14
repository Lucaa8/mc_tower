package ch.tower.commands;

import ch.tower.Main;
import ch.tower.managers.GameManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ChangeStateCommand implements CommandExecutor
{
    //Command "ChangeState"
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args)
    {
        Main.getInstance().getManager().setState(GameManager.GameState.GAME);
        return true;
    }
}
