package org.takino.mods;

import java.util.HashMap;
import java.util.Map;

public enum ToolType {
    SHOVEL(6), // Digs resources from nearby tiles
    HATCHET(5), // Chops trees in few tiles surrounding it
    PICKAXE(4), // Mines vein nearby
    CHISEL(3), // Consumes shards, makes bricks
    SAW(2), // Consumes logs, makes planks
    CARVING_KNIFE(1), // Consumes logs, makes shafts
    NONE(0) // Consumes clay, makes bricks
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

    public static ToolType getToolType(int id) {
        System.out.println("God id: " + id);
        return toolTypes.get(id);
    }
}