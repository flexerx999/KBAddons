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
import xyz.destiall.addons.valorant.common.Waller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static xyz.destiall.addons.listeners.FunListener.getBlockFace;

public class Phoenix extends Agent implements Flasher, Waller {
    private Vector prevWallDirection = null;
    public static final NamespacedKey phoenixFlashed = new NamespacedKey(Addons.INSTANCE, "phoenix_flash");
    public static final NamespacedKey flashId = new NamespacedKey(Addons.INSTANCE, "flash_task");
    private final List<Material> wallMaterials = Arrays.asList(Material.RED_CONCRETE, Material.ORANGE_CONCRETE, Material.RED_TERRACOTTA, Material.ORANGE_TERRACOTTA, Material.ORANGE_GLAZED_TERRACOTTA);
    private final Map<Integer, Snowball> flashes = new ConcurrentHashMap<>();

    public Phoenix(Player player) {
        super(player);
    }

    @Override
    public void flash(Player self, Location source, boolean leftClick) {
        Snowball snowball = self.launchProjectile(Snowball.class);
        snowball.setShooter(self);
        snowball.setItem(new ItemStack(Material.MAGMA_CREAM));
        snowball.getPersistentDataContainer().set(phoenixFlashed, PersistentDataType.STRING, "phoenix");
        snowball.setBounce(true);
        snowball.setGravity(false);
        Vector forward = self.getLocation().getDirection();
        Vector up = forward.clone().crossProduct(new Vector(0, 1, 0)).crossProduct(forward).normalize();

        snowball.setVelocity(forward.multiply(0.75f));
        tasks.add(new Scheduler.TaskRunnable() {
            int ticks;
            final int max_ticks = 10;
            final int half_ticks = max_ticks / 2;
            @Override
            public void run() {
                if (!snowball.getPersistentDataContainer().has(flashId)) {
                    snowball.getPersistentDataContainer().set(flashId, PersistentDataType.INTEGER, this.getExternalId());
                    flashes.put(this.getExternalId(), snowball);
                }

                final Snowball currentSnowball = flashes.get(this.getExternalId());
                if (ticks == max_ticks) {
                    this.cancel();
                    flashOut(self, currentSnowball.getLocation());
                    tasks.removeIf(t -> t.getExternalId() == this.getExternalId());
                    Addons.INSTANCE.getAgentManager().unsetAgent(self);
                    currentSnowball.remove();
                    Effects.spawnCrit(currentSnowball.getLocation());
                    flashes.remove(this.getExternalId());
                    return;
                }

                ticks++;
                if (ticks > half_ticks) {
                    float angle = Addons.lerp(0f, (float) Math.toRadians(45), (float) (ticks - half_ticks) / half_ticks);
                    currentSnowball.setVelocity(currentSnowball.getVelocity().rotateAroundAxis(up, leftClick ? angle : -angle));
                }

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
        if (!container.getOrDefault(phoenixFlashed, PersistentDataType.STRING, "null").equalsIgnoreCase("phoenix"))
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
            snowball.getPersistentDataContainer().set(phoenixFlashed, PersistentDataType.STRING, "phoenix");
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
        return 2;
    }

    @Override
    public void wall(Player source, Location origin) {
        prevWallDirection = wallDirection(source);
        wallUp(source, origin.clone());
    }

    @Override
    public List<Material> wallMaterials() {
        return wallMaterials;
    }

    @Override
    public int wallHeight() {
        return 4;
    }

    @Override
    public int wallLength() {
        return 8;
    }

    @Override
    public int wallSpeed() {
        return 1;
    }

    @Override
    public double wallDuration() {
        return 7;
    }

    // a + (b - a) * t
    @Override
    public Vector wallDirection(Player source) {
        Vector now = source.getLocation().getDirection().setY(0).normalize();
        if (prevWallDirection == null)
            return now;

        double degree = 0.5d;
        double x = prevWallDirection.getX() + (now.getX() - prevWallDirection.getX()) * degree;
        double z = prevWallDirection.getZ() + (now.getZ() - prevWallDirection.getZ()) * degree;

        while (new Vector(x, 0d, z).dot(prevWallDirection) < 0.5f) {
            degree -= 0.01d;
            x = prevWallDirection.getX() + (now.getX() - prevWallDirection.getX()) * degree;
            z = prevWallDirection.getZ() + (now.getZ() - prevWallDirection.getZ()) * degree;
        }
        return new Vector(x, 0d, z);
    }
}
