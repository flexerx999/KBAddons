package xyz.destiall.addons.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;
import xyz.destiall.addons.Addons;
import xyz.destiall.addons.managers.BlockManager;
import xyz.destiall.addons.managers.CombatLogManager;
import xyz.destiall.addons.managers.FlagManager;
import xyz.destiall.addons.managers.StrikeManager;
import xyz.destiall.addons.utils.Pair;

public class FlagListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onItemClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player) {
            Player player = (Player) e.getWhoClicked();
            if (e.getInventory() == player.getInventory()) {
                if (queryFlag(player.getLocation(), FlagManager.NO_MOVE_ITEMS)) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItem(InventoryDragEvent e) {
        if (e.getWhoClicked() instanceof Player) {
            Player player = (Player) e.getWhoClicked();
            if (e.getInventory() == player.getInventory()) {
                if (queryFlag(player.getLocation(), FlagManager.NO_MOVE_ITEMS)) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlaceBlock(BlockPlaceEvent e) {
        int timer = checkBlock(e.getPlayer(), e.getBlockPlaced());
        if (timer == -1)
            return;

        BlockManager.EXPIRIES.put(new Pair<>(e.getBlockPlaced(), e.getBlockReplacedState()), System.currentTimeMillis() + timer);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSnowballHit(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof Snowball))
            return;

        Snowball snowball = (Snowball) e.getEntity();
        if (queryFlag(snowball.getLocation(), FlagManager.SNOWBALL_BREAK_SNOW)) {
            Vector vector = snowball.getVelocity();
            Location hitLoc = snowball.getLocation();
            BlockIterator b = new BlockIterator(hitLoc.getWorld(), hitLoc.toVector(), vector, 0, 3);
            Block blockBefore = hitLoc.getBlock();
            Block nextBlock = b.next();
            while (b.hasNext() && nextBlock.getType() == Material.AIR) {
                blockBefore = nextBlock;
                nextBlock = b.next();
            }
            BlockFace blockFace = nextBlock.getFace(blockBefore);
            if (blockFace != null && nextBlock.getType() == Material.SNOW_BLOCK) {
                if (Addons.SP) {
                    StrikeManager.addBlockChange(nextBlock);
                }
                nextBlock.setType(Material.AIR);
                snowball.remove();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL && event.getDamage() >= 80.0) {
            if (queryFlag(event.getEntity().getLocation(), FlagManager.DISABLE_FALLDMG))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCrafting(CraftItemEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        if (queryFlag(player.getLocation(), FlagManager.DISABLE_CRAFTING)) {
            e.setCancelled(true);
            player.closeInventory();
            player.sendMessage(ChatColor.RED + "You are not allowed to craft in this area!");
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.isCancelled()) return;
        if (e instanceof PlayerTeleportEvent) {
            PlayerTeleportEvent ev = (PlayerTeleportEvent) e;
            if (ev.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) return;
        }
        Player player = e.getPlayer();
        if (Addons.CL && CombatLogManager.isCombatLogged(player) && queryFlag(e.getTo(), FlagManager.DISABLE_ENTERING_COMBATLOG)) {
            e.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You are not allowed to enter this area while combat logged!");
        }
    }

    private int checkBlock(Player player, Block block) {
        Location location = block.getLocation();
        ApplicableRegionSet set = getRegion(location);
        if (set == null) return -1;
        IntegerFlag flag = FlagManager.getFlag(FlagManager.REMOVE_PLACE_BLOCKs, IntegerFlag.class);
        if (flag == null) return -1;
        Integer timer = set.queryValue(new BukkitPlayer(WorldGuardPlugin.inst(), player), flag);
        return timer == null || timer <= 0 ? -1 : timer;
    }

    private boolean queryFlag(Location location, String flag) {
        ApplicableRegionSet set = getRegion(location);
        if (set == null)
            return false;
        StateFlag f = FlagManager.getFlag(flag, StateFlag.class);
        if (f == null)
            return false;
        return set.queryValue(null, f) == StateFlag.State.ALLOW;
    }

    private ApplicableRegionSet getRegion(Location location) {
        RegionManager rm = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(location.getWorld()));
        if (rm == null) return null;
        return rm.getApplicableRegions(BukkitAdapter.asBlockVector(location));
    }
}
