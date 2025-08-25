package xyz.destiall.addons.valorant.common;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import xyz.destiall.addons.Addons;
import xyz.destiall.addons.utils.Scheduler;
import xyz.destiall.addons.valorant.Agent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public interface Flasher {
    void flash(Player self, Location source, boolean leftClick);

    boolean selfFlash();

    NamespacedKey flashedKey = new NamespacedKey(Addons.INSTANCE, "flashed");

    Map<UUID, Scheduler.Task> flashed = new HashMap<>();

    default ItemStack flashItem() {
        ItemStack item = new ItemStack(Material.CARVED_PUMPKIN);
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(1);
        item.setItemMeta(meta);
        return item;
    }

    double flashDuration();

    default void flashOut(Player self, Location source) {
        List<Player> players = self.getWorld().getNearbyEntities(source, 50, 50, 50)
                .stream()
                .filter(e -> e instanceof Player)
                .map(e -> (Player) e)
                .collect(Collectors.toList());

        Agent agent = Addons.INSTANCE.getAgentManager().getAgentMap().get(self.getUniqueId());
        for (Player player : players) {
            if (player.getUniqueId().equals(self.getUniqueId()) && !selfFlash()) {
                continue;
            }

            Vector direction = player.getEyeLocation().toVector().subtract(source.toVector());
            double distance = direction.length();
            direction = direction.normalize();
            RayTraceResult result = self.getWorld().rayTrace(source, direction, distance, FluidCollisionMode.NEVER, true, 1d, e -> e instanceof LivingEntity || e instanceof BlockDisplay);
            if (result == null || (result.getHitEntity() != null && result.getHitEntity().getUniqueId().equals(player.getUniqueId()))) {
                Vector forward = player.getLocation().getDirection();
                direction = direction.clone().multiply(-1);
                double dot = forward.dot(direction);
                if (dot > 0.6d) {
                    player.addPotionEffect(PotionEffectType.BLINDNESS.createEffect((int) (flashDuration() * 20) + 10, 1));
                    //ItemStack helmet = null;
                    if (!player.getPersistentDataContainer().has(flashedKey)) {
                        //helmet = player.getInventory().getHelmet();
                        //player.getInventory().setHelmet(flashItem());
                        player.getPersistentDataContainer().set(flashedKey, PersistentDataType.BOOLEAN, true);
                    }

                    //ItemStack finalHelmet = helmet;
                    Scheduler.Task existing = flashed.get(player.getUniqueId());
                    if (existing != null) {
                        existing.cancel();
                        if (agent != null) {
                            agent.getTasks().removeIf(t -> t.getExternalId() == existing.getExternalId());
                        }
                    }
                    Scheduler.Task task = new Scheduler.TaskRunnable() {
                        @Override
                        public void run() {
                            Addons.INSTANCE.getLogger().info("Unset flashed");
                            //player.getInventory().setHelmet(finalHelmet);
                            player.getPersistentDataContainer().remove(flashedKey);
                            flashed.remove(player.getUniqueId());
                            if (agent != null) {
                                agent.getTasks().removeIf(t -> t.getExternalId() == this.getExternalId());
                            }
                        }

                        @Override
                        public synchronized void cancel() throws IllegalStateException {
                            Addons.INSTANCE.getLogger().info("Cancelled task from Flasher");
                            //player.getInventory().setHelmet(finalHelmet);
                            player.getPersistentDataContainer().remove(flashedKey);
                            super.cancel();
                        }
                    }.runTaskLater(Addons.scheduler, player, (long) (flashDuration() * 20) - 10);
                    flashed.put(player.getUniqueId(), task);
                    if (agent != null) {
                        agent.getTasks().add(task);
                    }
                }
            }
        }
    }
}
