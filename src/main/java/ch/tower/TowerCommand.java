package ch.tower;

import ch.tower.managers.ActionsManager;
import ch.tower.managers.TeamsManager;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.io.File;

public class TowerCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if(sender instanceof Player p){
            if(args.length == 1 && args[0].equals("original")){
                World w = Bukkit.getWorld("Tower_Copy");
                if(w != null){
                    for(Player players : w.getPlayers()){
                        players.teleport(TeamsManager.PlayerTeam.SPECTATOR.getInfo().lobbySpawn());
                    }
                    if(!Bukkit.unloadWorld(w, true)){
                        p.sendMessage("§4The world Tower_Copy has not been saved!!! Maybe check console or error files inside the server folder for more info.");
                    }
                    return true;
                }
                File uid = new File(Bukkit.getWorldContainer(), "Tower_Copy/uid.dat");
                if(uid.exists()){
                    if(uid.delete()){
                        System.out.println("Removed uid.dat of Tower_Copy world.");
                    }
                }
                w = Bukkit.createWorld(WorldCreator.name("Tower_Copy"));
                if(w == null){
                    p.sendMessage("§cFailed to create world. Are you sure your server does have a Tower_Copy world?");
                    return false;
                }
                w.setGameRule(GameRule.DO_MOB_SPAWNING, false);
                w.setGameRule(GameRule.DO_FIRE_TICK, false);
                w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                w.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                w.setTime(12000);
                w.setThundering(false);
                w.setClearWeatherDuration(1);
                Location loc = TeamsManager.PlayerTeam.SPECTATOR.getSpawn().clone();
                loc.setWorld(w);
                p.teleport(loc);
                return true;
            }
            else if((args.length == 1 || args.length == 3) && args[0].equals("tp")){
                if(args.length == 1){
                    p.sendMessage("§6Usage: §r/tower tp game|copy blue|red|spec");
                    p.sendMessage("§6I.e: §r/tower tp copy blue");
                    return true;
                } else {
                    String worldName = args[1].equals("copy") ? "Tower_Copy" : "Tower";
                    World w = Bukkit.getWorld(worldName);
                    if(w == null){
                        p.sendMessage("§4Are you sure this world is loaded? Use /tower original to load the tower copy map.");
                        return false;
                    }
                    String teamName = args[2].equals("spec") ? "SPECTATOR" : args[2].toUpperCase();
                    try{
                        TeamsManager.PlayerTeam team = TeamsManager.PlayerTeam.valueOf(teamName);
                        Location loc = team.getInfo().spawn().clone();
                        loc.setWorld(w);
                        p.teleport(loc);
                        return true;
                    }catch(Exception e){
                        p.sendMessage("§4Cannot find any team named §c"+teamName);
                        return false;
                    }
                }
            }
            else if(args.length >= 1 && args[0].equals("money")){
                if(args.length != 3){
                    p.sendMessage("§6Usage: §r/tower money pseudo <value>");
                    p.sendMessage("§6I.e: §r/tower money Luca008 487.56");
                    return true;
                } else {
                    TowerPlayer player = TowerPlayer.getPlayer(Bukkit.getOfflinePlayer(args[1]));
                    if(player == null){
                        p.sendMessage("§4Cannot find a player currently in game with the name §c"+args[1]);
                        return false;
                    }
                    try{
                        double money = Double.parseDouble(args[2]);
                        if(money < 0){
                            p.sendMessage("§4Cannot give negative money.");
                            return false;
                        }
                        player.setMoney(money);
                        return true;
                    }catch(NumberFormatException nfe){
                        p.sendMessage("§4Cannot parse " + args[2] + " as a decimal.");
                        return false;
                    }
                }
            }
            else if(args.length >= 1 && args[0].equals("multiplier")){
                ActionsManager actManager = Main.getInstance().getManager().getActionsManager();
                if(args.length == 1){
                    double multiplier = actManager.getMultiplier();
                    char color = multiplier < 1.0 ? 'c' : multiplier > 1.0 ? 'a' : 'f';
                    p.sendMessage("§6------ Multiplier ------");
                    p.sendMessage("§eThe current multiplier is: §"+color+"x"+multiplier);
                    p.sendMessage("§7/tower multiplier <new value> to change it!");
                    p.sendMessage("§6------------------------");
                    return true;
                }
                if(args.length != 2){
                    p.sendMessage("§6Usage: §r/tower multiplier <value>");
                    p.sendMessage("§6I.e: §r/tower multiplier 1.2");
                    return true;
                } else {
                    try{
                        double newMultiplier = Double.parseDouble(args[1]);
                        if(newMultiplier < 0 || newMultiplier > 1000.0){
                            p.sendMessage("§4Multiplier must be between 0.0 and 1000.0");
                            return false;
                        }
                        double oldMultiplier = actManager.getMultiplier();
                        if(newMultiplier == oldMultiplier){
                            p.sendMessage("§4Current multiplier and new multiplier are the same.");
                            return false;
                        }
                        actManager.setMultiplier(newMultiplier);
                        p.sendMessage("§7Multiplier has changed from §c" + oldMultiplier + " §7to §a" + newMultiplier);
                        return true;
                    }catch(NumberFormatException nfe){
                        p.sendMessage("§4Cannot parse " + args[1] + " as a decimal.");
                        return false;
                    }
                }
            }
        }
        return false;
    }
}
