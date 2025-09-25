package xyz.destiall.addons.abilities;

import me.wazup.kitbattle.PlayerData;
import me.wazup.kitbattle.abilities.Ability;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import xyz.destiall.addons.Addons;
import xyz.destiall.addons.utils.Scheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TemporalSplit extends Ability {
    private int cooldown;
    private int duration;
    private final Material activationMaterial = Material.ECHO_SHARD;

    // Track active splits: Player UUID -> Skeleton Entity
    private static final Map<UUID, Skeleton> activeSplits = new HashMap<>();
    // Track split ownership: Skeleton UUID -> Player UUID
    private static final Map<UUID, UUID> splitOwners = new HashMap<>();

    @Override
    public String getName() {
        return "TemporalSplit";
    }

    @Override
    public void load(FileConfiguration file) {
        if (!file.contains("Abilities.TemporalSplit.Cooldown")) {
            file.set("Abilities.TemporalSplit.Cooldown", 30);
        }
        cooldown = file.getInt("Abilities.TemporalSplit.Cooldown");

        if (!file.contains("Abilities.TemporalSplit.Duration")) {
            file.set("Abilities.TemporalSplit.Duration", 15);
        }
        duration = file.getInt("Abilities.TemporalSplit.Duration");
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
        return true; // To handle shared damage
    }

    @Override
    public boolean isEntityInteractionActivated() {
        return false;
    }

    @Override
    public boolean execute(Player player, PlayerData data, Event event) {
        // Handle shared damage
        if (event instanceof EntityDamageEvent) {
            EntityDamageEvent damageEvent = (EntityDamageEvent) event;
            UUID playerUUID = player.getUniqueId();

            // If player takes damage, damage their split too
            if (activeSplits.containsKey(playerUUID)) {
                Skeleton split = activeSplits.get(playerUUID);
                if (split != null && !split.isDead()) {
                    split.damage(damageEvent.getDamage());
                }
            }

            // If split takes damage, damage the owner
            UUID ownerUUID = splitOwners.get(player.getUniqueId());
            if (ownerUUID != null) {
                Player owner = Addons.INSTANCE.getServer().getPlayer(ownerUUID);
                if (owner != null && owner.isOnline()) {
                    owner.damage(damageEvent.getDamage());
                }
            }

            return false;
        }

        // Right-click activation
        if (data.hasCooldown(player, "TemporalSplit")) {
            return false;
        }

        // Check if player already has an active split
        if (activeSplits.containsKey(player.getUniqueId())) {
            return false;
        }

        data.setCooldown(player, "TemporalSplit", cooldown, true);

        // Spawn skeleton split
        Skeleton split = (Skeleton) player.getWorld().spawnEntity(player.getLocation(), EntityType.SKELETON);

        // Set custom name
        split.setCustomName("Undead " + player.getName());
        split.setCustomNameVisible(true);

        // Prevent burning in sunlight
        split.setFireTicks(0);
        split.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, duration * 20, 1));

        // Make skeleton not attack the owner
        split.setRemoveWhenFarAway(false);

        // Copy player's equipment
        split.getEquipment().setArmorContents(player.getInventory().getArmorContents().clone());
        split.getEquipment().setItemInMainHand(player.getInventory().getItemInMainHand().clone());
        split.getEquipment().setItemInOffHand(player.getInventory().getItemInOffHand().clone());

        // Set equipment drop chances to 0 (so items don't drop on death)
        split.getEquipment().setHelmetDropChance(0.0f);
        split.getEquipment().setChestplateDropChance(0.0f);
        split.getEquipment().setLeggingsDropChance(0.0f);
        split.getEquipment().setBootsDropChance(0.0f);
        split.getEquipment().setItemInMainHandDropChance(0.0f);
        split.getEquipment().setItemInOffHandDropChance(0.0f);

        // Copy health if possible
        try {
            split.setMaxHealth(player.getMaxHealth());
            split.setHealth(player.getHealth());
        } catch (Exception e) {
            // Use default skeleton health if copying fails
        }

        // Give speed effect
        split.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration * 20, 1));

        // Set aggressive targeting (attack players except owner within 10 blocks)
        // Don't set initial target to prevent attacking owner

        // Store the split
        activeSplits.put(player.getUniqueId(), split);
        splitOwners.put(split.getUniqueId(), player.getUniqueId());

        // Schedule removal after duration
        new Scheduler.TaskRunnable() {
            @Override
            public void run() {
                removeSplit(player.getUniqueId());
            }
        }.runTaskLater(Addons.scheduler, duration * 20L);

        // Task to continuously find targets (start after a short delay)
        new Scheduler.TaskRunnable() {
            @Override
            public void run() {
                if (split.isDead() || !activeSplits.containsKey(player.getUniqueId())) {
                    this.cancel();
                    return;
                }

                // Find new target if current target is invalid or is the owner
                LivingEntity currentTarget = split.getTarget();
                if (currentTarget == null || currentTarget.isDead() ||
                        currentTarget.getUniqueId().equals(player.getUniqueId()) ||
                        currentTarget.getLocation().distance(split.getLocation()) > 10) {
                    split.setTarget(findNearestEnemy(player, split));
                }
            }
        }.runTaskTimer(Addons.scheduler, 40L, 20L); // Start after 2 seconds, check every second

        return true;
    }

    private LivingEntity findNearestEnemy(Player owner, Skeleton split) {
        LivingEntity nearestEnemy = null;
        double nearestDistance = 10.0; // 10 block radius

        for (LivingEntity entity : split.getWorld().getLivingEntities()) {
            if (entity instanceof Player && !entity.getUniqueId().equals(owner.getUniqueId())) {
                double distance = entity.getLocation().distance(split.getLocation());
                if (distance <= 10.0 && distance < nearestDistance) {
                    nearestEnemy = entity;
                    nearestDistance = distance;
                }
            }
        }

        return nearestEnemy;
    }

    public static void removeSplit(UUID playerUUID) {
        Skeleton split = activeSplits.remove(playerUUID);
        if (split != null && !split.isDead()) {
            splitOwners.remove(split.getUniqueId());
            split.remove();
        }
    }

    // Clean up splits when players leave
    public static void cleanupPlayer(UUID playerUUID) {
        removeSplit(playerUUID);

        // Also remove if this player was a split owner
        splitOwners.entrySet().removeIf(entry -> entry.getValue().equals(playerUUID));
    }
}