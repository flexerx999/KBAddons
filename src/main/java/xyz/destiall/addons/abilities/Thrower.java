package xyz.destiall.addons.abilities;

import me.wazup.kitbattle.PlayerData;
import me.wazup.kitbattle.abilities.Ability;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;

public class Thrower extends Ability {
    private int cooldown;
    private int tickDelay;
    private final Material activationMaterial = Material.TNT;

    public String getName() {
        return "Thrower";
    }

    public void load(FileConfiguration file) {
        if (!file.contains("Abilities.Thrower.Cooldown")) {
            file.set("Abilities.Thrower.Cooldown", 30);
        }
        cooldown = file.getInt("Abilities.Thrower.Cooldown");
        if (!file.contains("Abilities.Thrower.Explodes-In-Ticks")) {
            file.set("Abilities.Thrower.Explodes-In-Ticks", 100);
        }
        tickDelay = file.getInt("Abilities.Thrower.Explodes-In-Ticks");
    }

    public Material getActivationMaterial() {
        return activationMaterial;
    }

    public EntityType getActivationProjectile() {
        return null;
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

    public boolean execute(Player p, PlayerData data, Event event) {
        if (data.hasCooldown(p, "Thrower")) {
            return false;
        }
        data.setCooldown(p, "Thrower", cooldown, true);
        TNTPrimed tnt = (TNTPrimed)p.getWorld().spawnEntity(p.getLocation().clone(), EntityType.TNT);
        tnt.setVelocity(p.getEyeLocation().getDirection().clone().normalize());
        tnt.setFuseTicks(tickDelay);
        return true;
    }
}

