package xyz.destiall.addons.valorant;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import xyz.destiall.addons.valorant.common.Waller;

import java.util.Arrays;
import java.util.List;

public class Neon extends Agent implements Waller {
    private Vector wallDirection = null;
    private final List<Material> wallMaterials = Arrays.asList(Material.BLUE_CONCRETE, Material.LIGHT_BLUE_CONCRETE, Material.LIGHT_BLUE_TERRACOTTA, Material.BLUE_TERRACOTTA, Material.BLUE_GLAZED_TERRACOTTA);

    public Neon(Player player) {
        super(player);
    }

    @Override
    public void wall(Player source, Location origin) {
        wallDirection = source.getLocation().getDirection().setY(0);
        Vector forward = wallDirection.clone().normalize();
        Vector up = new Vector(0.0, 1.0, 0.0);
        Vector right = forward.crossProduct(up).multiply(2);
        wallUp(source, origin.clone().add(right));
        wallUp(source, origin.clone().subtract(right));
    }

    @Override
    public List<Material> wallMaterials() {
        return wallMaterials;
    }

    @Override
    public int wallHeight() {
        return 4;
    }

    @Override
    public int wallLength() {
        return 15;
    }

    @Override
    public int wallSpeed() {
        return 1;
    }

    @Override
    public double wallDuration() {
        return 5;
    }

    @Override
    public Vector wallDirection(Player source) {
        return wallDirection;
    }
}
