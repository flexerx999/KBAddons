package xyz.destiall.addons.abilities;

import me.wazup.kitbattle.PlayerData;
import me.wazup.kitbattle.abilities.Ability;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import xyz.destiall.addons.Addons;
import xyz.destiall.addons.managers.BlockManager;
import xyz.destiall.addons.utils.Pair;
import xyz.destiall.addons.utils.Scheduler;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class IceTrail extends Ability {
    private int cooldown;
    private int duration; // Duration to track player movement
    private int iceBlockDuration; // How long ice blocks last
    private final Material activationMaterial = Material.IRON_SWORD;
    private static final Set<UUID> activeTrails = new HashSet<>();

    // Ice cycle pattern: Ice, Ice, Blue, Packed, Blue
    private static final Material[] ICE_PATTERN = {Material.ICE, Material.ICE, Material.BLUE_ICE, Material.PACKED_ICE, Material.BLUE_ICE};

    @Override
    public String getName() {
        return "IceTrail";
    }

    @Override
    public void load(FileConfiguration file) {
        if (!file.contains("Abilities.IceTrail.Cooldown")) {
            file.set("Abilities.IceTrail.Cooldown", 15);
        }
        cooldown = file.getInt("Abilities.IceTrail.Cooldown");

        if (!file.contains("Abilities.IceTrail.Duration")) {
            file.set("Abilities.IceTrail.Duration", 5);
        }
        duration = file.getInt("Abilities.IceTrail.Duration");

        if (!file.contains("Abilities.IceTrail.Ice-Block-Duration")) {
            file.set("Abilities.IceTrail.Ice-Block-Duration", 5);
        }
        iceBlockDuration = file.getInt("Abilities.IceTrail.Ice-Block-Duration");
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
        // Only activate on right-click
        if (!(event instanceof PlayerInteractEvent)) {
            return false;
        }

        PlayerInteractEvent interactEvent = (PlayerInteractEvent) event;
        if (interactEvent.getAction() != Action.RIGHT_CLICK_AIR &&
                interactEvent.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return false;
        }

        if (data.hasCooldown(player, "IceTrail")) {
            return false;
        }

        if (activeTrails.contains(player.getUniqueId())) {
            return false; // Already has active trail
        }

        data.setCooldown(player, "IceTrail", cooldown, true);
        activeTrails.add(player.getUniqueId());

        new Scheduler.TaskRunnable() {
            private int ticks = 0;
            private final int maxTicks = duration * 20; // Convert seconds to ticks
            private Location lastLocation = player.getLocation().clone();
            private int icePatternIndex = 0;

            @Override
            public void run() {
                if (!player.isOnline() || ticks >= maxTicks) {
                    activeTrails.remove(player.getUniqueId());
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                // Check if player moved enough to place ice
                if (currentLoc.distance(lastLocation) > 1.0) {
                    Block block = currentLoc.subtract(0, 1, 0).getBlock(); // Block below player

                    // Only replace blocks that aren't already ice types and are solid
                    if (block.getType() != Material.AIR &&
                            block.getType().isSolid() &&
                            block.getType() != Material.ICE &&
                            block.getType() != Material.PACKED_ICE &&
                            block.getType() != Material.BLUE_ICE) {

                        BlockState originalState = block.getState();

                        // Get ice type from pattern
                        Material iceType = ICE_PATTERN[icePatternIndex % ICE_PATTERN.length];
                        block.setType(iceType);
                        icePatternIndex++;

                        // Add to block manager for automatic cleanup (reverts back)
                        BlockManager.EXPIRIES.put(
                                new Pair<>(block, originalState),
                                System.currentTimeMillis() + (iceBlockDuration * 1000L)
                        );
                    }

                    lastLocation = currentLoc.clone();
                }

                ticks++;
            }
        }.runTaskTimer(Addons.scheduler, 0L, 2L); // Run every 2 ticks

        return true;
    }
}