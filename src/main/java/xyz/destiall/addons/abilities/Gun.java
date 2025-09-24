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
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class Gun extends Ability {
    private int cooldown;
    private double damage;
    private final Material activationMaterial = Material.WOODEN_HOE;
    private final EntityType activationProjectile = EntityType.SNOWBALL;

    public String getName() {
        return "Gun";
    }

    public void load(FileConfiguration file) {
        if (!file.contains("Abilities.Gun.Cooldown")) {
            file.set("Abilities.Gun.Cooldown", 30);
        }
        cooldown = file.getInt("Abilities.Gun.Cooldown");
        if (!file.contains("Abilities.Gun.Damage")) {
            file.set("Abilities.Gun.Damage", 5);
        }
        damage = file.getDouble("Abilities.Gun.Damage");
    }

    public Material getActivationMaterial() {
        return activationMaterial;
    }

    public EntityType getActivationProjectile() {
        return activationProjectile;
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

    @Override
    public boolean execute(Player p, PlayerData data, Event event) {
        if (event instanceof PlayerInteractEvent) {
            PlayerInteractEvent e = (PlayerInteractEvent) event;
            if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (data.hasCooldown(p, "Gun")) {
                    return false;
                }
                data.setCooldown(p, "Gun", cooldown, true);
                Kitbattle.getInstance().sendUseAbility(p, data);
                p.launchProjectile(Snowball.class).setMetadata("gun", new FixedMetadataValue(Kitbattle.getInstance(), true));
                return true;
            }
        } else if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
            if (e.getDamager().hasMetadata("gun")) {
                e.setDamage(damage);
                return true;
            }
        }
        return false;
    }
}
