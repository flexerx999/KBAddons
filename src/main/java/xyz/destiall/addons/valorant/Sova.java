package xyz.destiall.addons.valorant;

import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import xyz.destiall.addons.Addons;
import xyz.destiall.addons.utils.Effects;
import xyz.destiall.addons.utils.Scheduler;
import xyz.destiall.addons.valorant.common.Recon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Sova extends Agent implements Recon {
    private final double scanSpeed = 0.75d;
    private final double scanRadius = 20;
    private final Map<Projectile, Scheduler.Task> reconDarts;

    public static final NamespacedKey scannedKey = new NamespacedKey(Addons.INSTANCE, "sova_scanned");

    public Sova(Player player) {
        super(player);
        this.reconDarts = new HashMap<>();
    }

    public void recon(Location origin, Projectile dart) {
        int scans = dart.getPersistentDataContainer().getOrDefault(Recon.scannerKey, PersistentDataType.INTEGER, 0);
        if (scans == 0) {
            dart.remove();
            reconDarts.remove(dart);
            Addons.INSTANCE.getAgentManager().unsetAgent(self);
            return;
        }

        List<Location> ends = new ArrayList<>();
        for (double phi = 0; phi <= Math.PI; phi += Math.PI / 24) {
            for (double theta = 0; theta <= 2 * Math.PI; theta += Math.PI / 24) {
                double x = Math.cos(theta) * Math.sin(phi);
                double y = Math.cos(phi);
                double z = Math.sin(theta) * Math.sin(phi);
                Vector dir = new Vector(x, y, z);
                Location start = origin.clone();

                RayTraceResult res = start.getWorld().rayTraceBlocks(start, dir, scanRadius, FluidCollisionMode.NEVER, true, null);
                ends.add(res != null ? res.getHitPosition().toLocation(start.getWorld()) : start.clone().add(dir.normalize().multiply(scanRadius)));
            }
        }

        Scheduler.Task task = new Scheduler.TaskRunnable() {
            final AtomicDouble current = new AtomicDouble(0);

            @Override
            public void run() {
                double radius = current.addAndGet(scanSpeed);
                if (radius > scanRadius) {
                    dart.getPersistentDataContainer().set(Recon.scannerKey, PersistentDataType.INTEGER, scans - 1);
                    getTasks().removeIf(t -> t.getExternalId() == this.getExternalId());
                    cancel();
                    recon(origin, dart);
                    return;
                }

                for (Location end : ends) {
                    Vector distance = end.toVector().subtract(origin.toVector());
                    Vector dir = distance.normalize();
                    Vector forward = dir.multiply(radius);
                    Location effectLoc;
                    if (forward.lengthSquared() > distance.lengthSquared()) {
                        effectLoc = end;
                    } else {
                        effectLoc = origin.clone().add(forward);
                    }
                    Effects.spawnRecon(effectLoc);
                }

                /**
                // base effect solution
                int heightIndex = 0;
                for (double height = 0; height <= radius; height += Math.PI / 5) {
                    heightIndex++;
                    int thetaIndex = 0;
                    for (double theta = -Math.PI; theta <= Math.PI; theta += Math.PI / 8) {
                        thetaIndex++;
                        double x = radius * Math.cos(theta);
                        double z = radius * Math.sin(theta);
                        Location loc = origin.clone().add(x, 0, z);
                        Collection<Integer> thetas = stop.get(heightIndex);
                        if (thetas == null || !thetas.contains(thetaIndex)) {
                            loc.setY(origin.getY() + height);
                            Block topBlock = loc.getBlock();
                            if (topBlock.isPassable() || topBlock.isEmpty()) {
                                Effects.spawnRecon(loc);
                            } else {
                                stop.putIfAbsent(heightIndex, new ArrayList<>());
                                stop.get(heightIndex).add(thetaIndex);
                            }
                        }

                        loc = loc.clone();
                        thetas = stop.get(-heightIndex);
                        if (thetas == null || !thetas.contains(thetaIndex)) {
                            loc.setY(origin.getY() - height);
                            Block botBlock = loc.getBlock();
                            if (botBlock.isPassable() || botBlock.isEmpty()) {
                                Effects.spawnRecon(loc);
                            } else {
                                stop.putIfAbsent(-heightIndex, new ArrayList<>());
                                stop.get(-heightIndex).add(thetaIndex);
                            }
                        }
                    }
                }
                 **/
                scan(self, origin, radius);
            }
        }.runTaskTimer(Addons.scheduler, origin, 0L, 1L);

        reconDarts.put(dart, task);
        getTasks().add(task);
    }

    @EventHandler
    public void onProjectileRemove(EntityRemoveEvent e) {
        if (!(e.getEntity() instanceof Projectile))
            return;

        Projectile proj = (Projectile) e.getEntity();
        if (!reconDarts.containsKey(proj))
            return;

        Scheduler.Task task = reconDarts.get(proj);
        task.cancel();
        reconDarts.remove(proj);
        Agent agent = Addons.INSTANCE.getAgentManager().getAgentMap().get(((Player) proj.getShooter()).getUniqueId());
        agent.getTasks().removeIf(t -> t.getExternalId() == task.getExternalId());
        Addons.INSTANCE.getAgentManager().unsetAgent(agent.self);
    }

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
