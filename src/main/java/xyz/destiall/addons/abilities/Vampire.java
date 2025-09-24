package xyz.destiall.addons.abilities;

import me.wazup.kitbattle.PlayerData;
import me.wazup.kitbattle.abilities.Ability;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Bat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import xyz.destiall.addons.Addons;
import xyz.destiall.addons.utils.Scheduler;

public class Vampire extends Ability {
    private int cooldown;
    private int lastsFor;
    private double speedMult;
    private final Material activationMaterial = Material.SKELETON_SKULL;

    public String getName() {
        return "Vampire";
    }

    public void load(FileConfiguration file) {
        if (!file.contains("Abilities.Vampire.Cooldown")) {
            file.set("Abilities.Vampire.Cooldown", 30);
        }
        cooldown = file.getInt("Abilities.Vampire.Cooldown");

        if (!file.contains("Abilities.Vampire.Bat-Lasts-For")) {
            file.set("Abilities.Vampire.Bat-Lasts-For", 10);
        }
        lastsFor = file.getInt("Abilities.Vampire.Bat-Lasts-For");

        if (!file.contains("Abilities.Vampire.Speed-Multiplier")) {
            file.set("Abilities.Vampire.Speed-Multiplier", 1);
        }
        speedMult = file.getDouble("Abilities.Vampire.Speed-Multiplier");
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

    public boolean execute(final Player player, PlayerData playerData, Event event) {
        if (playerData.hasCooldown(player, "Vampire")) {
            return false;
        }
        playerData.setCooldown(player, "Vampire", cooldown, true);
        final Bat bat = (Bat) player.getWorld().spawnEntity(player.getEyeLocation(), EntityType.BAT);
        bat.addPassenger(player);

        new Scheduler.TaskRunnable() {
            int i = 0;
            public void run() {
                if (i >= lastsFor * 20) {
                    bat.remove();
                    this.cancel();
                }
                Vector vector = player.getEyeLocation().getDirection();
                bat.setVelocity(vector.normalize().multiply(speedMult));
                ++i;
            }
        }.runTaskTimer(Addons.scheduler, 0L, 1L);

        return true;
    }
}