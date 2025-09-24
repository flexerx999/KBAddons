package xyz.destiall.addons.listeners;

import me.wazup.kitbattle.Kitbattle;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashSet;

public class EggListener implements Listener {
    public static EggListener INSTANCE;
    public EggListener() {
        INSTANCE = this;
    }
    public final Collection<Egg> EGGS = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof Egg))
            return;

        Egg egg = (Egg) e.getEntity();
        if (!EGGS.contains(egg))
            return;

        Vector direction = egg.getVelocity().normalize();
        Location location = egg.getLocation();
        Block block = location.getBlock();
        BlockIterator b = new BlockIterator(location.getWorld(), location.toVector(), direction, 0, 3);
        Block blockBefore = location.getBlock();
        Block nextBlock = b.next();
        while (b.hasNext() && nextBlock.getType() == Material.AIR) {
            blockBefore = nextBlock;
            nextBlock = b.next();
        }
        BlockFace blockFace = nextBlock.getFace(blockBefore);
        if (blockFace != null) {
            EGGS.remove(egg);
            int bounces = 0;
            if (egg.hasMetadata("bounces")) {
                bounces = egg.getMetadata("bounces").get(0).asInt();
                egg.removeMetadata("bounces", Kitbattle.getInstance());
                if (bounces == xyz.destiall.addons.abilities.Egg.INSTANCE.bounces) {
                    egg.remove();
                    return;
                }
            }
            Egg entity = (Egg) block.getWorld().spawnEntity(location.add(0, 1, 0), xyz.destiall.addons.abilities.Egg.INSTANCE.type);
            if (blockFace == BlockFace.UP) {
                double x = Math.random() * 2 - 1;
                double z = Math.random() * 2 - 1;
                Vector v = new Vector(x, 2, z).normalize().multiply(xyz.destiall.addons.abilities.Egg.INSTANCE.speed);
                entity.setVelocity(v);
            } else {
                Vector N = new Vector(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ());
                double dotProduct = entity.getVelocity().dot(N);
                Vector u = N.multiply(dotProduct).multiply(2);
                Vector v = entity.getVelocity().clone().subtract(u).normalize().multiply(xyz.destiall.addons.abilities.Egg.INSTANCE.speed);
                entity.setVelocity(v);
            }
            //entity.setBounce(true);
            entity.setShooter(egg.getShooter());
            entity.setMetadata("bounces", new FixedMetadataValue(Kitbattle.getInstance(), ++bounces));
            //entity.setLastDamageCause(egg.getLastDamageCause());
            entity.setFallDistance(egg.getFallDistance());
            EGGS.add(entity);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent e) {
        if (e.getEntityType().equals(EntityType.CHICKEN)) {
            e.setCancelled(true);
        }
    }
}
