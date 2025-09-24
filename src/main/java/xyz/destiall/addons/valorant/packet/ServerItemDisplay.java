package xyz.destiall.addons.valorant.packet;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Vector3f;

// Does not use packets, uses SpigotAPI, stored server-sided
public class ServerItemDisplay implements ItemPacket {
    private final ItemDisplay item;

    public ServerItemDisplay(Location location) {
        item = (ItemDisplay) location.getWorld().spawnEntity(location, EntityType.ITEM_DISPLAY);
        item.setInterpolationDelay(1);
        item.setInterpolationDuration(1);
        item.setTeleportDuration(1);
        item.setPersistent(false);
    }

    public void setItem(ItemStack item) {
        this.item.setItemStack(item);
    }


    @Override
    public void rotate(double degrees, Vector axis) {
        Transformation transformation = item.getTransformation();
        transformation.getLeftRotation()
                .rotateAxis((float) Math.toRadians(degrees), new Vector3f((float) axis.getX(), (float) axis.getY(), (float) axis.getZ()));
        item.setTransformation(transformation);
    }

    @Override
    public void translate(float x, float y, float z) {
        Transformation transformation = item.getTransformation();
        transformation.getTranslation().set(x, y, z);
        item.setTransformation(transformation);
    }

    @Override
    public void scale(double scale) {
        Transformation transformation = item.getTransformation();
        transformation.getScale().mul((float) scale, (float) scale, (float) scale);
        item.setTransformation(transformation);
    }

    public void setGravity(boolean gravity) {
        item.setGravity(gravity);
    }

    public void teleport(Location location) {
        item.teleportAsync(location);
    }

    public void remove() {
        item.remove();
    }

    @Override
    public Location location() {
        return item.getLocation();
    }
}
