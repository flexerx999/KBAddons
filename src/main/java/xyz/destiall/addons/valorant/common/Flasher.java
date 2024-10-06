package xyz.destiall.addons.valorant.common;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import xyz.destiall.addons.Addons;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public interface Flasher {
    void flash(Player self, Location source);

    boolean selfFlash();

    NamespacedKey flashedKey = new NamespacedKey(Addons.INSTANCE, "flashed");

    Map<UUID, BukkitTask> flashed = new HashMap<>();

    default void flashOut(Player self, Location source) {
        List<Player> players = self.getWorld().getNearbyEntities(source, 50, 50, 50)
                .stream()
                .filter(e -> e instanceof Player)
                .map(e -> (Player) e)
                .collect(Collectors.toList());

        for (Player player : players) {
            if (player.getUniqueId().equals(self.getUniqueId()) && !selfFlash()) {
                continue;
            }

            Vector direction = player.getEyeLocation().toVector().subtract(source.toVector());
            double distance = direction.length();
            direction = direction.normalize();
            RayTraceResult result = self.getWorld().rayTrace(source, direction, distance, FluidCollisionMode.NEVER, true, 1d, e -> e instanceof LivingEntity || e instanceof BlockDisplay);
            Addons.INSTANCE.getLogger().info("Raytrace: " + result);
            if (result == null || (result.getHitEntity() != null && result.getHitEntity().getUniqueId().equals(player.getUniqueId()))) {
                Vector forward = player.getLocation().getDirection();
                direction = direction.clone().multiply(-1);
                double dot = forward.dot(direction);
                Addons.INSTANCE.getLogger().info("Flash dot: " + dot);
                if (dot > 0.6d) {
                    player.addPotionEffect(PotionEffectType.BLINDNESS.createEffect(100, 1));
                    ItemStack helmet = null;
                    if (!player.getPersistentDataContainer().has(flashedKey)) {
                        helmet = player.getInventory().getHelmet();
                        player.getInventory().setHelmet(new ItemStack(Material.YELLOW_CONCRETE));
                        player.getPersistentDataContainer().set(flashedKey, PersistentDataType.BOOLEAN, true);
                    }

                    ItemStack finalHelmet = helmet;
                    BukkitTask existing = flashed.get(player.getUniqueId());
                    if (existing != null) {
                        existing.cancel();
                    }
                    BukkitTask task = new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.getInventory().setHelmet(finalHelmet);
                            player.getPersistentDataContainer().remove(flashedKey);
                            flashed.remove(player.getUniqueId());
                        }

                        @Override
                        public synchronized void cancel() throws IllegalStateException {
                            super.cancel();
                            player.getInventory().setHelmet(finalHelmet);
                            player.getPersistentDataContainer().remove(flashedKey);
                        }
                    }.runTaskLater(Addons.INSTANCE, 100L);
                    flashed.put(player.getUniqueId(), task);
                }
            }
        }
    }
}
