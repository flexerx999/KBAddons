package xyz.destiall.addons.abilities;

import me.wazup.kitbattle.PlayerData;
import me.wazup.kitbattle.abilities.Ability;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SizeChange extends Ability {
    private int cooldown;
    private double smallSize;
    private double normalSize;
    private double bigSize;
    private final Material activationMaterial = Material.SLIME_BALL;

    // Track player sizes: 0=small, 1=normal, 2=big
    private static final Map<UUID, Integer> playerSizes = new HashMap<>();

    @Override
    public String getName() {
        return "SizeChange";
    }

    @Override
    public void load(FileConfiguration file) {
        if (!file.contains("Abilities.SizeChange.Cooldown")) {
            file.set("Abilities.SizeChange.Cooldown", 3);
        }
        cooldown = file.getInt("Abilities.SizeChange.Cooldown");

        if (!file.contains("Abilities.SizeChange.Small-Size")) {
            file.set("Abilities.SizeChange.Small-Size", 0.75);
        }
        smallSize = file.getDouble("Abilities.SizeChange.Small-Size");

        if (!file.contains("Abilities.SizeChange.Normal-Size")) {
            file.set("Abilities.SizeChange.Normal-Size", 1.0);
        }
        normalSize = file.getDouble("Abilities.SizeChange.Normal-Size");

        if (!file.contains("Abilities.SizeChange.Big-Size")) {
            file.set("Abilities.SizeChange.Big-Size", 1.25);
        }
        bigSize = file.getDouble("Abilities.SizeChange.Big-Size");
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
        return false; // No longer handle damage events
    }

    @Override
    public boolean isEntityInteractionActivated() {
        return false;
    }

    @Override
    public boolean execute(Player player, PlayerData data, Event event) {
        if (data.hasCooldown(player, "SizeChange")) {
            return false;
        }

        data.setCooldown(player, "SizeChange", cooldown, true);

        // Cycle through sizes: small -> normal -> big -> small
        int currentSizeIndex = playerSizes.getOrDefault(player.getUniqueId(), 1); // Default to normal
        int nextSizeIndex = (currentSizeIndex + 1) % 3;

        playerSizes.put(player.getUniqueId(), nextSizeIndex);

        double newScale;
        double speedMultiplier;

        switch (nextSizeIndex) {
            case 0: // Small
                newScale = smallSize;
                speedMultiplier = 1.3; // Faster when small
                break;
            case 1: // Normal
                newScale = normalSize;
                speedMultiplier = 1.0; // Normal speed
                break;
            case 2: // Big
                newScale = bigSize;
                speedMultiplier = 0.7; // Slower when big
                break;
            default:
                newScale = normalSize;
                speedMultiplier = 1.0;
                break;
        }

        // Apply size changes
        applyPlayerSize(player, newScale, speedMultiplier);
        return true;
    }

    private void applyPlayerSize(Player player, double scale, double speedMultiplier) {
        // Set scale attribute (affects hitbox)
        if (player.getAttribute(Attribute.SCALE) != null) {
            player.getAttribute(Attribute.SCALE).setBaseValue(scale);
        }

        // Set movement speed
        if (player.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
            double baseSpeed = 0.1; // Default Minecraft walking speed
            player.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(baseSpeed * speedMultiplier);
        }
    }

    private void resetPlayerSize(Player player) {
        // Reset to normal size
        if (player.getAttribute(Attribute.SCALE) != null) {
            player.getAttribute(Attribute.SCALE).setBaseValue(normalSize);
        }

        if (player.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
            player.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.1); // Default speed
        }
    }

    // Public static method for external access
    public static void resetPlayerSizeStatic(Player player) {
        // Reset to normal size (use default values)
        if (player.getAttribute(Attribute.SCALE) != null) {
            player.getAttribute(Attribute.SCALE).setBaseValue(1.0);
        }

        if (player.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
            player.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.1);
        }

        // Remove from tracking
        playerSizes.remove(player.getUniqueId());
    }
}