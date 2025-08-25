package xyz.destiall.addons.valorant.packet;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.Quaternion4f;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class PacketEventDisplayPacket {
    protected final int entityId;
    protected final EntityType entityType;
    protected final UUID uuid;
    protected final World world;
    protected final Collection<User> users;
    protected com.github.retrooper.packetevents.protocol.world.Location location;
    protected boolean gravity = false;
    protected Map<Integer, EntityData<?>> metadata;
    protected Transformation transformation;
    protected Matrix3f matrix;

    public PacketEventDisplayPacket(org.bukkit.entity.EntityType type, Location location) {
        this.entityId = SpigotReflectionUtil.generateEntityId();
        uuid = UUID.randomUUID();
        this.world = location.getWorld();
        this.location = SpigotConversionUtil.fromBukkitLocation(location);
        entityType = SpigotConversionUtil.fromBukkitEntityType(type);
        transformation = new Transformation(new Vector3f(0), new AxisAngle4f(), new Vector3f(1), new AxisAngle4f());
        matrix = new Matrix3f();
        metadata = new HashMap<>();

        // Interpolation metadata
        metadata.put(8, new EntityData<>(8, EntityDataTypes.INT, 1));
        metadata.put(9, new EntityData<>(9, EntityDataTypes.INT, 1));
        metadata.put(10, new EntityData<>(10, EntityDataTypes.INT, 1));

        this.users = new ArrayList<>();
    }

    public void setGravity(boolean gravity) {
        this.gravity = gravity;
    }

    public void rotate(double degrees, Vector axis) {
        Quaternionf quat = transformation.getLeftRotation()
                .rotateAxis((float) Math.toRadians(degrees), new Vector3f((float) axis.getX(), (float) axis.getY(), (float) axis.getZ()));
        Quaternion4f left = new Quaternion4f(quat.x, quat.y, quat.z, quat.w);
        metadata.put(13, new EntityData<>(13, EntityDataTypes.QUATERNION, left));
    }

    public void scale(double mul) {
        Vector3f s = transformation.getScale().mul((float) mul);
        com.github.retrooper.packetevents.util.Vector3f scale = new com.github.retrooper.packetevents.util.Vector3f(s.x, s.y, s.z);
        metadata.put(12, new EntityData<>(12, EntityDataTypes.VECTOR3F, scale));
    }

    protected void sendPacket(PacketWrapper<?> packet) {
        for (User user : users) {
            user.sendPacket(packet);
        }
    }

    protected void sendMetadata() {
        WrapperPlayServerEntityMetadata meta = new WrapperPlayServerEntityMetadata(entityId, new ArrayList<>(metadata.values()));
        sendPacket(meta);
    }
}
