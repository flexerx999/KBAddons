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
import xyz.destiall.addons.Addons;
import xyz.destiall.addons.managers.BlockManager;
import xyz.destiall.addons.utils.Pair;
import xyz.destiall.addons.utils.Scheduler;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FireTrail extends Ability {
    private int cooldown;
    private int duration; // Duration to track player movement
    private int fireBlockDuration; // How long fire blocks last
    private final Material activationMaterial = Material.DIAMOND_SWORD;
    private static final Set<UUID> activeTrails = new HashSet<>();

    @Override
    public String getName() {
        return "FireTrail";
    }

    @Override
    public void load(FileConfiguration file) {
        if (!file.contains("Abilities.FireTrail.Cooldown")) {
            file.set("Abilities.FireTrail.Cooldown", 15);
        }
        cooldown = file.getInt("Abilities.FireTrail.Cooldown");

        if (!file.contains("Abilities.FireTrail.Duration")) {
            file.set("Abilities.FireTrail.Duration", 5);
        }
        duration = file.getInt("Abilities.FireTrail.Duration");

        if (!file.contains("Abilities.FireTrail.Fire-Block-Duration")) {
            file.set("Abilities.FireTrail.Fire-Block-Duration", 4);
        }
        fireBlockDuration = file.getInt("Abilities.FireTrail.Fire-Block-Duration");
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
        if (data.hasCooldown(player, "FireTrail")) {
            return false;
        }

        if (activeTrails.contains(player.getUniqueId())) {
            return false; // Already has active trail
        }

        data.setCooldown(player, "FireTrail", cooldown, true);
        activeTrails.add(player.getUniqueId());

        new Scheduler.TaskRunnable() {
            private int ticks = 0;
            private final int maxTicks = duration * 20; // Convert seconds to ticks
            private Location lastLocation = player.getLocation().clone();

            @Override
            public void run() {
                if (!player.isOnline() || ticks >= maxTicks) {
                    activeTrails.remove(player.getUniqueId());
                    this.cancel();
                    return;
                }

                Location currentLoc = player.getLocation();

                // Check if player moved enough to place fire
                if (currentLoc.distance(lastLocation) > 1.0) {
                    Block block = currentLoc.subtract(0, 1, 0).getBlock(); // Block below player

                    if (block.getType().isSolid() && !block.getType().isFlammable()) {
                        Block fireBlock = block.getRelative(0, 1, 0); // Block above solid block

                        if (fireBlock.getType() == Material.AIR) {
                            BlockState originalState = fireBlock.getState();
                            fireBlock.setType(Material.FIRE);

                            // Add to block manager for automatic cleanup
                            BlockManager.EXPIRIES.put(
                                    new Pair<>(fireBlock, originalState),
                                    System.currentTimeMillis() + (fireBlockDuration * 1000L)
                            );
                        }
                    }

                    lastLocation = currentLoc.clone();
                }

                ticks++;
            }
        }.runTaskTimer(Addons.scheduler, 0L, 2L); // Run every 2 ticks

        return true;
    }
}