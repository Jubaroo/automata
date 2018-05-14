package org.takino.mods.helpers;

import com.wurmonline.server.items.Item;
import org.gotti.wurmunlimited.modsupport.ModSupportDb;
import org.takino.mods.ToolType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.takino.mods.Automata.debug;

public class DatabaseHelper {

    public static void createTable() {
        Connection supportDb = ModSupportDb.getModSupportDb();
        try {
            supportDb.createStatement().execute("CREATE TABLE IF NOT EXISTS attached_tools " +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT, target INTEGER UNIQUE, attached INTEGER) ");
            supportDb.createStatement().execute("CREATE TABLE IF NOT EXISTS strange_device_powers"+
            "(id INTEGER PRIMARY KEY AUTOINCREMENT, target INTEGER UNIQUE, powerlevel FLOAT);");
            supportDb.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ToolType getAttachedTool(Item item) throws SQLException {
        Connection supportDb = ModSupportDb.getModSupportDb();
        debug("wid: " + item.getWurmId());
        ResultSet results = supportDb.createStatement().executeQuery("SELECT attached FROM attached_tools WHERE (target==" + item.getWurmId() + ");");
        if (!results.next()) {
            supportDb.close();
            debug("No tool?");
            return ToolType.NONE;
        } else {
            debug("Has tool");
            ToolType type = ToolType.getToolType(results.getInt(results.findColumn("attached")));
            supportDb.close();
            return type;
        }
    }


    public static boolean hasTool(Item target) throws SQLException {
        Connection conn = ModSupportDb.getModSupportDb();
        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM attached_tools where target==" + target.getWurmId() + ";");
        boolean result = false;
        if (rs.next()) {
            result = true;
        }
        conn.close();
        return result;
    }


    public static void setTool(Item source, Item target) {
        Connection conn = ModSupportDb.getModSupportDb();
        ToolType type;
        switch (source.getTemplateId()) {
            case 25:
                //shovel;
                type = ToolType.SHOVEL;
                break;
            default:
                type = null;
                break;
        }
        if (type != null) {
            try {
                conn.createStatement().execute("INSERT INTO attached_tools (target, attached) values (" + target.getWurmId()
                        + "," + type.id + ");");
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public static void increasePower(Item item, float power) {
        if (power<0) {
            power=-power;
        }
        Connection supportDb = ModSupportDb.getModSupportDb();
        float currentPower = getCurrentPowerLevel(item);
        currentPower+=power;
        try {
            supportDb.createStatement().execute("INSERT OR REPLACE INTO strange_device_powers (target, powerlevel) " +
                    "values (" + item.getWurmId() + ", " + currentPower + ");");
            supportDb.close();
        } catch (SQLException e) {
            e.printStackTrace();

        }
    }

    public static void decreasePower(Item item, float power) {
        if (power>0) {
            power=-power;
        }
        Connection supportDb = ModSupportDb.getModSupportDb();
        float currentPower = getCurrentPowerLevel(item);
        currentPower+=power;
        try {
            supportDb.createStatement().execute("INSERT OR REPLACE INTO strange_device_powers (target, powerlevel) " +
                    "values (" + item.getWurmId() + ", " + currentPower + ");");
            supportDb.close();
        } catch (SQLException e) {
            e.printStackTrace();

        }
    }

    public static float getCurrentPowerLevel(Item item) {
        Connection supportDb = ModSupportDb.getModSupportDb();
        try {
            ResultSet set = supportDb.createStatement().executeQuery("SELECT powerlevel FROM strange_device_powers WHERE " +
                    "(target==" + item.getWurmId() + ");");
            if (set.next()) {
                float result = set.getFloat(set.findColumn("powerlevel"));
                debug("Current powerlevel: " + result);
                supportDb.close();
                return result;
            }
            supportDb.close();
            return 0.0f;

        } catch (SQLException e) {
            e.printStackTrace();
            return 0.0f;
        }
    }
}
