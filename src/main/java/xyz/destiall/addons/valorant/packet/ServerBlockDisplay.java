package xyz.destiall.addons.valorant.packet;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Transformation;

public class ServerBlockDisplay implements WallPacket {
    private final BlockDisplay block;

    public ServerBlockDisplay(Location location) {
        block = (BlockDisplay) location.getWorld().spawnEntity(location, EntityType.BLOCK_DISPLAY);
        block.setPersistent(false);
    }

    public void setBlock(Material material) {
        block.setBlock(material.createBlockData());
    }

    public void setScale(double scale) {
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
}
