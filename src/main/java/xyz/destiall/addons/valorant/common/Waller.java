package xyz.destiall.addons.valorant.common;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.destiall.addons.Addons;
import xyz.destiall.addons.valorant.packet.ServerBlockDisplay;
import xyz.destiall.addons.valorant.packet.WallPacket;

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

    default void wallUp(Player source, Location origin) {
        int j = 0;
        for (double i = 0; i <= wallLength(); i += 0.75f) {
            final double ii = i;
            final Set<WallPacket> asList = new HashSet<>();
            long ticks = (long) j++ * wallSpeed();
            new BukkitRunnable() {
                @Override
                public void run() {
                    Vector dir = wallDirection(source).clone().normalize();
                    Vector vect = new Vector(dir.getX() * ii, 0, dir.getZ() * ii);
                    Location location = origin.clone().add(vect);
                    location.setDirection(dir);
                    int minDrop = 5;
                    int y = source.getWorld().getHighestBlockYAt(location.getBlockX(), location.getBlockZ());
                    if (y <= origin.getY() + minDrop || y >= origin.getY() - minDrop) {
                        location.setY(y + 0.4d);
                    }
                    for (double j = 0; j < wallHeight(); j += 0.25f) {
                        WallPacket as1 = wallPacket(location.clone().add(0d, j, 0d).subtract(dir.getX() * 0.25f, 0, dir.getZ() * 0.25f));
                        as1.createFor(source);
                        asList.add(as1);

                        WallPacket as2 = wallPacket(location.clone().add(0d, j, 0d).subtract(dir.getX() * 0.5f, 0, dir.getZ() * 0.5f));
                        as2.createFor(source);
                        asList.add(as2);

                        WallPacket as3 = wallPacket(location.clone().add(0d, j, 0d));
                        as3.createFor(source);
                        asList.add(as3);
                    }
                }
            }.runTaskLater(Addons.INSTANCE, ticks);

            new BukkitRunnable() {
                @Override
                public void run() {
                    for (WallPacket packet : asList) {
                        packet.remove();
                    }
                }

                @Override
                public synchronized void cancel() throws IllegalStateException {
                    for (WallPacket packet : asList) {
                        packet.remove();
                    }
                }
            }.runTaskLater(Addons.INSTANCE, (long) (wallDuration() * 20d) + ticks);
        }
    }

    default WallPacket wallPacket(Location location) {
        //PacketBlockDisplay as = new PacketBlockDisplay(location);
        ServerBlockDisplay as = new ServerBlockDisplay(location);
        as.setBlock(wallMaterial());
        as.setGravity(false);
        as.setScale(0.25f);
        return as;
    }
}
