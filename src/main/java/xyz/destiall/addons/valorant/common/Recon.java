package xyz.destiall.addons.valorant.common;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import xyz.destiall.addons.Addons;
import xyz.destiall.addons.utils.Scheduler;
import xyz.destiall.addons.valorant.Agent;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public interface Recon {
    double scanDuration();

    NamespacedKey scanId();

    Sound tagSound();

    NamespacedKey scannerKey = new NamespacedKey(Addons.INSTANCE, "scanner");

    default List<LivingEntity> scan(Player self, Location source, double radius) {
        List<LivingEntity> entities = self.getWorld().getNearbyEntities(source, radius, radius, radius)
                .stream()
                .filter(e -> e instanceof LivingEntity && e.getLocation().distance(source) <= radius)
                .map(e -> (LivingEntity) e)
                .collect(Collectors.toList());

        Iterator<LivingEntity> iter = entities.iterator();

        Agent agent = Addons.INSTANCE.getAgentManager().getAgentMap().get(self.getUniqueId());
        while (iter.hasNext()) {
            LivingEntity entity = iter.next();
            if (entity.getUniqueId().equals(self.getUniqueId())) {
                iter.remove();
                continue;
            }

            Vector direction = entity.getEyeLocation().toVector().subtract(source.toVector());
            double distance = direction.length();
            direction = direction.normalize();
            RayTraceResult result = self.getWorld().rayTrace(source, direction, distance, FluidCollisionMode.NEVER, true, 1d, e -> e instanceof BlockDisplay);
            if (result == null || (result.getHitEntity() != null && result.getHitEntity().getUniqueId().equals(entity.getUniqueId()))) {
                PersistentDataContainer data = entity.getPersistentDataContainer();
                if (data.has(scanId())) {
                    iter.remove();
                    continue;
                }

                if (entity instanceof Player) {
                    ((Player) entity).playSound(entity, tagSound(), 1f, 1f);
                }

                if (!entity.isGlowing()) {
                    data.set(scanId(), PersistentDataType.BOOLEAN, true);
                    entity.setGlowing(true);

                    Scheduler.Task task = new Scheduler.TaskRunnable() {
                        @Override
                        public void run() {
                            Addons.INSTANCE.getLogger().info("Unset scanned");
                            entity.setGlowing(false);
                            data.remove(scanId());
                            if (agent != null) {
                                agent.getTasks().removeIf(t -> t.getExternalId() == this.getExternalId());
                            }
                        }

                        @Override
                        public synchronized void cancel() throws IllegalStateException {
                            Addons.INSTANCE.getLogger().info("Cancelled task from Recon");
                            entity.setGlowing(false);
                            data.remove(scanId());
                            super.cancel();
                        }
                    }.runTaskLater(Addons.scheduler, entity, (long) scanDuration() * 20);
                    if (agent != null) {
                        agent.getTasks().add(task);
                    }
                }
            }
        }

        return entities;
    }
}
