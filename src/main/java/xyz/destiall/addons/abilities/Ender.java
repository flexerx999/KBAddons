package xyz.destiall.addons.abilities;

import me.wazup.kitbattle.PlayerData;
import me.wazup.kitbattle.abilities.Ability;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class Ender extends Ability {
    private int cooldown;
    private double maxDistance;
    private final Material activationMaterial = Material.ENDER_EYE;

    @Override
    public String getName() {
        return "Ender";
    }

    @Override
    public void load(FileConfiguration file) {
        if (!file.contains("Abilities.Ender.Cooldown")) {
            file.set("Abilities.Ender.Cooldown", 10);
        }
        cooldown = file.getInt("Abilities.Ender.Cooldown");

        if (!file.contains("Abilities.Ender.Max-Distance")) {
            file.set("Abilities.Ender.Max-Distance", 10.0);
        }
        maxDistance = file.getDouble("Abilities.Ender.Max-Distance");
    }

    @Override
    public Material getActivationMaterial() {
        return activationMaterial;
    }

    @Override
    public EntityType getActivationProjectile() {
        return null;
    }

    @Override
    public boolean isAttackActivated() {
        return false;
    }

    @Override
    public boolean isAttackReceiveActivated() {
        return false;
    }

    @Override
    public boolean isDamageActivated() {
        return false;
    }

    @Override
    public boolean isEntityInteractionActivated() {
        return false;
    }

    @Override
    public boolean execute(Player player, PlayerData data, Event event) {
        if (data.hasCooldown(player, "Ender")) {
            return false;
        }

        data.setCooldown(player, "Ender", cooldown, true);

        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection();

        RayTraceResult result = player.getWorld().rayTraceBlocks(eyeLoc, direction, maxDistance);
        Location teleportLoc;

        if (result != null && result.getHitBlock() != null) {
            // Teleport slightly back from the block to avoid getting stuck
            teleportLoc = result.getHitPosition().toLocation(player.getWorld()).subtract(direction.multiply(0.5));
        } else {
            // Teleport to max distance if no block hit
            teleportLoc = eyeLoc.add(direction.multiply(maxDistance));
        }

        // Ensure the teleport location is safe (not inside blocks)
        if (!teleportLoc.getBlock().isPassable()) {
            teleportLoc.add(0, 1, 0); // Move up one block
        }

        player.teleport(teleportLoc);
        return true;
    }
}