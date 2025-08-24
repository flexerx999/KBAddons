package xyz.destiall.addons.valorant.packet;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public interface ItemPacket {

    default void createFor(Player player) {}

    void teleport(Location location);

    void setItem(ItemStack item);

    void setGravity(boolean gravity);

    void remove();

    void rotate(double degrees, Vector axis);

    void scale(double scale);

    Location location();
}
