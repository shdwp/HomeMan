package org.sp.hm;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: sp
 * Date: 19.06.12
 * Time: 12:34
 * To change this template use File | Settings | File Templates.
 */
public class TeleportManager {
    HashMap<Long, Object[]> data = new HashMap<Long, Object[]>();
    HashMap<String, Long> usage = new HashMap<String, Long>();

    Server server;
    Long delay;


    public TeleportManager(Server _server, Long _delay) {
        server = _server;
        delay = _delay;

    }

    public void add(Long time, String player, Location loc) {
        data.put(time, new Object[] {player,  loc});
        usage.put(player, System.currentTimeMillis());
    }
    
    public boolean usageCheck(String player, Long delay) {
        try {
            return System.currentTimeMillis() > usage.get(player) + delay;
        } catch (NullPointerException e) {
            return true;
        }
    }
    
    public void update() {
        Object[] keySet = data.keySet().toArray();
        for (int i = 0; i < keySet.length; i++) {
            if ((Long) keySet[i] < System.currentTimeMillis()) {
                Object[] teleport = data.get(keySet[i]);
                Player player = server.getPlayer(teleport[0].toString());
                if (player != null) {
                    player.teleport((Location) teleport[1]);
                }
                data.remove(keySet[i]);
            }
        }
    }

}
