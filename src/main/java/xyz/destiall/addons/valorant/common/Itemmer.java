package xyz.destiall.addons.valorant.common;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import xyz.destiall.addons.valorant.packet.ItemPacket;

public interface Itemmer {
    default ItemPacket itemPacket(Location location, ItemStack item) {
        ItemPacket as = ItemPacket.create(location);
        as.setItem(item);
        as.setGravity(false);
        as.scale(0.25f);
        as.rotate(90, new Vector(0, 1, 0));
        as.rotate(45, new Vector(0, 0, 1));
        return as;
    }
}
