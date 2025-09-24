package xyz.destiall.addons.abilities;

import me.wazup.kitbattle.PlayerData;
import me.wazup.kitbattle.abilities.Ability;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.stream.Collectors;

public class SoundBlast extends Ability {
    private int cooldown;
    private double radius;
    private double knockbackStrength;
    private final Material activationMaterial = Material.NOTE_BLOCK;

    @Override
    public String getName() {
        return "SoundBlast";
    }

    @Override
    public void load(FileConfiguration file) {
        if (!file.contains("Abilities.SoundBlast.Cooldown")) {
            file.set("Abilities.SoundBlast.Cooldown", 8);
        }
        cooldown = file.getInt("Abilities.SoundBlast.Cooldown");

        if (!file.contains("Abilities.SoundBlast.Radius")) {
            file.set("Abilities.SoundBlast.Radius", 5.0);
        }
        radius = file.getDouble("Abilities.SoundBlast.Radius");

        if (!file.contains("Abilities.SoundBlast.Knockback-Strength")) {
            file.set("Abilities.SoundBlast.Knockback-Strength", 1.5);
        }
        knockbackStrength = file.getDouble("Abilities.SoundBlast.Knockback-Strength");
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
        if (data.hasCooldown(player, "SoundBlast")) {
            return false;
        }

        data.setCooldown(player, "SoundBlast", cooldown, true);

        // Play loud sound at player's location
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 2.0f, 1.0f);

        // Get nearby entities (excluding the player)
        List<LivingEntity> nearbyEntities = player.getNearbyEntities(radius, radius, radius)
                .stream()
                .filter(entity -> entity instanceof LivingEntity)
                .map(entity -> (LivingEntity) entity)
                .collect(Collectors.toList());

        // Apply knockback to each entity
        for (LivingEntity entity : nearbyEntities) {
            Vector direction = entity.getLocation().toVector().subtract(player.getLocation().toVector());
            direction.setY(0.3); // Add slight upward component
            direction.normalize().multiply(knockbackStrength);

            entity.setVelocity(direction);
        }

        return true;
    }
}