package org.takino.mods.helpers;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
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
            case ItemList.shovel:
                //shovel;
                type = ToolType.SHOVEL;
                break;
            case ItemList.stoneChisel:
                type = ToolType.CHISEL;
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
        if (currentPower>getMaximumPower(item)) {
            //Should something bad happen?
            Zones.flash(item.getTileX(), item.getTileY(), false);
            //TODO : Replace with proper spell effect
            item.getSpellEffect((byte)121).
                    setPower(
                            (float)(item.getSpellEffectPower((byte)121)*0.9));

            String force="violent";
            float sp = item.getSpellEffectPower((byte)121);
            if (sp<70)  {
                force="loud";
            }
            if (sp<50) {
                force="";
            }
            if (sp<20) {
                force="silent";
            }
            if (sp<10) {
                force="barely heard";
            }
            String message = "You hear " +   force + " thunder, as some spirits escape " +
                    item.getName() + " after power overload.";
            sendMessageToPlayersAroundItem(message,item, 50);
            currentPower = getMaximumPower(item);
        }

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

    public static float getMaximumPower(Item item) {
        return (float)(100+Math.pow(item.getCurrentQualityLevel(),2));
    }


    public static float getUsage(Item item) {
        return Math.max(Math.min(50.0f/(item.getQualityLevel()+item.getSpellEffectPower((byte) 121)),3.0f),0.5f);
    }

    public static String getUsageString(Item item) {
        float usage = getUsage(item);
        String result = "It uses";
        if (usage > 2.5) {
            result+=" enormous amounts ";
        } else if (usage > 2.0) {
            result+=" a lot ";
        } else if (usage > 1.0) {
            result+=" some amount ";
        } else {
            result+=" only marginal quantities ";
        }
        result+=" of power.";
        return result;
    }

    public static String getPowerLevelString(Item item) {
        float currentPower = getCurrentPowerLevel(item);
        float maxPower = getMaximumPower(item);
        float percentPower = (currentPower/maxPower)*100;
        String powerString="no";
        if (percentPower>0.1) {
            powerString=" a little ";
        }
        if (percentPower>10) {
            powerString=" some ";
        }
        if (percentPower>30) {
            powerString=" quite some ";
        }
        if (percentPower>60) {
            powerString=" a lot of ";
        }
        if (percentPower>90) {
            return "It shakes violently from amount of power it currently holds.";
        }
        if (percentPower>80) {
            return "It vibrates from the amount of power it currently holds.";
        }
        return "It emits" + powerString + " power.";
    }


    public static void sendMessageToPlayersAroundItem(String message, Item theItem, int tileDist) {
        if (message.length() > 0) {
            int lTileDist = Math.abs(tileDist);
            int tilex = theItem.getTileX();
            int tiley = theItem.getTileY();

            for(int x = tilex - lTileDist; x <= tilex + lTileDist; ++x) {
                for(int y = tiley - lTileDist; y <= tiley + lTileDist; ++y) {
                    try {
                        Zone zone = Zones.getZone(x, y, theItem.isOnSurface());
                        VolaTile tile = zone.getTileOrNull(x, y);
                        for (Creature c: tile.getCreatures()) {
                            if (c instanceof Player) {
                                c.getCommunicator().sendNormalServerMessage(message);
                            }
                        }
                    } catch (NoSuchZoneException var12) {
                        ;
                    }
                }
            }
        }

    }
}
