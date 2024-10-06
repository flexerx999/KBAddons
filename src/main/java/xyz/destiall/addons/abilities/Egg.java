package xyz.destiall.addons.abilities;

import me.wazup.kitbattle.Kitbattle;
import me.wazup.kitbattle.PlayerData;
import me.wazup.kitbattle.abilities.Ability;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import xyz.destiall.addons.listeners.EggListener;

public class Egg extends Ability {
    public double damage;
    public int bounces;
    public double speed;
    public final EntityType type = EntityType.EGG;
    public static Egg INSTANCE;

    public Egg() {
        INSTANCE = this;
    }

    @Override
    public String getName() {
        return "Egg";
    }

    @Override
    public void load(FileConfiguration file) {
        if (!file.contains("Abilities.Egg.Bounces")) {
            file.set("Abilities.Egg.Bounces", 3);
        }
        bounces = file.getInt("Abilities.Egg.Bounces", 3);
        if (!file.contains("Abilities.Egg.Damage")) {
            file.set("Abilities.Egg.Damage", 10);
        }
        damage = file.getDouble("Abilities.Egg.Damage", 10.f);
        if (!file.contains("Abilities.Egg.Speed")) {
            file.set("Abilities.Egg.Speed", 2);
        }
        speed = file.getDouble("Abilities.Egg.Speed", 2.f);
    }

    @Override
    public Material getActivationMaterial() {
        return null;
    }

    @Override
    public EntityType getActivationProjectile() {
        return type;
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
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
            if (e.getDamager().getType() == type) {
                org.bukkit.entity.Egg prevEgg = (org.bukkit.entity.Egg) e.getDamager();
                Kitbattle.getInstance().sendUseAbility(player, data);
                LivingEntity le = (LivingEntity) e.getEntity();
                int bounces = 0;
                if (prevEgg.hasMetadata("bounces")) {
                    bounces = prevEgg.getMetadata("bounces").get(0).asInt();
                    prevEgg.removeMetadata("bounces", Kitbattle.getInstance());
                    if (bounces == this.bounces) {
                        le.damage(damage * bounces);
                        return true;
                    }
                }
                le.damage(damage * (++bounces));
                org.bukkit.entity.Egg egg = (org.bukkit.entity.Egg) e.getEntity().getWorld()
                        .spawnEntity(e.getEntity().getLocation().add(0, 3, 0), type);
                double x = Math.random() * 2 - 1;
                double z = Math.random() * 2 - 1;
                Vector v = new Vector(x, 2, z).normalize().multiply(speed);
                egg.setVelocity(v);
                egg.setShooter(prevEgg.getShooter());
                egg.setBounce(prevEgg.doesBounce());
                egg.setLastDamageCause(prevEgg.getLastDamageCause());
                egg.setFallDistance(prevEgg.getFallDistance());
                egg.setMetadata("bounces", new FixedMetadataValue(Kitbattle.getInstance(), bounces));
                EggListener.INSTANCE.EGGS.add(egg);
                return true;
            }
        }
        return false;
    }
}
