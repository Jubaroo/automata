package org.takino.mods;

import com.wurmonline.server.items.ItemList;

import java.util.HashMap;
import java.util.Map;

import static org.takino.mods.Automata.debug;

public enum ToolType {
    BUCKET(9), // Milks nearby cows into biggest available container
    SPATULA(8), // Consumes clay, makes bricks
    TROWEL(7), // Combines clay + sand into mortar
    SHOVEL(6), // Digs resources from nearby tiles
    HATCHET(5), // Chops trees in few tiles surrounding it
    PICKAXE(4), // Mines vein nearby
    CHISEL(3), // Consumes shards, makes bricks
    SAW(2), // Consumes logs, makes planks
    CARVING_KNIFE(1), // Consumes logs, makes shafts
    NONE(0) //
    ;
    private static Map<Integer,ToolType> toolTypes = new HashMap<>();

    public int id;
    ToolType(int i) {
        id=i;
    }
    static {
        for (ToolType t: ToolType.values()) {
            toolTypes.put(t.id, t);
        }
    }

    public static String getJobString(ToolType type){
        if (type == ToolType.SHOVEL) {
            return "digging nearby resources";
        }else if (type == ToolType.CHISEL){
            return "creating bricks from shards";
        }
        return "doing something";
    }

    public static ToolType getToolType(int id) {
        debug("God id: " + id);
        return toolTypes.get(id);
    }

    private static int[] getImplementedToolsList() {
        return new int[] {ItemList.shovel, ItemList.stoneChisel};
    }

    public static boolean supports(int templateid) {
        for (int id: getImplementedToolsList()) {
            debug("Is " + id + " equal to " + templateid + "?");
            if (id==templateid) {
                return true;
            }
        }
        return false;
    }
}