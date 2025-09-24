package xyz.destiall.addons.abilities;

import me.wazup.kitbattle.PlayerData;
import me.wazup.kitbattle.abilities.Ability;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import xyz.destiall.addons.items.BowRebound;

public class Rebound extends Ability {
    @Override
    public String getName() {
        return "Rebound";
    }

    @Override
    public void load(FileConfiguration file) {
        if (!file.contains("Abilities.Rebound.Amplifier")) {
            file.set("Abilities.Rebound.Amplifier", 1.2);
        }
        BowRebound.AMPLIFIER = (float) file.getDouble("Abilities.Rebound.Amplifier", 1.2);
        if (!file.contains("Abilities.Rebound.Bounces")) {
            file.set("Abilities.Rebound.Bounces", 2);
        }
        BowRebound.BOUNCES = file.getInt("Abilities.Rebound.Bounces", 2);
        if (!file.contains("Abilities.Rebound.Spread")) {
            file.set("Abilities.Rebound.Spread", 0);
        }
        BowRebound.SPREAD = (float) file.getDouble("Abilities.Rebound.Spread", 0);
    }

    @Override
    public Material getActivationMaterial() {
        return null;
    }

    @Override
    public EntityType getActivationProjectile() {
        return EntityType.ARROW;
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
    public boolean execute(Player player, PlayerData playerData, Event event) {
        return true;
    }
}
