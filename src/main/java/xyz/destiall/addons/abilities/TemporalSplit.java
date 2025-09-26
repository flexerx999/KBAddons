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
    private final Material activationMaterial = Material.IRON_SWORD;

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

            if (damageEvent.getEntity() instanceof Player) {
                Player damagedPlayer = (Player) damageEvent.getEntity();
                UUID playerUUID = damagedPlayer.getUniqueId();

                // If player takes damage, damage their split too
                if (activeSplits.containsKey(playerUUID)) {
                    Skeleton split = activeSplits.get(playerUUID);
                    if (split != null && !split.isDead()) {
                        split.damage(damageEvent.getDamage());
                    }
                }
            } else if (damageEvent.getEntity() instanceof Skeleton) {
                Skeleton damagedSkeleton = (Skeleton) damageEvent.getEntity();
                UUID skeletonUUID = damagedSkeleton.getUniqueId();

                // If split takes damage, damage the owner
                UUID ownerUUID = splitOwners.get(skeletonUUID);
                if (ownerUUID != null) {
                    Player owner = Addons.INSTANCE.getServer().getPlayer(ownerUUID);
                    if (owner != null && owner.isOnline()) {
                        owner.damage(damageEvent.getDamage());
                    }
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

        // Make skeleton not attack the owner by clearing default hostile AI
        split.setTarget(null);
        split.getPathfinder().stopPathfinding();

        // Set custom name (same as player, no "Undead" prefix)
        split.setCustomName(player.getName());
        split.setCustomNameVisible(true);

        // Prevent burning in sunlight
        split.setFireTicks(0);
        split.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, duration * 20, 1));

        // Make skeleton not despawn
        split.setRemoveWhenFarAway(false);

        // Try to make it not naturally hostile to players
        try {
            split.setAware(false); // Disable AI awareness temporarily
        } catch (Exception ignored) {}

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

        // Task to manage targeting with better protection against attacking owner
        new Scheduler.TaskRunnable() {
            private int tickCount = 0;

            @Override
            public void run() {
                if (split.isDead() || !activeSplits.containsKey(player.getUniqueId()) || !player.isOnline()) {
                    removeSplit(player.getUniqueId());
                    this.cancel();
                    return;
                }

                // Re-enable awareness after 3 seconds
                if (tickCount == 60) {
                    try {
                        split.setAware(true);
                    } catch (Exception ignored) {}
                }

                // Manage targeting after awareness is enabled
                if (tickCount >= 60) {
                    LivingEntity currentTarget = split.getTarget();

                    // Force clear target if it's the owner
                    if (currentTarget != null && currentTarget.getUniqueId().equals(player.getUniqueId())) {
                        split.setTarget(null);
                        split.getPathfinder().stopPathfinding();
                        currentTarget = null;
                    }

                    // Find new target every 20 ticks (1 second) if no valid target
                    if (tickCount % 20 == 0) {
                        if (currentTarget == null || currentTarget.isDead() ||
                                currentTarget.getLocation().distance(split.getLocation()) > 10 ||
                                currentTarget.getUniqueId().equals(player.getUniqueId())) {

                            LivingEntity newTarget = findNearestEnemy(player, split);
                            if (newTarget != null && !newTarget.getUniqueId().equals(player.getUniqueId())) {
                                split.setTarget(newTarget);
                            }
                        }
                    }
                }

                tickCount++;
            }
        }.runTaskTimer(Addons.scheduler, 1L, 1L);

        return true;
    }

    private LivingEntity findNearestEnemy(Player owner, Skeleton split) {
        LivingEntity nearestEnemy = null;
        double nearestDistance = 10.0; // 10 block radius

        for (LivingEntity entity : split.getWorld().getLivingEntities()) {
            if (entity instanceof Player) {
                Player target = (Player) entity;
                // Never target the owner (double check with UUID)
                if (!target.getUniqueId().equals(owner.getUniqueId())) {
                    double distance = entity.getLocation().distance(split.getLocation());
                    if (distance <= 10.0 && distance < nearestDistance) {
                        nearestEnemy = entity;
                        nearestDistance = distance;
                    }
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

    // Clean up splits when players leave or die
    public static void cleanupPlayer(UUID playerUUID) {
        removeSplit(playerUUID);

        // Also clean up any orphaned split ownership entries
        splitOwners.entrySet().removeIf(entry -> entry.getValue().equals(playerUUID));
    }
}