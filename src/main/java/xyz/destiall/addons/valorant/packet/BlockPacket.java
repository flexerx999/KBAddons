package xyz.destiall.addons.valorant.packet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;

public interface BlockPacket {

    default void createFor() {}

    void teleport(Location location);

    void setBlock(Material material);

    void setGravity(boolean gravity);

    void remove();

    void rotate(double degrees, Vector axis);

    void scale(double scale);

    void translate(float x, float y, float z);

    static BlockPacket create(Location location) {
        return new PacketEventBlockPacket(location);
    }
}
