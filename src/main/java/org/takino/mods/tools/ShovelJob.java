package org.takino.mods.tools;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Server;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.NoSuchTemplateException;
import org.takino.mods.Config;
import org.takino.mods.helpers.DatabaseHelper;
import org.takino.mods.helpers.WorkerHelper;

import static org.takino.mods.Automata.debug;

public class ShovelJob implements ToolJob {

    @Override
    public void doWork(Item target) throws NoSuchTemplateException, FailedException {
        tryDigAround(target);
    }


    private static void tryDigAround(Item item) throws NoSuchTemplateException, FailedException {
        if (!item.isOnSurface()) {
            return;
        }
        // TODO: Replace (byte) 121 with SpellcraftSpell.LABOURING_SPIRIT.getEnchant()

       if (!WorkerHelper.hasEnoughPower(item, DatabaseHelper.getUsage(item))) {
           return;
       }

        debug("Will be digging now.");
        int initialX = item.getTileX();
        int initialY = item.getTileY();
        byte foundType = 0;
        //VolaTile vtile = Zones.getTileOrNull(posx, posy, item.isOnSurface());

        // TODO: Replace (byte) 121 with SpellcraftSpell.LABOURING_SPIRIT.getEnchant()
        float effect = item.getSpellEffectPower((byte) 121);
        int radius = (int) Math.max(1.0, effect / 10);
        int maxAmount = WorkerHelper.getMaxAmount(item);
        outerloop:
        for (int x = initialX - radius; x <= initialX + radius; x++) {

            for (int y = initialY - radius; y <= initialY + radius; y++) {
                byte tileType = Tiles.decodeType(Server.surfaceMesh.getTile(x, y));
                if (tileType == Tiles.Tile.TILE_CLAY.id ||
                        tileType == Tiles.Tile.TILE_PEAT.id ||
                        tileType == Tiles.Tile.TILE_TAR.id ||
                        tileType == Tiles.Tile.TILE_MOSS.id) {
                    debug("Found a workable tile near shovel-enabled turret");
                    debug("Searching for a workable Crate nearby");
                    foundType = tileType;
                    break outerloop;
                }
            }
        }
        if (foundType == 0) {
            return;
        }
        int templateId;
        Item container = WorkerHelper.findBulkContainerOrNull(item);
        if (container == null) {
            debug("All containers null or none found");
            WorkerHelper.removeJob(item.getWurmId());
            return; // All containers full or none found.
        } else {
            debug("Container found: " + container.getName());
        }
        if (foundType == Tiles.Tile.TILE_CLAY.id) {
            templateId = ItemList.clay;
        } else if (foundType == Tiles.Tile.TILE_PEAT.id) {
            templateId = ItemList.peat;
        } else if (foundType == Tiles.Tile.TILE_TAR.id) {
            templateId = ItemList.tar;
        } else {
            templateId = ItemList.moss;
        }
        float ql = Server.rand.nextFloat() * Config.maxQualityLevel;
        int num = (int) (Config.defaultQuantity + Config.defaultQuantity *
                (Server.rand.nextFloat() * maxAmount));
        debug("Creating " + num + " items");
        Item toInsert = ItemFactory.createItem(templateId, ql, (byte) 0, null);
        toInsert.setWeight(num*toInsert.getTemplate().getWeightGrams(), true);
        debug("Weight: " + toInsert.getWeightGrams());
        // toInsert.setWeight(num * toInsert.getTemplate().getWeightGrams(), true);
        WorkerHelper.addItemToCrate(container, toInsert);
        container.updateModelNameOnGroundItem();
        DatabaseHelper.decreasePower(item, DatabaseHelper.getUsage(item));
    }
}
