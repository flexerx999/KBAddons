package xyz.destiall.addons.valorant.packet;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.destiall.addons.utils.Effects;

public class PacketEventBlockPacket extends PacketEventDisplayPacket implements BlockPacket {
    public PacketEventBlockPacket(Location location) {
        super(org.bukkit.entity.EntityType.BLOCK_DISPLAY, location);
    }

    @Override
    public void createFor(Player player) {
        for (Player p : player.getWorld().getNearbyPlayers(player.getLocation(), 60)) {
            users.add(Effects.API.getPlayerManager().getUser(p));
        }
        WrapperPlayServerSpawnEntity spawn = new WrapperPlayServerSpawnEntity(
                entityId,
                uuid,
                entityType,
                location,
                0f,
                0,
                new Vector3d()
        );
        sendPacket(spawn);
        sendMetadata();
    }

    @Override
    public void teleport(Location location) {
        this.location = SpigotConversionUtil.fromBukkitLocation(location);
        WrapperPlayServerEntityTeleport packet = new WrapperPlayServerEntityTeleport(entityId, this.location, gravity);
        sendPacket(packet);
    }

    @Override
    public void setBlock(Material material) {
        WrappedBlockState blockData = SpigotConversionUtil.fromBukkitBlockData(material.createBlockData());
        metadata.put(23, new EntityData<>(23, EntityDataTypes.BLOCK_STATE, blockData.getGlobalId()));
    }

    @Override
    public void remove() {
        WrapperPlayServerDestroyEntities packet = new WrapperPlayServerDestroyEntities(entityId);
        sendPacket(packet);
    }
}
