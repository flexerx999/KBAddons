package xyz.destiall.addons.abilities;

import me.wazup.kitbattle.PlayerData;
import me.wazup.kitbattle.abilities.Ability;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class Chemist extends Ability {
    private int potionCount;
    private ItemStack potion;

    public String getName() {
        return "Chemist";
    }

    public void load(FileConfiguration file) {
        if (!file.contains("Abilities.Chemist.Potion-Refill")) {
            file.set("Abilities.Chemist.Potion-Refill", 1);
        }
        potionCount = file.getInt("Abilities.Chemist.Potion-Refill");
        if (!file.contains("Abilities.Chemist.Potion-Strength")) {
            file.set("Abilities.Chemist.Potion-Strength", 2);
        }
        int potStrength = file.getInt("Abilities.Chemist.Potion-Strength");
        potion = new ItemStack(Material.SPLASH_POTION, 1);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        meta.setBasePotionType(PotionType.HARMING);
        meta.addCustomEffect(PotionEffectType.INSTANT_DAMAGE.createEffect(1, potStrength), true);
        potion.setItemMeta(meta);
    }

    public Material getActivationMaterial() {
        return null;
    }

    public EntityType getActivationProjectile() {
        return EntityType.SPLASH_POTION;
    }

    public boolean isAttackActivated() {
        return true;
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
    public boolean execute(Player player, PlayerData playerData, Event event) {
        EntityDamageEvent e;
        if (event instanceof EntityDamageEvent &&
                (e = (EntityDamageEvent)event).getEntity() instanceof LivingEntity &&
                ((LivingEntity)e.getEntity()).getHealth() - e.getFinalDamage() <= 0.0) {
            for (int i = 0; i < potionCount; ++i) {
                player.getInventory().addItem(potion.clone());
            }
            return true;
        }
        return false;
    }
}