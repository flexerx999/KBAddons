package xyz.destiall.addons.valorant.packet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public interface BlockPacket {

    default void createFor(Player player) {}

    void teleport(Location location);

    void setBlock(Material material);

    void setGravity(boolean gravity);

    void remove();

    void rotate(double degrees, Vector axis);

    void scale(double scale);
}
