package xyz.destiall.addons.valorant.packet;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class PacketEventItemPacket extends PacketEventDisplayPacket implements ItemPacket {
    public PacketEventItemPacket(Location location) {
        super(org.bukkit.entity.EntityType.ITEM_DISPLAY, location);
    }

    @Override
    public void teleport(Location location) {
        this.location = SpigotConversionUtil.fromBukkitLocation(location);
        WrapperPlayServerEntityTeleport packet = new WrapperPlayServerEntityTeleport(entityId, this.location, gravity);
        sendPacket(packet);
    }

    @Override
    public void setItem(ItemStack item) {
        com.github.retrooper.packetevents.protocol.item.ItemStack itemData = SpigotConversionUtil.fromBukkitItemStack(item);
        metadata.put(23, new EntityData<>(23, EntityDataTypes.ITEMSTACK, itemData));
    }

    @Override
    public void remove() {
        WrapperPlayServerDestroyEntities packet = new WrapperPlayServerDestroyEntities(entityId);
        sendPacket(packet);
    }

    @Override
    public Location location() {
        return SpigotConversionUtil.toBukkitLocation(world, location);
    }
}
