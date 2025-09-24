package xyz.destiall.addons.listeners;

import me.wazup.kitbattle.Kit;
import me.wazup.kitbattle.KitbattleAPI;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import xyz.destiall.addons.abilities.SizeChange;

public class SizeChangeListener implements Listener {

    // Spawn coordinates
    private static final double SPAWN_X = -0.5;
    private static final double SPAWN_Y = 137.0;
    private static final double SPAWN_Z = -2.5;
    private static final double SPAWN_RADIUS = 2.0;

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        SizeChange.resetPlayerSizeStatic(player);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        SizeChange.resetPlayerSizeStatic(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        SizeChange.resetPlayerSizeStatic(player);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();

        if (to == null) return;

        // Check if player has the "Sizer" kit
        try {
            Kit kit = KitbattleAPI.getPlayerData(player).getKit();
            if (kit == null || !kit.getName().equals("Sizer")) {
                return;
            }
        } catch (Exception e) {
            // If KitBattle API fails, skip this check
            return;
        }

        // Check if teleported to spawn coordinates (within radius)
        double distance = Math.sqrt(
                Math.pow(to.getX() - SPAWN_X, 2) +
                        Math.pow(to.getY() - SPAWN_Y, 2) +
                        Math.pow(to.getZ() - SPAWN_Z, 2)
        );

        if (distance <= SPAWN_RADIUS) {
            SizeChange.resetPlayerSizeStatic(player);
        }
    }
}