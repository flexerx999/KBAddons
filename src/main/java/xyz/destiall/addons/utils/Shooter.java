package xyz.destiall.addons.utils;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
import xyz.destiall.addons.Addons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class Shooter {
    public static void shoot(Player shooter, Location origin, Vector direction, double damage) {
        if (!shooter.isOnline())
            return;

        Location current = origin.clone().add(direction);
        Vector dir = direction.clone().normalize().multiply(0.15);
        Effects.spawnSmoke(current);
        shooter.playSound(shooter.getEyeLocation(), Sound.BLOCK_LEVER_CLICK, 0.5f, 1);
        Collection<Entity> hitEntities = new ArrayList<>();
        while (Raytrace.canPassThrough(current.getBlock())) {
            if (origin.clone().subtract(current).length() > Addons.INSTANCE.getConfig().getInt("quake-gun-distance", 60)) break;
            current.add(dir);
            // Effects.spawnDust(current, 255, 255, 255);
            Effects.spawnCrit(current);
            hitEntities = getNearbyEntities(current, 0.5).stream().filter(e -> e != shooter && (!(e instanceof ArmorStand))).collect(Collectors.toList());
            if (!hitEntities.isEmpty())
                break;
        }
        Effects.spawnExplosion(current);
        if (!hitEntities.isEmpty()) {
            shooter.playSound(shooter.getEyeLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);
            for (Entity entity : hitEntities) {
                if (entity instanceof LivingEntity) {
                    LivingEntity live = (LivingEntity) entity;
                    live.damage(damage, shooter);
                } else if (entity instanceof Item) {
                    Item item = (Item) entity;
                    int id = item.getItemStack().getType().getId();
                    entity.getWorld().playEffect(entity.getLocation(), Effect.STEP_SOUND, id);
                    entity.remove();
                }
            }
        }
        getNearbyEntities(current, 40).forEach(e -> {
            if (e instanceof Player) {
                Player player = (Player) e;
                player.playSound(current, Sound.BLOCK_WOOD_BREAK, 0.5f, 1);
            }
        });
    }

    private static Collection<Entity> getNearbyEntities(Location location, double radius) {
        return location.getWorld().getNearbyEntities(location, radius, radius, radius);
    }
}
