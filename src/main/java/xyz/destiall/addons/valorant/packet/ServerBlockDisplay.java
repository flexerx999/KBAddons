package xyz.destiall.addons.valorant.packet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Vector3f;

public class ServerBlockDisplay implements BlockPacket {
    private final BlockDisplay block;

    public ServerBlockDisplay(Location location) {
        block = (BlockDisplay) location.getWorld().spawnEntity(location, EntityType.BLOCK_DISPLAY);
        block.setPersistent(false);
    }

    public void setBlock(Material material) {
        block.setBlock(material.createBlockData());
    }

    public void rotate(double degrees, Vector axis) {
        Transformation transformation = block.getTransformation();
        transformation.getLeftRotation()
                .rotateAxis((float) Math.toRadians(degrees), new Vector3f((float) axis.getX(), (float) axis.getY(), (float) axis.getZ()));
        block.setTransformation(transformation);
    }

    public void translate(Vector transform) {
        Transformation transformation = block.getTransformation();
        transformation.getTranslation().set(transform.getX(), transform.getY(), transform.getZ());
        block.setTransformation(transformation);
    }

    public void scale(double scale) {
        Transformation transformation = block.getTransformation();
        transformation.getScale().mul((float) scale, (float) scale, (float) scale);
        block.setTransformation(transformation);
    }

    public void setGravity(boolean gravity) {
        block.setGravity(gravity);
    }

    public void teleport(Location location) {
        block.teleport(location);
    }

    public void remove() {
        block.remove();
    }

    @Override
    public Location location() {
        return block.getLocation();
    }
}
