package xyz.destiall.addons.abilities;

import me.wazup.kitbattle.PlayerData;
import me.wazup.kitbattle.abilities.Ability;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.RayTraceResult;

public class LaunchFirework extends Ability {
    private int cooldown;
    private double maxRange;
    private final Material activationMaterial = Material.FIREWORK_ROCKET;

    @Override
    public String getName() {
        return "LaunchFirework";
    }

    @Override
    public void load(FileConfiguration file) {
        if (!file.contains("Abilities.LaunchFirework.Cooldown")) {
            file.set("Abilities.LaunchFirework.Cooldown", 5);
        }
        cooldown = file.getInt("Abilities.LaunchFirework.Cooldown");

        if (!file.contains("Abilities.LaunchFirework.Max-Range")) {
            file.set("Abilities.LaunchFirework.Max-Range", 30.0);
        }
        maxRange = file.getDouble("Abilities.LaunchFirework.Max-Range");
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
        if (!(event instanceof PlayerInteractEvent)) {
            return false;
        }

        PlayerInteractEvent interactEvent = (PlayerInteractEvent) event;

        // Only activate on right-click
        if (interactEvent.getAction() != Action.RIGHT_CLICK_BLOCK && interactEvent.getAction() != Action.RIGHT_CLICK_AIR) {
            return false;
        }

        if (data.hasCooldown(player, "LaunchFirework")) {
            return false;
        }

        // Find the block the player is looking at
        RayTraceResult result = player.rayTraceBlocks(maxRange);
        Location fireworkLocation;

        if (result != null && result.getHitBlock() != null) {
            Block hitBlock = result.getHitBlock();
            fireworkLocation = hitBlock.getLocation().add(0.5, 1, 0.5); // Center of block, 1 block above
        } else {
            return false; // No block targeted
        }

        data.setCooldown(player, "LaunchFirework", cooldown, true);

        // Spawn firework
        Firework firework = (Firework) fireworkLocation.getWorld().spawnEntity(fireworkLocation, EntityType.FIREWORK_ROCKET);
        FireworkMeta meta = firework.getFireworkMeta();

        // Create orange creeper effect with sparkles
        FireworkEffect effect = FireworkEffect.builder()
                .withColor(Color.ORANGE)
                .withFade(Color.YELLOW)
                .with(FireworkEffect.Type.CREEPER)
                .withTrail()
                .withFlicker()
                .build();

        meta.addEffect(effect);
        meta.setPower(2); // Flight duration
        firework.setFireworkMeta(meta);

        return true;
    }
}