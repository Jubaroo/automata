package org.takino.mods.helpers;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import org.takino.mods.ToolType;
import org.takino.mods.tools.ToolJob;

import java.util.*;

import static org.takino.mods.Automata.debug;

public class WorkerHelper {

    static Map<Long, Creature> performerMap = new HashMap<>();
    static Map<Long, Date> startedMap = new HashMap<>();
    static Map<ToolType, ToolJob> classesMap = new HashMap<>();

    public static List<Long> runningJobs = new ArrayList<>();

    public static void registerToolJob(ToolType type, ToolJob job) {
        classesMap.put(type, job);
    }

    public static ToolJob getJob(ToolType type) {
        return classesMap.get(type);
    }

    public static void addJob(Item item, Creature performer) {
        Long wurmid = item.getWurmId();
        runningJobs.add(wurmid);
        performerMap.put(wurmid, performer);
        startedMap.put(wurmid, new Date(System.currentTimeMillis()));
        item.setTemplateId(942);
        item.updateModelNameOnGroundItem();
    }
    public static void removeJob(Long wurmid){
        if (contains(wurmid)) {
            runningJobs.remove(wurmid);
            performerMap.remove(wurmid);
            startedMap.remove(wurmid);
            try {
                Item item = Items.getItem(wurmid);
                item.setTemplateId(934);
                item.updateModelNameOnGroundItem();
            } catch (NoSuchItemException e) {
                e.printStackTrace();
            }
        }
    }
    public static boolean contains(Long wurmid) {
        return runningJobs.contains(wurmid);
    }

    public static Creature getPerformer(Long wurmid) {
        return performerMap.get(wurmid);
    }







    public static void shutDown() {
        for (Long i: runningJobs) {
            removeJob(i);
        }
    }


    public static Item findBulkContainerOrNull(Item item, boolean returnFull) {
        int initialX = item.getTileX();
        int initialY = item.getTileY();
        float effect = item.getSpellEffectPower((byte) 121); //TODO: Replace (byte) 121 with SpellcraftSpell.LABOURING_SPIRIT.getEnchant()
        int radius = (int) Math.max(1.0, effect / 10);
        for (int x = initialX - radius; x <= initialX + radius; x++) {
            for (int y = initialY - radius; y <= initialY + radius; y++) {
                VolaTile vtile = Zones.getTileOrNull(x, y, item.isOnSurface());
                if (vtile != null && vtile.getItems() != null) {
                    for (Item s : vtile.getItems()) {
                        if (s.isCrate()) { // Dont' want BSBs to be used, too OP
                            if (returnFull) {
                                return s;
                            } else {
                                if (!isFull(s)) {
                                    return s;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }




    public static void addItemToCrate(Item crate, Item toInsert) throws NoSuchTemplateException, FailedException {
        Iterator<Item> items = crate.getItems().iterator();
        Item toAddTo = null;
        while (items.hasNext()) {
            Item i = items.next();
            if (i.getRealTemplateId() == toInsert.getTemplateId() &&
                    i.getMaterial() == toInsert.getMaterial() &&
                    i.getAuxData() == toInsert.getAuxData()) {
                //found!
                toAddTo = i;
                break;
            }
        }
        if (toAddTo != null) {
            debug("Adding the stuff!");
            float fe = toAddTo.getBulkNumsFloat(false);
            float percent1 = (float) toInsert.getWeightGrams() / (float) toInsert.getTemplate().getWeightGrams();
            debug("Percent added: " + percent1);
            float percentAdded1 = percent1 / (fe + percent1);
            float qlDiff = toAddTo.getQualityLevel() - toInsert.getCurrentQualityLevel();
            float qlChange = percentAdded1 * qlDiff;
            float newQl;
            if (qlDiff > 0.0F) {
                newQl = toAddTo.getQualityLevel() - qlChange * 1.1F;
                toAddTo.setQualityLevel(Math.max(1.0F, newQl));
            } else if (qlDiff < 0.0F) {
                newQl = toAddTo.getQualityLevel() - qlChange * 0.9F;
                toAddTo.setQualityLevel(Math.max(1.0F, newQl));
            }
            int newWeight = toAddTo.getWeightGrams() + (int) (percent1 * (float) toInsert.getTemplate().getVolume());
            debug("Setting weight to: " + newWeight);
            toAddTo.setWeight(newWeight, true);
            Items.destroyItem(toInsert.getWurmId());
        } else {
            debug("Creating the stuff!");
            toAddTo = ItemFactory.createItem(669,
                    toInsert.getCurrentQualityLevel(), toInsert.getMaterial(), (byte) 0, null);
            toAddTo.setRealTemplate(toInsert.getTemplateId());
            toAddTo.setAuxData(toInsert.getAuxData());
            float fe = (float) toInsert.getWeightGrams() / (float) toInsert.getTemplate().getWeightGrams();
            if (!toAddTo.setWeight((int) (fe * (float) toAddTo.getTemplate().getVolume()), true)) {
                crate.insertItem(toAddTo, true);
            }
            debug("FE: " + fe);
            Items.destroyItem(toInsert.getWurmId());

        }
    }

    public static Item findBulkContainerOrNull(Item item) {
        return findBulkContainerOrNull(item, false);
    }

    private static boolean isFull(Item crate) {
        int count =0;
        for (Item i : crate.getItemsAsArray()) {
            count+=i.getBulkNums();
        }
        if (crate.getTemplateId() == 852) {
            debug("Large container found!");
            return count>= 300;

        } else {
            debug("Small container found!");
            return count>= 150;
        }
    }



}
