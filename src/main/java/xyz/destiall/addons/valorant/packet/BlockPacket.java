package xyz.destiall.addons.valorant.packet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public interface BlockPacket {

    default void createFor(Player player) {}

    void teleport(Location location);

    void setBlock(Material material);

    void setGravity(boolean gravity);

    void remove();

    Location location();
}
