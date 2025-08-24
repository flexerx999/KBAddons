package xyz.destiall.addons.valorant;

import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import xyz.destiall.addons.Addons;
import xyz.destiall.addons.utils.Effects;
import xyz.destiall.addons.valorant.common.Recon;

import java.util.ArrayList;
import java.util.List;

public class Sova extends Agent implements Recon {
    private final double scanSpeed = 0.75d;
    private final double scanRadius = 20;
    public static final NamespacedKey scannedKey = new NamespacedKey(Addons.INSTANCE, "sova_scanned");

    public Sova(Player player) {
        super(player);
    }

    public void recon(Location origin, Projectile dart) {
        int scans = dart.getPersistentDataContainer().get(Recon.scannerKey, PersistentDataType.INTEGER);
        if (scans == 0) {
            dart.remove();
            Addons.INSTANCE.getAgentManager().unsetAgent(self);
            return;
        }

        BukkitTask task = new BukkitRunnable() {
            final AtomicDouble current = new AtomicDouble(0);
            private final List<Integer> topStop = new ArrayList<>();
            private final List<Integer> bottomStop = new ArrayList<>();

            @Override
            public void run() {
                double radius = current.addAndGet(scanSpeed);
                if (radius > scanRadius) {
                    dart.getPersistentDataContainer().set(Recon.scannerKey, PersistentDataType.INTEGER, scans - 1);
                    getTasks().removeIf(t -> t.getTaskId() == this.getTaskId());
                    cancel();

                    recon(origin, dart);
                    return;
                }

                int top = 0;
                int bottom = 0;
                for (double height = 0; height <= radius; height += Math.PI / 5) {
                    for (double theta = -Math.PI; theta <= Math.PI; theta += Math.PI / 8) {
                        if (topStop.contains(top)) {
                            top++;
                            continue;
                        }
                        double x = radius * Math.cos(theta);
                        double z = radius * Math.sin(theta);
                        Location loc = origin.clone().add(x, height, z);
                        Block block = loc.getBlock();
                        if (block.isPassable() || block.isEmpty()) {
                            Effects.spawnRecon(loc);
                        } else {
                            topStop.add(top);
                        }
                        top++;
                    }

                    for (double theta = -Math.PI; theta <= Math.PI; theta += Math.PI / 8) {
                        if (bottomStop.contains(bottom)) {
                            bottom++;
                            continue;
                        }
                        double x = radius * Math.cos(theta);
                        double z = radius * Math.sin(theta);
                        Location loc = origin.clone().add(x, -height, z);
                        Block block = loc.getBlock();
                        if (block.isPassable() || block.isEmpty()) {
                            Effects.spawnRecon(loc);
                        } else {
                            bottomStop.add(bottom);
                        }
                        bottom++;
                    }
                }

                scan(self, origin, radius);
            }
        }.runTaskTimer(Addons.INSTANCE, 0L, 1L);

        getTasks().add(task);
    }

    //@EventHandler
    //public void onProjectileRemove(EntityRemoveEvent e) {
    //    if (!(e.getEntity() instanceof Projectile))
    //        return;
    //}

    @Override
    public double scanDuration() {
        return 1.75;
    }

    @Override
    public NamespacedKey scanId() {
        return scannedKey;
    }

    @Override
    public Sound tagSound() {
        return Sound.ENTITY_ARROW_HIT_PLAYER;
    }
}
