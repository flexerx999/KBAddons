package xyz.destiall.addons.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import xyz.destiall.addons.Addons;
import xyz.destiall.addons.items.BowRebound;
import xyz.destiall.addons.items.FunFactory;
import xyz.destiall.addons.managers.BlockManager;
import xyz.destiall.addons.utils.Pair;
import xyz.destiall.addons.utils.Shooter;
import xyz.destiall.addons.valorant.Neon;
import xyz.destiall.addons.valorant.Phoenix;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class FunListener implements Listener {
    private final HashMap<UUID, Long> cooldown;
    private final HashMap<ArmorStand, BukkitTask> thrownKunais;
    private final HashMap<UUID, Long> jettUsers;
    private final HashMap<UUID, HashMap<ArmorStand, Vector>> hoveringKunais;
    private final ItemStack kunai;

    public FunListener() {
        cooldown = new HashMap<>();
        thrownKunais = new HashMap<>();
        hoveringKunais = new HashMap<>();
        jettUsers = new HashMap<>();
        kunai = new ItemStack(Material.IRON_SWORD);
        Bukkit.getScheduler().runTaskTimer(Addons.INSTANCE, () -> {
            HashSet<UUID> remove = new HashSet<>();
            for (Map.Entry<UUID, Long> cd : cooldown.entrySet()) {
                try {
                    Player player = Bukkit.getPlayer(cd.getKey());
                    long current = System.currentTimeMillis();
                    if (cd.getValue() <= current) {
                        remove.add(cd.getKey());
                        if (player == null || !player.isOnline()) continue;
                        player.setExp(0);
                        continue;
                    }
                    if (player == null || !player.isOnline()) {
                        remove.add(cd.getKey());
                        continue;
                    }
                    long diff = cd.getValue() - current;
                    player.setExp((float) diff / Addons.INSTANCE.getConfig().getInt("quake-gun-delay", 5000));
                } catch (Exception ignored) {}
            }
            for (UUID rm : remove) {
                cooldown.remove(rm);
            }
            remove.clear();
            for (Map.Entry<UUID, Long> cd : jettUsers.entrySet()) {
                try {
                    long current = System.currentTimeMillis();
                    if (cd.getValue() <= current) {
                        remove.add(cd.getKey());
                        continue;
                    }
                    Player player = Bukkit.getPlayer(cd.getKey());
                    if (player == null || !player.isOnline()) {
                        remove.add(cd.getKey());
                    }
                } catch (Exception ignored) {}
            }
            for (UUID rm : remove) {
                jettUsers.remove(rm);
                for (Map.Entry<ArmorStand, Vector> en : hoveringKunais.get(rm).entrySet()) {
                    en.getKey().remove();
                }
                hoveringKunais.remove(rm).clear();
            }
            remove.clear();
        }, 0L, 1L);
    }

    @EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityShootBowEvent(EntityShootBowEvent event) {
        if (is(event.getBow(), "REBOUND")) {
            Entity projectile = event.getProjectile();
            if (projectile.getType() == EntityType.ARROW) {
                projectile.setMetadata("bounces", new FixedMetadataValue(Addons.INSTANCE, 0));
            }
        }
    }

    @EventHandler(priority= EventPriority.HIGH, ignoreCancelled = true)
    public void onProjectileHitEvent(ProjectileHitEvent event) {
        Projectile entity = event.getEntity();
        if (entity.getShooter() instanceof Player && entity.getType() == EntityType.ARROW && entity.hasMetadata("bounces")) {
            Arrow arrow = (Arrow) entity;
            LivingEntity shooter = (LivingEntity) entity.getShooter();
            Vector arrowVector = entity.getVelocity();
            final double magnitude = Math.sqrt(Math.pow(arrowVector.getX(), 2) + Math.pow(arrowVector.getY(), 2) + Math.pow(arrowVector.getZ(), 2));
            Location hitLoc = entity.getLocation();
            BlockIterator b = new BlockIterator(hitLoc.getWorld(), hitLoc.toVector(), arrowVector, 0, 3);
            Block blockBefore = event.getEntity().getLocation().getBlock();
            Block nextBlock = b.next();
            while (b.hasNext() && !nextBlock.getType().isSolid()) {
                blockBefore = nextBlock;
                nextBlock = b.next();
            }
            BlockFace blockFace = nextBlock.getFace(blockBefore);
            if (blockFace != null) {
                if (blockFace == BlockFace.SELF) {
                    blockFace = BlockFace.UP;
                }
                entity.remove();
                double speed = magnitude * 0.6D;
                if (speed < 0.3D) return;
                Vector N = new Vector(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ());
                double dotProduct = arrowVector.dot(N);
                Vector u = N.multiply(dotProduct).multiply(2);
                Arrow newArrow = entity.getWorld().spawnArrow(entity.getLocation(), arrowVector.subtract(u), (float) speed, BowRebound.SPREAD);
                List<MetadataValue> metaDataValues = entity.getMetadata("bounces");
                if (!metaDataValues.isEmpty()) {
                    int prevBouncingRate = metaDataValues.get(0).asInt();
                    if (prevBouncingRate != BowRebound.BOUNCES) {
                        newArrow.setMetadata("bounces", new FixedMetadataValue(Addons.INSTANCE, ++prevBouncingRate));
                    }
                }
                newArrow.setShooter(shooter);
                newArrow.setLastDamageCause(entity.getLastDamageCause());
                newArrow.setDamage(arrow.getDamage() * BowRebound.AMPLIFIER);
                newArrow.setFireTicks(entity.getFireTicks());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent e) {
        if (is(e.getItem(), "GHEAD")) {
            e.getPlayer().addPotionEffect(PotionEffectType.REGENERATION.createEffect(5 * 20, 1));
            e.getPlayer().addPotionEffect(PotionEffectType.RESISTANCE.createEffect(5 * 20, 0));
            e.getPlayer().addPotionEffect(PotionEffectType.ABSORPTION.createEffect(180 * 20, 0));
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack item = e.getItem();
        if (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (is(item, "QUAKE")) {
                Long delay = cooldown.get(player.getUniqueId());
                if (delay == null) {
                    Shooter.shoot(player, player.getEyeLocation(), player.getLocation().getDirection(), Addons.INSTANCE.getConfig().getInt("quake-gun-damage", 20));
                    cooldown.put(player.getUniqueId(), System.currentTimeMillis() + Addons.INSTANCE.getConfig().getInt("quake-gun-delay", 5000));
                }
            } else if (is(item, "JETT")) {
                if (jettUsers.containsKey(player.getUniqueId())) return;
                e.setUseItemInHand(Event.Result.DENY);
                player.getInventory().setItemInHand(null);
                jettUsers.put(player.getUniqueId(), System.currentTimeMillis() + 5000);
                spawnBlades(player);
            } else if (is(item, "OP_QUAKE")) {
                Long delay = cooldown.get(player.getUniqueId());
                if (delay == null) {
                    for (double phi = 0; phi <= Math.PI; phi += Math.PI / 4) {
                        for (double theta = 0; theta <= 2 * Math.PI; theta += Math.PI / 4) {
                            double x = Math.cos(theta) * Math.sin(phi);
                            double y = Math.cos(phi);
                            double z = Math.sin(theta) * Math.sin(phi);
                            Vector direction = new Vector(x, y, z);
                            Shooter.shoot(player, player.getEyeLocation(), direction, Addons.INSTANCE.getConfig().getInt("quake-gun-damage", 20));
                        }
                    }
                    cooldown.put(player.getUniqueId(), System.currentTimeMillis() + Addons.INSTANCE.getConfig().getInt("quake-gun-delay", 5000));
                }
            } else if (is(item, "NEONWALL")) {
                Neon neon = Addons.INSTANCE.getAgentManager().setAgent(player, Neon.class);
                neon.wall(player, player.getLocation());
            } else if (is(item, "PHOENIXWALL")) {
                Phoenix phoenix = Addons.INSTANCE.getAgentManager().setAgent(player, Phoenix.class);
                phoenix.wall(player, player.getLocation());
            } else if (is(item, "PHOENIXFLASH")) {
                Phoenix phoenix = Addons.INSTANCE.getAgentManager().setAgent(player, Phoenix.class);
                phoenix.flash(player, player.getLocation());
            }
            return;
        }
        if (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_AIR) {
            if (jettUsers.containsKey(player.getUniqueId())) {
                Location loc = player.getLocation().add(0, player.getEyeHeight() * 0.5, 0);
                throwSword(player, loc, kunai, 1.25f, false);
                return;
            }
            if (is(e.getItem(), "SWORD")) {
                e.setUseItemInHand(Event.Result.DENY);
                throwSword(player, player.getLocation().add(0, player.getEyeHeight() * 0.5, 0), item, 0.75f, true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onClickEntity(PlayerInteractAtEntityEvent e) {
        if (e.getRightClicked() instanceof ArmorStand) {
            ArmorStand as = (ArmorStand) e.getRightClicked();
            if (thrownKunais.containsKey(as)) {
                e.setCancelled(true);
                return;
            }
            for (HashMap<ArmorStand, Vector> en : hoveringKunais.values()) {
                if (en.get(as) != null) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof ArmorStand) {
            ArmorStand as = (ArmorStand) e.getEntity();
            if (thrownKunais.containsKey(as)) {
                e.setCancelled(true);
                e.setDamage(0);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (is(e.getItemInHand(), "BRIDGE")) {
            BlockManager.EXPIRIES.put(new Pair<>(e.getBlockPlaced(), e.getBlockReplacedState()), System.currentTimeMillis() + Addons.INSTANCE.getConfig().getInt("bridge-block-expiry-ms", 5000));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        BlockManager.remove(e.getBlock());
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (!hoveringKunais.containsKey(player.getUniqueId())) return;
        Location loc = player.getLocation().add(0, player.getPlayer().getEyeHeight() * 0.5, 0);
        Vector dir = loc.getDirection().setY(0).normalize();
        loc.subtract(dir.clone().multiply(0.3));
        Vector right = dir.crossProduct(new Vector(0, 1, 0)).normalize();
        loc.subtract(right.multiply(0.2));
        for (Map.Entry<ArmorStand, Vector> entry : hoveringKunais.get(player.getUniqueId()).entrySet()) {
            Vector vector = entry.getValue();
            double yaw = Math.toRadians(-loc.getYaw());
            double xRotate = Math.cos(yaw) * vector.getX() + Math.sin(yaw) * vector.getZ();
            double zRotate = -Math.sin(yaw) * vector.getX() + Math.cos(yaw) * vector.getZ();
            EulerAngle angle = EulerAngle.ZERO.setX(Math.toRadians(loc.getPitch() - 10));
            entry.getKey().setRightArmPose(angle);
            entry.getKey().teleport(loc.add(xRotate, 0, zRotate));
            loc.subtract(xRotate, 0, zRotate);
        }
    }

    private boolean is(ItemStack item, String s) {
        if (item == null) return false;
        return FunFactory.isItem(item, s);
    }

    private void spawnBlades(Player player) {
        Location loc = player.getLocation().add(0, player.getEyeHeight() * 0.5, 0);
        Vector dir = loc.getDirection().setY(0).normalize();
        loc.subtract(dir.clone().multiply(0.3));
        Vector right = dir.crossProduct(new Vector(0, 1, 0)).normalize();
        loc.subtract(right.multiply(0.2));
        HashMap<ArmorStand, Vector> map = new HashMap<>();
        for (double i = 0; i <= Math.PI; i += Math.PI / 4) {
            double x = Math.cos(i);
            double z = Math.sin(i);
            double yaw = Math.toRadians(-loc.getYaw());
            double xRotate = Math.cos(yaw) * x + Math.sin(yaw) * z;
            double zRotate = -Math.sin(yaw) * x + Math.cos(yaw) * z;
            loc.add(xRotate, 0, zRotate);
            final ArmorStand as = spawnStand(loc.getWorld(), loc);
            as.setItemInHand(kunai);
            map.put(as, new Vector(x, 0, z));
            loc.subtract(xRotate, 0, zRotate);
        }
        hoveringKunais.put(player.getUniqueId(), map);
    }

    private ArmorStand spawnStand(World world, Location loc) {
        final Vector dir = loc.getDirection();
        final ArmorStand as = world.spawn(loc.add(dir), ArmorStand.class);
        as.teleport(loc);
        as.setArms(true);
        as.setGravity(false);
        as.setMaxHealth(100);
        as.setHealth(100);
        as.setVelocity(dir);
        as.setCanPickupItems(false);
        as.setBasePlate(false);
        as.setMarker(true);
        as.setSmall(true);
        as.setVisible(false);
        EulerAngle angle = EulerAngle.ZERO.setX(Math.toRadians(loc.getPitch() - 10));
        as.setRightArmPose(angle);
        return as;
    }

    public void throwSword(Player player, Location loc, ItemStack item, float speed, boolean ret) {
        player.getInventory().setItemInHand(null);
        Bukkit.getScheduler().runTask(Addons.INSTANCE, () -> {
            final Location origin = loc.clone();
            final Vector dir = loc.getDirection().multiply(speed);
            final ArmorStand as = spawnStand(player.getWorld(), loc);
            as.setItemInHand(item);
            thrownKunais.put(as, Bukkit.getScheduler().runTaskTimer(Addons.INSTANCE, () -> {
                as.teleport(loc.add(dir));
                List<Entity> hit = as.getNearbyEntities(0.5, 0.5, 0.5).stream().filter(e -> e instanceof LivingEntity && e != as && e != player).collect(Collectors.toList());
                for (Entity e : hit) {
                    LivingEntity live = (LivingEntity) e;
                    live.damage(Addons.INSTANCE.getConfig().getInt("throwing-knives-damage", 5), player);
                }
                if (loc.distance(origin) >= Addons.INSTANCE.getConfig().getInt("throwing-knives-distance", 10)) {
                    thrownKunais.remove(as).cancel();
                    as.remove();
                    if (ret) player.getInventory().addItem(item);
                }
            }, 0L, 1L));
        });
    }
}
