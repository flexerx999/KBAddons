package xyz.destiall.addons.abilities;

import me.wazup.kitbattle.PlayerData;
import me.wazup.kitbattle.abilities.Ability;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.stream.Collectors;

public class Scorpion extends Ability {
    private int cooldown;
    private double radius;
    private PotionEffect effect;
    @Override
    public String getName() {
        return "Scorpion";
    }

    @Override
    public void load(FileConfiguration file) {
        if (!file.contains("Abilities.Scorpion.Cooldown")) {
            file.set("Abilities.Scorpion.Cooldown", 5);
        }
        cooldown = file.getInt("Abilities.Scorpion.Cooldown", 5);
        if (!file.contains("Abilities.Scorpion.Duration")) {
            file.set("Abilities.Scorpion.Duration", 3);
        }
        int duration = file.getInt("Abilities.Scorpion.Duration", 3);
        if (!file.contains("Abilities.Scorpion.Strength")) {
            file.set("Abilities.Scorpion.Strength", 1);
        }
        int amplifier = file.getInt("Abilities.Scorpion.Strength", 1);
        if (!file.contains("Abilities.Scorpion.Radius")) {
            file.set("Abilities.Scorpion.Radius", 4);
        }
        radius = file.getDouble("Abilities.Scorpion.Radius", 4);
        effect = new PotionEffect(PotionEffectType.POISON, duration * 20, amplifier);
    }

    @Override
    public Material getActivationMaterial() {
        return Material.SPIDER_EYE;
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
        if (data.hasCooldown(player, "Scorpion")) return false;
        data.setCooldown(player, "Scorpion", cooldown, true);
        Collection<Entity> nearby = player.getNearbyEntities(radius, radius, radius)
                .stream().filter(e -> e instanceof LivingEntity).collect(Collectors.toSet());
        for (Entity entity : nearby) {
            LivingEntity le = (LivingEntity) entity;
            le.addPotionEffect(effect);
        }
        return true;
    }
}
