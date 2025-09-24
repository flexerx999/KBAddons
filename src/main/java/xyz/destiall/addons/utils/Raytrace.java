package xyz.destiall.addons.utils;

import org.bukkit.block.Block;

public class Raytrace {
    public static boolean canPassThrough(Block block) {
        return block.isLiquid() || block.isEmpty() || !block.getType().isSolid();
    }
}
