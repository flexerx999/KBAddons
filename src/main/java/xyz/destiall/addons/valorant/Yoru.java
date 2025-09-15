package xyz.destiall.addons.valorant;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import xyz.destiall.addons.Addons;
import xyz.destiall.addons.utils.Effects;
import xyz.destiall.addons.utils.Scheduler;
import xyz.destiall.addons.valorant.common.Flasher;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static xyz.destiall.addons.listeners.FunListener.getBlockFace;

public class Yoru extends Agent implements Flasher {
    public static final NamespacedKey yoruFlashed = new NamespacedKey(Addons.INSTANCE, "yoru_flash");
    public static final NamespacedKey flashId = new NamespacedKey(Addons.INSTANCE, "yr_flash_task");
    private final Map<Integer, Snowball> flashes = new ConcurrentHashMap<>();

    public Yoru(Player player) {
        super(player);
    }

    @Override
    public void flash(Player self, Location source, boolean leftClick) {
        Snowball snowball = self.launchProjectile(Snowball.class);
        snowball.setShooter(self);
        snowball.setItem(new ItemStack(Material.HEART_OF_THE_SEA));
        snowball.getPersistentDataContainer().set(yoruFlashed, PersistentDataType.STRING, "yoru");
        snowball.setBounce(true);
        snowball.setGravity(true);
        Vector forward = self.getLocation().getDirection();
        snowball.setVelocity(forward.multiply(2));
        tasks.add(new Scheduler.TaskRunnable() {
            int ticks;
            @Override
            public void run() {
                if (!snowball.getPersistentDataContainer().has(flashId)) {
                    snowball.getPersistentDataContainer().set(flashId, PersistentDataType.INTEGER, this.getExternalId());
                    flashes.put(this.getExternalId(), snowball);
                }

                final Snowball currentSnowball = flashes.get(this.getExternalId());
                if (ticks == 20 * flashDuration()) {
                    this.cancel();
                    flashOut(self, currentSnowball.getLocation());
                    tasks.removeIf(t -> t.getExternalId() == this.getExternalId());
                    //Addons.INSTANCE.getAgentManager().unsetAgent(self);
                    currentSnowball.remove();
                    Effects.spawnCrit(currentSnowball.getLocation());
                    flashes.remove(this.getExternalId());
                    return;
                }

                ticks++;
            }
        }.runTaskTimer(Addons.scheduler, snowball.getLocation(), 0L, 1L));
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile proj = event.getEntity();
        ProjectileSource shooter = proj.getShooter();
        if (!(shooter instanceof Player) || !((Player) shooter).getUniqueId().equals(self.getUniqueId()))
            return;

        if (!(proj instanceof Snowball))
            return;

        PersistentDataContainer container = proj.getPersistentDataContainer();
        if (!container.getOrDefault(yoruFlashed, PersistentDataType.STRING, "null").equalsIgnoreCase("yoru"))
            return;

        event.setCancelled(true);
        Vector forward = proj.getVelocity();
        final double magnitude = Math.sqrt(Math.pow(forward.getX(), 2) + Math.pow(forward.getY(), 2) + Math.pow(forward.getZ(), 2));
        BlockFace blockFace = getBlockFace(event, proj, forward);
        if (blockFace != null) {
            if (blockFace == BlockFace.SELF) {
                blockFace = BlockFace.UP;
            }
            proj.remove();
            Vector N = new Vector(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ());
            double dotProduct = forward.dot(N);
            Vector u = N.multiply(dotProduct).multiply(2);

            Vector newDirection = forward.subtract(u);
            Snowball snowball = proj.getWorld().spawn(proj.getLocation(), Snowball.class);
            snowball.setItem(new ItemStack(Material.MAGMA_CREAM));
            snowball.setShooter(self);
            snowball.getPersistentDataContainer().set(yoruFlashed, PersistentDataType.STRING, "yoru");
            snowball.setBounce(true);
            snowball.setGravity(false);
            snowball.setVelocity(newDirection.normalize().multiply(magnitude));
            int taskId = flashes.entrySet().stream().filter(entry -> entry.getValue() == proj).findFirst().orElse(null).getKey();
            snowball.getPersistentDataContainer().set(flashId, PersistentDataType.INTEGER, taskId);
            flashes.put(taskId, snowball);
        }
    }

    @Override
    public boolean selfFlash() {
        return true;
    }

    @Override
    public double flashDuration() {
        return 1.5f;
    }
}
