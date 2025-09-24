package xyz.destiall.addons.managers;

import ga.strikepractice.StrikePractice;
import ga.strikepractice.arena.Arena;
import ga.strikepractice.arena.DefaultCachedBlockChange;
import ga.strikepractice.fights.Fight;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class StrikeManager {
    private StrikeManager() {}

    public static Arena getArena(Location location) {
        return StrikePractice.getAPI().getArenas().stream().filter(a -> isInArena(a, location)).findFirst().orElse(null);
    }

    public static boolean isInArena(Arena arena, Location location) {
        if (arena == null) return false;
        Location p1 = arena.getCorner1() == null ? arena.getLoc1() : arena.getCorner1();
        Location p2 = arena.getCorner2() == null ? arena.getLoc2() : arena.getCorner2();
        if (p1 == null || p2 == null) return false;

        double maxX = Math.max(p1.getX(), p2.getX());
        double maxY = Math.max(p1.getY(), p2.getY());
        double maxZ = Math.max(p1.getZ(), p2.getZ());

        double minX = Math.min(p1.getX(), p2.getX());
        double minY = Math.min(p1.getY(), p2.getY());
        double minZ = Math.min(p1.getZ(), p2.getZ());

        return  location.getX() <= maxX && location.getX() >= minX &&
                location.getY() <= maxY && location.getY() >= minY &&
                location.getZ() <= maxZ && location.getZ() >= minZ;
    }

    public static void addBlockChange(Block block) {
        Arena arena = getArena(block.getLocation());
        if (arena == null) return;
        Fight fight = arena.getCurrentFight();
        if (fight != null) {
            fight.addBlockChange(new DefaultCachedBlockChange(block.getLocation(), block));
        }
    }
}
