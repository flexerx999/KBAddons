package xyz.destiall.addons.abilities;

import me.wazup.kitbattle.PlayerData;
import me.wazup.kitbattle.abilities.Ability;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import xyz.destiall.addons.Addons;
import xyz.destiall.addons.utils.Scheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TemporalSplit extends Ability {
    private int cooldown;
    private int duration;
    private final Material activationMaterial = Material.ECHO_SHARD;

    // Track active splits
    private static final Map<UUID, NPC> activeSplits = new HashMap<>();
    private static final Map<UUID, UUID> splitOwners = new HashMap<>(); // NPC UUID -> Player UUID

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
                NPC split = activeSplits.get(playerUUID);
                if (split != null && split.getEntity() != null && split.getEntity() instanceof LivingEntity) {
                    LivingEntity splitEntity = (LivingEntity) split.getEntity();
                    splitEntity.damage(damageEvent.getDamage());
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

        // Check if Citizens plugin is available
        if (!Addons.INSTANCE.getServer().getPluginManager().isPluginEnabled("Citizens")) {
            player.sendMessage("§cCitizens plugin not found! Temporal Split requires Citizens.");
            return false;
        }

        data.setCooldown(player, "TemporalSplit", cooldown, true);

        // Create NPC split
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        NPC split = registry.createNPC(EntityType.PLAYER, player.getName());
        split.spawn(player.getLocation());

        // Copy player's appearance and setup
        if (split.getEntity() instanceof Player) {
            Player splitPlayer = (Player) split.getEntity();

            // Copy player skin
            splitPlayer.setPlayerProfile(player.getPlayerProfile());

            // Copy inventory
            splitPlayer.getInventory().setContents(player.getInventory().getContents().clone());
            splitPlayer.getInventory().setArmorContents(player.getInventory().getArmorContents().clone());

            // Copy health
            splitPlayer.setHealth(player.getHealth());
            splitPlayer.setFoodLevel(player.getFoodLevel());
        }

        // Set up AI behavior - simplified approach for combat
        split.getDefaultGoalController().clear();

        // Enable combat traits
        if (split.hasTrait(net.citizensnpcs.trait.ArmorStandTrait.class)) {
            split.getOrAddTrait(net.citizensnpcs.trait.ArmorStandTrait.class);
        }

        // Try to add combat behavior using Citizens API
        try {
            // This will make the NPC aggressive toward other players
            split.data().setPersistent("aggressive", true);
            split.data().setPersistent("attack-range", 10.0);
        } catch (Exception e) {
            // Fallback if traits don't work
            Addons.INSTANCE.getLogger().warning("Could not set NPC combat traits: " + e.getMessage());
        }

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

        return true;
    }

    public static void removeSplit(UUID playerUUID) {
        NPC split = activeSplits.remove(playerUUID);
        if (split != null) {
            splitOwners.remove(split.getUniqueId());
            if (split.isSpawned()) {
                split.despawn();
            }
            split.destroy();
        }
    }

    // Clean up splits when players leave
    public static void cleanupPlayer(UUID playerUUID) {
        removeSplit(playerUUID);

        // Also remove if this player was a split owner
        splitOwners.entrySet().removeIf(entry -> entry.getValue().equals(playerUUID));
    }
}