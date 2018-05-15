package org.takino.mods;

import java.util.HashMap;
import java.util.Map;

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
        Automata.debug("God id: " + id);
        return toolTypes.get(id);
    }
}