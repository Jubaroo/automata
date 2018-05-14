package org.takino.mods.tools;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Server;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import org.takino.mods.Config;
import org.takino.mods.helpers.DatabaseHelper;
import org.takino.mods.helpers.WorkerHelper;

import java.io.IOException;

import static org.takino.mods.Automata.debug;

public class ShovelJob implements ToolJob {

    @Override
    public void doWork(Item target) throws NoSuchTemplateException, FailedException, IOException {
        tryDigAround(target);
    }


    private static void tryDigAround(Item item) throws NoSuchTemplateException, FailedException, IOException {
        if (!item.isOnSurface()) {
            return;
        }
        float power = Math.max(Math.min(50.0f/(item.getQualityLevel()+item.getSpellEffectPower(Config.spellId)),3.0f),0.5f);

        if (power > DatabaseHelper.getCurrentPowerLevel(item)) {
            WorkerHelper.removeJob(item.getWurmId());
            debug("Power is under minimum, powering down");
            return;
        } else {
            debug("current powerlevel: " +  DatabaseHelper.getCurrentPowerLevel(item));
        }

        debug("Will be digging now.");
        int initialX = (int) item.getTileX();
        int initialY = (int) item.getTileY();
        byte foundType = 0;
        //VolaTile vtile = Zones.getTileOrNull(posx, posy, item.isOnSurface());

        float effect = item.getSpellEffectPower(Config.spellId);
        int radius = (int) Math.max(1.0, effect / 10);
        int maxAmount = (int) Math.max(1.0, effect / 10 + item.getCurrentQualityLevel()/20);
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
                } else {
                    //nothing here!
                }
            }
        }
        if (foundType == 0) {
            return;
        }
        int templateId = 0;
        Item container = WorkerHelper.findBulkContainerOrNull(item);
        if (container == null) {
            debug("All containers null or none found");
            WorkerHelper.removeJob(item.getWurmId());
            return; // All containers full or none found.
        } else {
            debug("Container found: " + container.getName());
        }
        if (foundType == Tiles.Tile.TILE_CLAY.id) {
            templateId = 130;
        } else if (foundType == Tiles.Tile.TILE_PEAT.id) {
            templateId = 467;
        } else if (foundType == Tiles.Tile.TILE_TAR.id) {
            templateId = 153;
        } else if (foundType == Tiles.Tile.TILE_MOSS.id) {
            templateId = 479;
        }
        float ql = Server.rand.nextFloat() * Config.maxQualityLevel;
        int num = (int) (Config.defaultQuantity + Config.defaultQuantity *
                (Server.rand.nextFloat() * maxAmount));
        debug("Creating " + num + " items");
        Item toInsert = ItemFactory.createItem(templateId, ql, (byte) 0, null);
        debug("Weight: " + toInsert.getWeightGrams());
        // toInsert.setWeight(num * toInsert.getTemplate().getWeightGrams(), true);
        WorkerHelper.addItemToCrate(container, toInsert);
        container.updateModelNameOnGroundItem();
        DatabaseHelper.decreasePower(item, power);
    }
}
