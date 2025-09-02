package xyz.destiall.addons.valorant.common;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import xyz.destiall.addons.Addons;
import xyz.destiall.addons.utils.Scheduler;
import xyz.destiall.addons.valorant.Agent;
import xyz.destiall.addons.valorant.packet.BlockPacket;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface Waller {
    void wall(Player source, Location origin);

    List<Material> wallMaterials();

    int wallHeight();

    int wallLength();

    int wallSpeed();

    double wallDuration();

    Vector wallDirection(Player source);

    default Material wallMaterial() {
        return wallMaterials().get((int) (Math.random() * wallMaterials().size()));
    }

    default void wallUp(Player self, Location origin) {
        int j = 0;
        Agent agent = Addons.INSTANCE.getAgentManager().getAgentMap().get(self.getUniqueId());
        for (double i = 0; i <= wallLength(); i += 0.75f) {
            final double ii = i;
            final Set<BlockPacket> asList = new HashSet<>();
            long ticks = (long) j++ * wallSpeed();
            Scheduler.Task wall = new Scheduler.TaskRunnable() {
                @Override
                public void run() {
                    Vector dir = wallDirection(self).clone().normalize();
                    Vector vect = new Vector(dir.getX() * ii, 0, dir.getZ() * ii);
                    Location location = origin.clone().add(vect);
                    location.setDirection(dir);
                    int minDrop = 5;
                    int y = self.getWorld().getHighestBlockYAt(location.getBlockX(), location.getBlockZ());
                    if (y <= origin.getY() + minDrop || y >= origin.getY() - minDrop) {
                        location.setY(y + 0.4d);
                    }

                    // Interpolate
                    for (double j = 0; j < wallHeight(); j += 0.25f) {
                        Location base = location.clone().add(0d, j, 0d);

                        BlockPacket as1 = wallPacket(base.clone().subtract(dir.getX() * 0.25f, 0, dir.getZ() * 0.25f));
                        as1.createFor();
                        asList.add(as1);

                        BlockPacket as2 = wallPacket(base.clone().subtract(dir.getX() * 0.5f, 0, dir.getZ() * 0.5f));
                        as2.createFor();
                        asList.add(as2);

                        BlockPacket as3 = wallPacket(base);
                        as3.createFor();
                        asList.add(as3);
                    }

                    if (agent != null) {
                        agent.getTasks().removeIf(t -> t.getExternalId() == this.getExternalId());
                    }
                }
            }.runTaskLater(Addons.scheduler, origin, ticks);
            if (agent != null) {
                agent.getTasks().add(wall);
            }

            Scheduler.Task expiry = new Scheduler.TaskRunnable() {
                @Override
                public void run() {
                    for (BlockPacket packet : asList) {
                        packet.remove();
                    }
                    if (agent != null) {
                        agent.getTasks().removeIf(t -> t.getExternalId() == this.getExternalId());
                    }
                }

                @Override
                public synchronized void cancel() throws IllegalStateException {
                    Addons.INSTANCE.getLogger().info("Cancelled task from Waller");
                    for (BlockPacket packet : asList) {
                        packet.remove();
                    }
                    super.cancel();
                }
            }.runTaskLater(Addons.scheduler, origin, (long) (wallDuration() * 20d) + ticks);
            if (agent != null) {
                agent.getTasks().add(expiry);
            }
        }
    }

    default BlockPacket wallPacket(Location location) {
        //PacketBlockDisplay as = new PacketBlockDisplay(location);
        BlockPacket as = BlockPacket.create(location);
        as.setBlock(wallMaterial());
        as.setGravity(false);
        as.scale(0.25f);
        return as;
    }
}
