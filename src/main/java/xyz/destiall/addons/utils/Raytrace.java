package xyz.destiall.addons.utils;

import org.bukkit.block.Block;

public class Raytrace {
    public static boolean canPassThrough(Block block) {
        try {
            Class.forName("com.sk89q.worldedit.blocks.BlockType");
            return com.sk89q.worldedit.blocks.BlockType.canPassThrough(block.getType().getId());
        } catch (Exception ignored) {}
        return block.isLiquid() || block.isEmpty() || !block.getType().isSolid();
    }
}
