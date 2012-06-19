package org.sp.hm;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.xml.ws.soap.Addressing;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: sp
 * Date: 19.06.12
 * Time: 14:22
 * To change this template use File | Settings | File Templates.
 */


public class AdminCommands {
    DB db;
    String defaultHome;
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Args {
        int min();
        int max();
    }
    @Retention(RetentionPolicy.RUNTIME)
    public @interface PlayerOnly {

    }

    public AdminCommands(DB _db, String _defaultHome) {
        db = _db;
        defaultHome = _defaultHome;
    }

    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Chat.$("/ahome:"));
            sender.sendMessage(Chat.$("list (PLAYER) - list homes of PLAYER"));
            sender.sendMessage(Chat.$("delete (PLAYER) [HOME] - delete HOME (or default) of PLAYER"));
            sender.sendMessage(Chat.$("purge (PLAYER) - delete all PLAYER'S homes"));
            sender.sendMessage(Chat.$("As player only:"));
            sender.sendMessage(Chat.$("add (PLAYER) [HOME] - add HOME to PLAYER on your current location. Overrides player's homes limit!"));
            sender.sendMessage(Chat.$("tp (PLAYER) [HOME] - teleport to PLAYER'S HOME"));
        } else {
            String cmd = args[0];
            String[] callbackArgs = new String[args.length-1];
            for (int i = 1; i < args.length; i++) {
                callbackArgs[i-1] = args[i];
            }
            try {
                Method callback = getClass().getMethod(cmd, CommandSender.class, String[].class);
                Args notation = callback.getAnnotation(AdminCommands.Args.class);
                PlayerOnly playerOnly = callback.getAnnotation(AdminCommands.PlayerOnly.class);
                if (callbackArgs.length < notation.min() || callbackArgs.length > notation.max()) {
                    execute(sender, new String[]{});
                    return;
                }
                if (playerOnly != null) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(Chat.$("This command player-only!"));
                        return;
                    }
                    callback.invoke(this, (Player) sender, callbackArgs);
                } else
                    callback.invoke(this, sender, callbackArgs);
                
                sender.sendMessage(Chat.$("Action " + cmd + " performed!"));

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                sender.sendMessage(Chat.$("No such command!"));
                e.printStackTrace();
            }
        }

    }

    @Args(min=1, max=1)
    public void list(CommandSender sender, String[] args) {
        sender.sendMessage(Chat.$("Player " + args[0] + " homes:"));
        Object[] homes = db.getNames(args[0]);
        if (homes == null) {
            sender.sendMessage(Chat.$("There is no homes!"));
            return;
        }
        for (Object home : homes) {
            sender.sendMessage(Chat.$(home.toString()));
        }
            
    }

    @Args(min=1, max=2)
    public void delete(CommandSender sender, String[] args) {
        String homeName = defaultHome;
        if (args.length == 2)
            homeName = args[1];
        db.deleteHome(args[0], args[1]);
    }

    @Args(min=1, max=2)
    public void purge(CommandSender sender, String[] args) {
        db.purgeHomes(args[0]);
    }

    @PlayerOnly
    @Args(min=1, max=2)
    public void add(CommandSender sender, String[] args) {
        String homeName = defaultHome;
        if (args.length == 2)
            homeName = args[1];
        db.setHome(args[0], homeName, ((Player) sender).getLocation());
    }
    
    @PlayerOnly
    @Args(min=1, max=2)
    public void tp(CommandSender sender, String args[]) {
        String homeName = defaultHome;
        if (args.length == 2)
            homeName = args[1];
        Location home = db.getHome(args[0], homeName);
        if (home == null) {
            sender.sendMessage(Chat.$("Home not found!"));
        } else ((Player) sender).teleport(home);
    }
}
