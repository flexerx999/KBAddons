package xyz.destiall.addons.abilities;

import me.wazup.kitbattle.Kitbattle;
import me.wazup.kitbattle.PlayerData;
import me.wazup.kitbattle.abilities.Ability;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Rage extends Ability {
    private int cooldown;
    private int duration;
    private final Material activationMaterial = Material.REDSTONE;

    @Override
    public String getName() {
        return "Rage";
    }

    @Override
    public void load(FileConfiguration file) {
        if (!file.contains("Abilities.Rage.Cooldown")) {
            file.set("Abilities.Rage.Cooldown", 60);
        }
        cooldown = file.getInt("Abilities.Rage.Cooldown");

        if (!file.contains("Abilities.Rage.Duration")) {
            file.set("Abilities.Rage.Duration", 10);
        }
        duration = file.getInt("Abilities.Rage.Duration");
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
    public boolean execute(Player p, PlayerData data, Event event) {
        if (data.hasCooldown(p, "Rage")) {
            return false;
        }
        data.setCooldown(p, "Rage", cooldown, true);
        Kitbattle.getInstance().sendUseAbility(p, data);

        // Duration is in seconds in config, convert to ticks (20 ticks = 1 second)
        int ticks = duration * 20;
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, ticks, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, ticks, 1));

        return true;
    }
}