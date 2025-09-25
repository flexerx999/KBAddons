package xyz.destiall.addons.abilities;

import me.wazup.kitbattle.PlayerData;
import me.wazup.kitbattle.abilities.Ability;
import me.wazup.kitbattle.abilities.AbilityManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Randomiser extends Ability {
    private int cooldown;
    private final Material activationMaterial = Material.GOLDEN_SWORD;
    private static final Random random = new Random();

    // List of abilities to randomly pick from
    private static final List<String> RANDOM_ABILITIES = Arrays.asList(
            "Ender", "FireTrail", "IceTrail", "Leap", "TemporalSplit",
            "Boost", "Summoner", "Thor", "LaunchFirework", "Scorpion",
            "SoundBlast", "Thrower", "Timelord", "Burrower"
    );

    @Override
    public String getName() {
        return "Randomiser";
    }

    @Override
    public void load(FileConfiguration file) {
        if (!file.contains("Abilities.Randomiser.Cooldown")) {
            file.set("Abilities.Randomiser.Cooldown", 10);
        }
        cooldown = file.getInt("Abilities.Randomiser.Cooldown");
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
        if (data.hasCooldown(player, "Randomiser")) {
            return false;
        }

        data.setCooldown(player, "Randomiser", cooldown, true);

        // Pick a random ability
        String randomAbilityName = RANDOM_ABILITIES.get(random.nextInt(RANDOM_ABILITIES.size()));

        // Get the ability instance from AbilityManager
        Ability randomAbility = AbilityManager.getInstance().getAbility(randomAbilityName);

        if (randomAbility == null) {
            player.sendMessage(ChatColor.RED + "Randomiser failed to find ability: " + randomAbilityName);
            return false;
        }

        // Notify player which ability was selected
        player.sendMessage(ChatColor.LIGHT_PURPLE + "★" + ChatColor.WHITE + "Randomiser Ability: " + ChatColor.GOLD + randomAbilityName + ChatColor.LIGHT_PURPLE + "★");

        // Execute the random ability (force execute - ignore individual cooldown)
        try {
            // Temporarily store the original cooldown state
            boolean hadCooldown = data.hasCooldown(player, randomAbilityName);

            // Force execute the ability regardless of its cooldown
            boolean success = randomAbility.execute(player, data, event);

            if (success) {
                // If the ability was already on cooldown and we forced it,
                // we need to manage cooldowns properly
                if (hadCooldown) {
                    // The ability might have reset its own cooldown, so we don't interfere
                    player.sendMessage(ChatColor.GRAY + "(Forced execution - " + randomAbilityName + " was on cooldown)");
                }
            }

            return success;
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Randomiser failed to execute " + randomAbilityName + ": " + e.getMessage());
            return false;
        }
    }
}