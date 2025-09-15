package xyz.destiall.addons.valorant.common;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import xyz.destiall.addons.Addons;

import java.util.List;

public interface Stunner {
    void stun(Location origin);

    boolean selfStun();

    double stunDuration();

    NamespacedKey stunnedKey = new NamespacedKey(Addons.INSTANCE, "stunned");

    default void stunArea(Player self, List<LivingEntity> entities) {
        PotionEffect nauseaEffect = new PotionEffect(PotionEffectType.NAUSEA, (int) (stunDuration() * 20) + 40, 2, true, false);
        PotionEffect slownessEffect = new PotionEffect(PotionEffectType.SLOWNESS, (int) (stunDuration() * 20) + 40, 2, true, false);
        for (LivingEntity entity : entities) {
            if (entity.getUniqueId().equals(self.getUniqueId()) && !selfStun())
                continue;

            PersistentDataContainer data = entity.getPersistentDataContainer();
            if (data.has(stunnedKey)) {
                entity.removePotionEffect(PotionEffectType.NAUSEA);
                entity.removePotionEffect(PotionEffectType.SLOWNESS);
                data.remove(stunnedKey);
            }
            data.set(stunnedKey, PersistentDataType.BOOLEAN, true);
            entity.addPotionEffect(nauseaEffect);
            entity.addPotionEffect(slownessEffect);
        }
    }
}
