package xyz.destiall.addons.abilities;

import me.wazup.kitbattle.PlayerData;
import me.wazup.kitbattle.abilities.Ability;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Soldier extends Ability {
    private int cooldown;
    private final Material activationMaterial = Material.ENDER_CHEST;

    public String getName() {
        return "Soldier";
    }

    public void load(FileConfiguration file) {
        if (!file.contains("Abilities.Soldier.Cooldown")) {
            file.set("Abilities.Soldier.Cooldown", 30);
        }
        cooldown = file.getInt("Abilities.Soldier.Cooldown");
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
        if (data.hasCooldown(p, "Soldier")) {
            return false;
        }
        data.setCooldown(p, "Soldier", this.cooldown, true);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 3));
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 100, 0));
        return true;
    }
}
