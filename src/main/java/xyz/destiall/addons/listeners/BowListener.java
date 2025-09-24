package xyz.destiall.addons.listeners;

import me.wazup.kitbattle.Kit;
import me.wazup.kitbattle.KitbattleAPI;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.metadata.FixedMetadataValue;
import xyz.destiall.addons.Addons;
import xyz.destiall.addons.abilities.Rebound;
import xyz.destiall.addons.items.BowRebound;

public class BowListener implements Listener {

    @EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityShootBowEvent(EntityShootBowEvent event) {
        LivingEntity entity = event.getEntity();
        Entity projectile = event.getProjectile();
        if (entity instanceof Player && projectile.getType() == EntityType.ARROW) {
            Player p = (Player) entity;
            Kit kit = KitbattleAPI.getPlayerData(p).getKit();
            if (kit != null && kit.getProjectileAbilities().stream().anyMatch(a -> a instanceof Rebound)) {
                projectile.setMetadata("bounces", new FixedMetadataValue(Addons.INSTANCE, BowRebound.BOUNCES));
            }
        }
    }
}
