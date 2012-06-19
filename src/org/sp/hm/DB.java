package org.sp.hm;

import com.mysql.jdbc.ResultSetRow;
import com.sun.org.apache.bcel.internal.generic.TABLESWITCH;
import moc.MOCDBLib.DBConnector;
import moc.MOCDBLib.MOCDBLib;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: sp
 * Date: 19.06.12
 * Time: 11:45
 * To change this template use File | Settings | File Templates.
 */
public class DB {
    MOCDBLib db;
    DBConnector dbc;
    public static String TABLE = "homes";
    public static String PLAYER_ROW = "player";
    public static String NAME_ROW = "name";
    public static String LOC_ROW = "location";
    Server server;
    public DB(PluginManager pluginManager, Server _server) {
        db = (MOCDBLib) pluginManager.getPlugin("MOCDBLib");
        dbc = db.getMineCraftDB("HomeMan", Logger.global);
        server = _server;

        dbc.ensureTable(TABLE, "player varchar(64), name varchar(32), location text");
    }

    public void setHome(String player, String name,  Location location) {
        String deleteQuery = "DELETE FROM " + TABLE + " WHERE "+PLAYER_ROW+" = ? AND "+NAME_ROW+" = ?";
        PreparedStatement _deleteQuery = dbc.prepareStatement(deleteQuery);

        String insertQuery = String.format(
                "INSERT INTO " +TABLE+ " SET " +PLAYER_ROW+ " = ?, "+NAME_ROW+" = ?, "+LOC_ROW+" = '%s'", locationSet(location)
        );
        PreparedStatement _insertQuery = dbc.prepareStatement(insertQuery);


        try {
            _deleteQuery.setString(1, player.toLowerCase());
            _deleteQuery.setString(2, name.toLowerCase());

            _insertQuery.setString(1, player.toLowerCase());
            _insertQuery.setString(2, name.toLowerCase());

            dbc.insertQuery(_deleteQuery);
            dbc.insertQuery(_insertQuery);

        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

    }

    public void purgeHomes(String player) {
        try {
            String deleteQuery = "DELETE FROM " + TABLE + " WHERE "+PLAYER_ROW+" = ?";
            PreparedStatement _deleteQuery = dbc.prepareStatement(deleteQuery);
            _deleteQuery.setString(1, player.toLowerCase());
            dbc.insertQuery(_deleteQuery);
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
    }

    public int homeIndex(String player, String home) {
        int i = 0;
        for (Object name : getNames(player)) {
            if (name.toString() == home) {
                return i;
            }
            i++;
        }
        return 0;
    }
    
    public void deleteHome(String player, String name) {
        String deleteQuery = String.format("DELETE FROM " + TABLE + " WHERE "+PLAYER_ROW+" = ? AND "+NAME_ROW+" = ?");
        PreparedStatement _deleteQuery = dbc.prepareStatement(deleteQuery);
        try {
            _deleteQuery.setString(0, player.toLowerCase());
            _deleteQuery.setString(1, name.toLowerCase());
            dbc.insertQuery(_deleteQuery);
        } catch (SQLException e) {
            System.out.println(e.toString());
        }
    }

    public Object[] getNames(String player) {
        String selectQuery = String.format("SELECT "+NAME_ROW+" FROM " + TABLE + " WHERE "+PLAYER_ROW+" = ?");
        try {
            PreparedStatement _selectQuery = dbc.prepareStatement(selectQuery);
            _selectQuery.setString(1, player.toLowerCase());
            ResultSet rs = dbc.sqlQuery(_selectQuery);
            rs.last();
            if (rs.getRow() == 0) {
                return null;
            }
            Vector result = new Vector();
            do {
                result.add(rs.getString("name"));
            } while (rs.previous());
            return result.toArray();

        } catch (SQLException sqle) {
            return null;
        }
    }

    public int countHomes(String player) {
        String selectQuery = "SELECT COUNT(*) FROM " + TABLE + " WHERE "+PLAYER_ROW+" = ?";
        try {
            PreparedStatement _selectQuery = dbc.prepareStatement(selectQuery);
            _selectQuery.setString(1, player.toLowerCase());

            ResultSet rs = dbc.sqlQuery(_selectQuery);
            rs.first();
            return rs.getInt(1);
        }catch (SQLException sqle) {
            return 0;
        }
    }

    public Location getHome(String player, String name) {
        String selectQuery = "SELECT * FROM " + TABLE + " WHERE "+PLAYER_ROW+" = ? AND "+NAME_ROW+" = ? ";

        try {
            PreparedStatement _selectQuery = dbc.prepareStatement(selectQuery);
            _selectQuery.setString(1, player.toLowerCase());
            _selectQuery.setString(2, name.toLowerCase());
            ResultSet rs = dbc.sqlQuery(_selectQuery);
            rs.last();
            if (rs.getRow() == 0)
                return null;
            return locationGet(rs.getString("location"));
        } catch (SQLException sqle) {
            return null;
        }
    }
    
    public String locationSet(Location location) {
        return String.format("%s,%s,%s,%s", location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
    }
    
    public Location locationGet(String data) {
        String[] loc = data.split(",");
        Double[] result = new Double[3];
        for (int i = 1; i < loc.length; i++) {
            result[i-1] = Double.parseDouble(loc[i]);
        }
        return new Location(
                server.getWorld(loc[0]),
                result[0],
                result[1],
                result[2]
        );
    }
}
