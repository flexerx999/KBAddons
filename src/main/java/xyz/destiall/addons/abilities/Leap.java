package xyz.destiall.addons.abilities;

import me.wazup.kitbattle.PlayerData;
import me.wazup.kitbattle.abilities.Ability;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

public class Leap extends Ability {
    private int cooldown;
    private double leapStrength;
    private final Material activationMaterial = Material.RABBIT_FOOT;

    @Override
    public String getName() {
        return "Leap";
    }

    @Override
    public void load(FileConfiguration file) {
        if (!file.contains("Abilities.Leap.Cooldown")) {
            file.set("Abilities.Leap.Cooldown", 8);
        }
        cooldown = file.getInt("Abilities.Leap.Cooldown");

        if (!file.contains("Abilities.Leap.Strength")) {
            file.set("Abilities.Leap.Strength", 1.5);
        }
        leapStrength = file.getDouble("Abilities.Leap.Strength");
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
        return true; // For fall damage prevention
    }

    @Override
    public boolean isEntityInteractionActivated() {
        return false;
    }

    @Override
    public boolean execute(Player player, PlayerData data, Event event) {
        if (event instanceof EntityDamageEvent) {
            EntityDamageEvent damageEvent = (EntityDamageEvent) event;
            // Cancel fall damage for players with leap ability
            if (damageEvent.getCause() == EntityDamageEvent.DamageCause.FALL) {
                damageEvent.setCancelled(true);
                return true;
            }
            return false;
        }

        // Right-click activation
        if (data.hasCooldown(player, "Leap")) {
            return false;
        }

        data.setCooldown(player, "Leap", cooldown, true);

        Vector direction = player.getLocation().getDirection();
        direction.setY(0.5); // Add upward component
        direction.normalize().multiply(leapStrength);

        player.setVelocity(direction);
        return true;
    }
}