package xyz.destiall.addons.abilities;

import me.wazup.kitbattle.PlayerData;
import me.wazup.kitbattle.abilities.Ability;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Locale;
import java.util.Objects;

public class Boost extends Ability {
    private int cooldown;
    private Material activationMaterial;
    private PotionEffect boostPotion;

    public String getName() {
        return "Boost";
    }

    public void load(FileConfiguration file) {
        if (!file.contains("Abilities.Boost.Cooldown")) {
            file.set("Abilities.Boost.Cooldown", 30);
        }
        cooldown = file.getInt("Abilities.Boost.Cooldown");
        if (!file.contains("Abilities.Boost.Material")) {
            file.set("Abilities.Boost.Material", "FEATHER");
        }
        String material = file.getString("Abilities.Boost.Material", "FEATHER");
        activationMaterial = Material.getMaterial(material);
        if (!file.contains("Abilities.Boost.Duration")) {
            file.set("Abilities.Boost.Duration", 100);
        }
        int duration = file.getInt("Abilities.Boost.Duration", 100);
        if (!file.contains("Abilities.Boost.Amplifier")) {
            file.set("Abilities.Boost.Amplifier", "SPEED");
        }
        int amplifier = file.getInt("Abilities.Boost.Amplifier", 2);
        String potionType = file.getString("Abilities.Boost.Potion", "SPEED");
        boostPotion = new PotionEffect(Objects.requireNonNull(
                Registry.POTION_EFFECT_TYPE.get(Objects.requireNonNull(
                        NamespacedKey.fromString(potionType.toLowerCase())))), duration, amplifier);
    }

    public Material getActivationMaterial() {
        return activationMaterial;
    }

    public boolean isAttackActivated() {
        return false;
    }

    public boolean isAttackReceiveActivated() {
        return false;
    }

    public boolean isDamageActivated() {
        return false;
    }

    public boolean isEntityInteractionActivated() {
        return false;
    }

    public EntityType getActivationProjectile() {
        return null;
    }

    @Override
    public boolean execute(Player p, PlayerData data, Event event) {
        if (data.hasCooldown(p, "Boost")) {
            return false;
        }
        data.setCooldown(p, "Boost", cooldown, true);
        p.addPotionEffect(boostPotion);
        return true;
    }
}