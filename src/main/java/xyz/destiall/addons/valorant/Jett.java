package xyz.destiall.addons.valorant;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import xyz.destiall.addons.Addons;
import xyz.destiall.addons.utils.Effects;
import xyz.destiall.addons.utils.Scheduler;
import xyz.destiall.addons.valorant.common.Itemmer;
import xyz.destiall.addons.valorant.packet.ItemPacket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Jett extends Agent implements Itemmer {
    private final Map<ItemPacket, Vector> hoveringKunais;
    private final ItemStack kunai;

    private final double maxDistance = 100;
    private final double kunaiDamage = 10f;
    private final float kunaiCollision = 0.3f;

    public Jett(Player player) {
        super(player);
        hoveringKunais = new ConcurrentHashMap<>();
        kunai = new ItemStack(Material.IRON_SWORD);
    }

    @Override
    public void unset() {
        for (ItemPacket knive : hoveringKunais.keySet()) {
            knive.remove();
        }

        hoveringKunais.clear();
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
        if (!e.getPlayer().getUniqueId().equals(self.getUniqueId()))
            return;

        if (hoveringKunais.isEmpty())
            return;

        throwAllKnives();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(PlayerInteractEvent e) {
        if (!e.getPlayer().getUniqueId().equals(self.getUniqueId()))
            return;

        if (hoveringKunais.isEmpty())
            return;

        if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
            throwSingleKnife();
            return;
        }

        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            throwAllKnives();
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!e.getPlayer().getUniqueId().equals(self.getUniqueId()))
            return;

        if (hoveringKunais.isEmpty())
            return;

        Location loc = bladesBaseLoc();
        for (Map.Entry<ItemPacket, Vector> entry : hoveringKunais.entrySet()) {
            Vector vector = entry.getValue();
            double yaw = Math.toRadians(-loc.getYaw());
            double xRotate = Math.cos(yaw) * vector.getX() + Math.sin(yaw) * vector.getZ();
            double zRotate = -Math.sin(yaw) * vector.getX() + Math.cos(yaw) * vector.getZ();
            entry.getKey().teleport(loc.clone().add(xRotate, 0, zRotate));
        }
    }

    public void throwSingleKnife() {
        ItemPacket k = hoveringKunais.keySet().stream().findAny().get();

        Vector dir = self.getLocation().getDirection();
        Location start = self.getEyeLocation().add(dir.clone().multiply(0.5f));
        RayTraceResult result = self.getWorld().rayTrace(start, dir, maxDistance, FluidCollisionMode.NEVER, true, kunaiCollision, en -> en instanceof LivingEntity && !en.getUniqueId().equals(self.getUniqueId()));
        Location end;
        if (result == null) {
            end = self.getEyeLocation().add(dir.clone().multiply(maxDistance));
        } else {
            Vector hit = result.getHitPosition();
            end = new Location(self.getWorld(), hit.getX(), hit.getY(), hit.getZ());
        }

        self.playSound(self, Sound.BLOCK_GROWING_PLANT_CROP, 1f, 1.5f);

        double distance = k.location().distance(end);
        double interval = distance / 5d;
        Vector vectorInterval = end.clone().subtract(k.location()).toVector().normalize().multiply(interval);
        while (vectorInterval.lengthSquared() < distance * distance) {
            Effects.spawnCrit(k.location().clone().add(vectorInterval));
            vectorInterval.add(vectorInterval);
        }

        if (result != null && result.getHitEntity() != null) {
            LivingEntity entity = (LivingEntity) result.getHitEntity();
            entity.damage(kunaiDamage, self);

            if (entity.getHealth() == 0) {
                unset();
                activateBlades();
                return;
            }
        }

        hoveringKunais.remove(k);
        k.remove();

        if (hoveringKunais.isEmpty()) {
            Addons.INSTANCE.getAgentManager().unsetAgent(self);
        }
    }

    public void throwAllKnives() {
        Vector dir = self.getLocation().getDirection();
        self.swingMainHand();
        for (ItemPacket k : hoveringKunais.keySet()) {
            Location start =  k.location().clone();
            Vector rand = dir.clone().add(new Vector((Math.random() - 0.5d) * 0.01, (Math.random() - 0.5d) * 0.01, (Math.random() - 0.5d) * 0.01)).normalize();
            RayTraceResult result = self.getWorld().rayTrace(k.location(), rand, maxDistance, FluidCollisionMode.NEVER, true, kunaiCollision, en -> en instanceof LivingEntity && !en.getUniqueId().equals(self.getUniqueId()));
            Location end;
            if (result == null) {
                end = k.location().clone().add(rand.clone().multiply(maxDistance));
            } else {
                Vector hit = result.getHitPosition();
                end = new Location(self.getWorld(), hit.getX(), hit.getY(), hit.getZ());
            }

            double distance = start.distance(end);
            double interval = distance / 5d;
            Vector vectorInterval = rand.clone().multiply(interval);
            Effects.spawnCrit(start);
            while (vectorInterval.lengthSquared() <= distance * distance) {
                Effects.spawnCrit(start.clone().add(vectorInterval));
                vectorInterval.add(vectorInterval);
            }

            if (result != null && result.getHitEntity() != null) {
                LivingEntity entity = (LivingEntity) result.getHitEntity();
                int ndt = entity.getNoDamageTicks();
                entity.setNoDamageTicks(0);
                entity.damage(kunaiDamage, self);
                Scheduler.Task task = new Scheduler.TaskRunnable() {
                    @Override
                    public void run() {
                        entity.setNoDamageTicks(ndt);
                        getTasks().removeIf(t -> t.getExternalId() == this.getExternalId());
                    }

                    @Override
                    public synchronized void cancel() throws IllegalStateException {
                        entity.setNoDamageTicks(ndt);
                        super.cancel();
                    }
                }.runTaskLater(Addons.scheduler, self, 1L);
                getTasks().add(task);
            }

            k.remove();
        }

        hoveringKunais.clear();
        Addons.INSTANCE.getAgentManager().unsetAgent(self);
    }

    public Location bladesBaseLoc() {
        Location loc = self.getLocation().add(0, self.getEyeHeight() * 0.75, 0);
        Vector dir = loc.getDirection().setY(0).normalize();
        loc.subtract(dir.clone().multiply(0.3));
        return loc;
    }

    public void activateBlades() {
        Location loc = bladesBaseLoc();
        for (double i = 0; i <= Math.PI; i += Math.PI / 4) {
            double x = Math.cos(i);
            double z = Math.sin(i);
            double yaw = Math.toRadians(-loc.getYaw());
            double xRotate = Math.cos(yaw) * x + Math.sin(yaw) * z;
            double zRotate = -Math.sin(yaw) * x + Math.cos(yaw) * z;
            ItemPacket as = itemPacket(loc.clone().add(xRotate, 0, zRotate), kunai);
            as.createFor(self);
            hoveringKunais.put(as, new Vector(x, 0, z));
        }
    }
}
