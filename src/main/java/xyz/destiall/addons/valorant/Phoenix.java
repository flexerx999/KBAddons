package xyz.destiall.addons.valorant;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import xyz.destiall.addons.Addons;
import xyz.destiall.addons.valorant.common.Flasher;
import xyz.destiall.addons.valorant.common.Waller;

import java.util.Arrays;
import java.util.List;

public class Phoenix extends Agent implements Flasher, Waller {
    private Vector prevWallDirection = null;
    private final NamespacedKey key = new NamespacedKey(Addons.INSTANCE, "flash");

    public Phoenix(Player player) {
        super(player);
    }

    @Override
    public void flash(Player self, Location source) {
        Snowball snowball = self.launchProjectile(Snowball.class);
        snowball.setVelocity(self.getLocation().getDirection());
        snowball.setShooter(self);
        snowball.getPersistentDataContainer().set(key, PersistentDataType.STRING, "phoenix");
    }

    @EventHandler
    public void onLand(ProjectileHitEvent e) {
        Projectile proj = e.getEntity();
        ProjectileSource shooter = proj.getShooter();
        if (!(shooter instanceof Player) || !((Player) shooter).getUniqueId().equals(player.getUniqueId()))
            return;

        if (proj instanceof Snowball) {
            if (proj.getPersistentDataContainer().has(key) && proj.getPersistentDataContainer().get(key, PersistentDataType.STRING).equalsIgnoreCase("phoenix")) {
                flashOut((Player) shooter, proj.getLocation());
                Addons.INSTANCE.getAgentManager().unsetAgent(player);
            }
        }
    }

    @Override
    public boolean selfFlash() {
        return true;
    }

    @Override
    public void wall(Player source, Location origin) {
        prevWallDirection = wallDirection(source);
        wallUp(source, origin.clone());
    }

    @Override
    public List<Material> wallMaterials() {
        return Arrays.asList(Material.RED_CONCRETE, Material.ORANGE_CONCRETE, Material.RED_TERRACOTTA, Material.ORANGE_TERRACOTTA, Material.ORANGE_GLAZED_TERRACOTTA);
    }

    @Override
    public int wallHeight() {
        return 4;
    }

    @Override
    public int wallLength() {
        return 8;
    }

    @Override
    public int wallSpeed() {
        return 1;
    }

    @Override
    public double wallDuration() {
        return 7;
    }

    // a + (b - a) * t
    @Override
    public Vector wallDirection(Player source) {
        Vector now = source.getLocation().getDirection().setY(0).normalize();
        if (prevWallDirection == null)
            return now;

        double degree = 0.5d;
        double x = prevWallDirection.getX() + (now.getX() - prevWallDirection.getX()) * degree;
        double z = prevWallDirection.getZ() + (now.getZ() - prevWallDirection.getZ()) * degree;

        while (new Vector(x, 0d, z).dot(prevWallDirection) < 0.5f) {
            degree -= 0.01d;
            x = prevWallDirection.getX() + (now.getX() - prevWallDirection.getX()) * degree;
            z = prevWallDirection.getZ() + (now.getZ() - prevWallDirection.getZ()) * degree;
        }
        return new Vector(x, 0d, z);
    }
}
