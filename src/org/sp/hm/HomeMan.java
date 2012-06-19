package org.sp.hm;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by IntelliJ IDEA.
 * User: sp
 * Date: 19.06.12
 * Time: 11:42
 * To change this template use File | Settings | File Templates.
 */

public class HomeMan extends JavaPlugin {
    DB db;
    TeleportManager tp;
    AdminCommands ac;
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();
        DB.TABLE = getConfig().getString("db.table");
        DB.PLAYER_ROW = getConfig().getString("db.player_row");
        DB.NAME_ROW = getConfig().getString("db.name_row");
        DB.LOC_ROW = getConfig().getString("db.location_row");
        db = new DB(getServer().getPluginManager(), getServer());
        tp = new TeleportManager(getServer(), getConfig().getLong("teleport-update")*100);
        getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
            public void run() {
                tp.update();
            }
        }, 0L, getConfig().getLong("teleport-update"));
        ac = new AdminCommands(db, getConfig().getString("default-home"));
        Chat.chatColor = getConfig().getString("chat-color");
    }
    public void onDisable() {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if (cmd.getName().equalsIgnoreCase("home") || cmd.getName().equalsIgnoreCase("sethome") || cmd.getName().equalsIgnoreCase("homelist")) {
            if (! (sender instanceof Player)) {
                sender.sendMessage(Chat.$("You must be a player to perform that!"));
                return true;
            }
            Player player = (Player) sender;
            String homeName = getConfig().getString("default-home");
            if (args.length == 1)
                homeName = args[0];

            int maxHomes = getConfig().getInt("max-homes");
            if (player.hasPermission("homeman.premium"))
                maxHomes = getConfig().getInt("max-premium-homes");
            /*
                /home
             */
            if (cmd.getName().equalsIgnoreCase("home")) {
                if (!tp.usageCheck(player.getName(), getConfig().getLong("usage-delay")*1000) && !player.hasPermission("homeman.no-usage-delay")) {
                    player.sendMessage(Chat.$(getConfig().getString("lang.cooldown")));
                    return true;
                }
                Location home = db.getHome(player.getName(), homeName);
                if (db.homeIndex(player.getName(), homeName) > maxHomes) {
                    player.sendMessage(Chat.$(getConfig().getString("lang.no-premium")));
                    return true;
                }
                if (home == null) {
                    player.sendMessage(Chat.$(getConfig().getString("lang.no-home")));
                    return true;
                }
                Long delay = getConfig().getLong("teleport-delay")*1000;
                if (player.hasPermission("homeman.no-tp-delay")) {
                    delay = new Long(0);
                    player.sendMessage(Chat.$(getConfig().getString("lang.tp-now")));
                } else {
                    player.sendMessage(Chat.$(getConfig().getString("lang.tp-delay").replace("%1", ""+delay/1000)));
                }
                tp.add(System.currentTimeMillis() + delay, player.getName(), home);
            }
            /*
                /sethome
             */
            if (cmd.getName().equalsIgnoreCase("sethome")) {


                if (db.countHomes(player.getName()) > maxHomes) {
                    player.sendMessage(Chat.$(getConfig().getString("lang.max-homes")));
                    return true;
                }
                db.setHome(player.getName(), homeName, player.getLocation());
                sender.sendMessage(Chat.$(getConfig().getString("lang.home-set")));
            }
            /*
                /homelist
             */
            if (cmd.getName().equalsIgnoreCase("homelist")) {
                player.sendMessage(Chat.$(getConfig().getString("lang.homes").replace("%s", ""+db.countHomes(player.getName()))));
                Object[] homes = db.getNames(player.getName());
                if (homes != null)
                    for (Object home : homes) {
                        player.sendMessage(Chat.$(home.toString()));
                    }
                else player.sendMessage(Chat.$(getConfig().getString("lang.no-homes")));
            }
        }
        if (cmd.getName().equalsIgnoreCase("ahome")) {
            if (!sender.hasPermission("homeman.admin")) {
                sender.sendMessage(Chat.$(getConfig().getString("lang.restricted")));
                return true;
            }
            ac.execute(sender, args);
        }
        return true;
    }
}
