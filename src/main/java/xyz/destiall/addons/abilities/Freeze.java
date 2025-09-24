package xyz.destiall.addons.abilities;

import me.wazup.kitbattle.Kitbattle;
import me.wazup.kitbattle.PlayerData;
import me.wazup.kitbattle.abilities.Ability;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Freeze extends Ability {
    private int cooldown;
    private int duration;
    private int amplifier;
    private final Material activationMaterial = Material.ICE;
    private final EntityType activationProjectile = EntityType.SNOWBALL;

    public String getName() {
        return "Freeze";
    }

    public void load(FileConfiguration file) {
        if (!file.contains("Abilities.Freeze.Cooldown")) {
            file.set("Abilities.Freeze.Cooldown", 3);
        }
        cooldown = file.getInt("Abilities.Freeze.Cooldown");
        if (!file.contains("Abilities.Freeze.Slow-Last-For")) {
            file.set("Abilities.Freeze.Slow-Last-For", 10);
        }
        duration = file.getInt("Abilities.Freeze.Slow-Last-For") * 20;
        if (!file.contains("Abilities.Freeze.Slow-Level")) {
            file.set("Abilities.Freeze.Slow-Level", 2);
        }
        amplifier = file.getInt("Abilities.Freeze.Slow-Level");
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
        return activationProjectile;
    }

    public boolean execute(Player p, PlayerData data, Event event) {
        if (event instanceof PlayerInteractEvent) {
            if (data.hasCooldown(p, "Freeze")) {
                return false;
            }
            data.setCooldown(p, "Freeze", cooldown, true);
            Kitbattle.getInstance().sendUseAbility(p, data);
            (p.launchProjectile(Snowball.class)).setMetadata("freeze", new FixedMetadataValue(Kitbattle.getInstance(), true));
            return true;
        }
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent)event;
        if (e.getDamager().hasMetadata("freeze")) {
            Player damaged = (Player) e.getEntity();
            damaged.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, duration, amplifier));
            return true;
        }
        return false;
    }
}

