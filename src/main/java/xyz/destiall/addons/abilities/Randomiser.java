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
            "Boost", "Gun", "Freeze", "LaunchFirework", "Scorpion",
            "SoundBlast", "Thrower"
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
        player.sendMessage(ChatColor.GREEN + "Randomiser selected: " + ChatColor.YELLOW + randomAbilityName + "!");

        // Execute the random ability (ignore its cooldown since Randomiser has its own)
        try {
            boolean success = randomAbility.execute(player, data, event);

            // Note: We don't remove the individual ability's cooldown since
            // removeCooldown() method may not exist in all KitBattle versions
            // The Randomiser's own cooldown serves as the limiting factor

            return success;
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Randomiser failed to execute " + randomAbilityName + ": " + e.getMessage());
            return false;
        }
    }
}