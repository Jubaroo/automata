package org.takino.mods.tools;


import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.Server;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.sounds.SoundPlayer;
import org.takino.mods.Config;
import org.takino.mods.helpers.DatabaseHelper;
import org.takino.mods.helpers.WorkerHelper;

import java.io.IOException;

public class ChiselJob implements ToolJob {
    @Override
    public void doWork(Item target) throws NoSuchTemplateException, FailedException, IOException {
        chiselSome(target);
    }

    private static Item findSourceBsb(Item item, int templateid) {
        return WorkerHelper.findBulkContainerOrNull(item, templateid);
    }

    private static void playRandomSound(Item item) {
        String[] sounds = {
                "sound.work.masonry",
                "sound.work.stonecutting"
        };
        int rand = Server.rand.nextInt(1);
        SoundPlayer.playSound(sounds[rand], item, item.getPosZ());
    }

    private static void chiselSome(Item item) throws NoSuchTemplateException, FailedException {
        int[] templates = {ItemList.rock, ItemList.slateShard, ItemList.marbleShard, ItemList.sandstone};
        int realid = 0;
        Item sourceBsb = null;
        for (int templateid : templates) {
            sourceBsb = findSourceBsb(item, templateid);
            if (sourceBsb != null) {
                realid = templateid;
                break;
            }
        }
        if (sourceBsb == null) {
            WorkerHelper.removeJob(item.getWurmId());
            return;
        }
        if (!WorkerHelper.hasEnoughPower(item, DatabaseHelper.getUsage(item))) {
            WorkerHelper.removeJob(item.getWurmId());
        }
        Item targetCrate = WorkerHelper.findBulkContainerOrNull(item);
        int num = (int) (Config.defaultQuantity + Config.defaultQuantity *
                (Server.rand.nextFloat() * WorkerHelper.getMaxAmount(item)));
        int templateId = 0;
        switch (realid) {
            case ItemList.rock:
                templateId = ItemList.stoneBrick;
                break;
            case ItemList.slateShard:
                templateId = ItemList.slateBrick;
                break;
            case ItemList.marbleShard:
                templateId = ItemList.marbleBrick;
                break;
            case ItemList.sandstone:
                templateId = ItemList.sandstoneBrick;
                break;
            default:
                return;
        }
        float ql = Server.rand.nextFloat() * Config.maxQualityLevel;
        playRandomSound(item);
        Item creation = ItemFactory.createItem(templateId, ql, (byte) 0, null);
        creation.setWeight(num * creation.getTemplate().getWeightGrams(), true);
        WorkerHelper.addItemToCrate(targetCrate, creation);
        targetCrate.updateModelNameOnGroundItem();
        DatabaseHelper.decreasePower(item, DatabaseHelper.getUsage(item));
        Item toRemove = ItemFactory.createItem(realid, 1, null);
        WorkerHelper.removeItemFromBsb(sourceBsb, toRemove, num);
        Items.destroyItem(toRemove.getWurmId());

    }
}
