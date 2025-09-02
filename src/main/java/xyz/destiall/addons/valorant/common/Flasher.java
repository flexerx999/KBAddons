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
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import xyz.destiall.addons.Addons;
import xyz.destiall.addons.utils.Scheduler;
import xyz.destiall.addons.valorant.packet.BlockPacket;

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
        List<LivingEntity> entities = source.getWorld().getNearbyEntities(source, 50, 50, 50)
                .stream()
                .filter(e -> e instanceof LivingEntity)
                .map(e -> (LivingEntity) e)
                .collect(Collectors.toList());

        for (LivingEntity livingEntity : entities) {
            if (livingEntity.getUniqueId().equals(self.getUniqueId()) && !selfFlash()) {
                continue;
            }

            Vector direction = livingEntity.getEyeLocation().toVector().subtract(source.toVector());
            double distance = direction.length();
            direction = direction.normalize();
            RayTraceResult result = self.getWorld().rayTrace(source, direction, distance, FluidCollisionMode.NEVER, true, 1d, e -> e instanceof LivingEntity || e instanceof BlockDisplay);
            if (result == null || (result.getHitEntity() != null && result.getHitEntity().getUniqueId().equals(livingEntity.getUniqueId()))) {
                Vector forward = livingEntity.getLocation().getDirection();
                direction = direction.clone().multiply(-1);
                double dot = forward.dot(direction);
                if (dot > 0.6d) {
                    livingEntity.addPotionEffect(PotionEffectType.BLINDNESS.createEffect(-1, 1));
                    //ItemStack helmet = null;
                    BlockPacket packet = BlockPacket.create(livingEntity.getEyeLocation());
                    packet.setBlock(Material.YELLOW_CONCRETE);
                    packet.setGravity(false);
                    packet.scale(0.6f);
                    packet.translate(-0.3f, -0.3f, -0.3f);
                    packet.createFor();
                    Addons.INSTANCE.getAgentManager().setFlashed(livingEntity.getUniqueId(), packet, flashDuration() + 0.2d);
                    //helmet = player.getInventory().getHelmet();
                    //player.getInventory().setHelmet(flashItem());
                }
            }
        }
    }
}
