package xyz.destiall.addons.listeners;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import xyz.destiall.addons.Addons;
import xyz.destiall.addons.items.BowRebound;
import xyz.destiall.addons.items.FunFactory;
import xyz.destiall.addons.managers.BlockManager;
import xyz.destiall.addons.utils.Effects;
import xyz.destiall.addons.utils.Pair;
import xyz.destiall.addons.utils.Scheduler;
import xyz.destiall.addons.utils.Shooter;
import xyz.destiall.addons.valorant.Jett;
import xyz.destiall.addons.valorant.Neon;
import xyz.destiall.addons.valorant.Phoenix;
import xyz.destiall.addons.valorant.Sova;
import xyz.destiall.addons.valorant.common.Recon;

import java.util.*;
import java.util.stream.Collectors;

import static xyz.destiall.addons.items.FunFactory.color;

public class FunListener implements Listener {
    private final Map<UUID, Long> cooldown;
    private final Map<ArmorStand, Scheduler.Task> thrownKunais;
    private final Map<Entity, EntityShootBowEvent> entityShootBowEventMap;

    private final NamespacedKey bounceKey = new NamespacedKey(Addons.INSTANCE, "bounces");

    public FunListener(Addons plugin) {
        cooldown = new HashMap<>();
        thrownKunais = new HashMap<>();
        entityShootBowEventMap = new HashMap<>();
        Addons.scheduler.runTaskTimer(() -> {
            HashSet<UUID> remove = new HashSet<>();
            for (Map.Entry<UUID, Long> cd : cooldown.entrySet()) {
                try {
                    Player player = plugin.getServer().getPlayer(cd.getKey());
                    long current = System.currentTimeMillis();
                    if (cd.getValue() <= current) {
                        remove.add(cd.getKey());
                        if (player == null || !player.isOnline())
                            continue;
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
        }, 0L, 1L);
    }

    @EventHandler(priority= EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityShootBowEvent(EntityShootBowEvent event) {
        Entity projectile = event.getProjectile();
        PersistentDataContainer container = event.getProjectile().getPersistentDataContainer();
        if (is(event.getBow(), "REBOUND")) {
            if (projectile.getType() == EntityType.ARROW && !container.has(bounceKey)) {
                container.set(bounceKey, PersistentDataType.INTEGER, BowRebound.BOUNCES);
                entityShootBowEventMap.put(projectile, event);
            }
        } else if (is(event.getBow(), "SOVASCAN")) {
            entityShootBowEventMap.put(projectile, event);
            PersistentDataContainer bowContainer = event.getBow().getItemMeta().getPersistentDataContainer();
            int bounces = 0;
            if (bowContainer.has(Recon.scannerKey)) {
                bounces = bowContainer.get(Recon.scannerKey, PersistentDataType.INTEGER);
            }
            container.set(bounceKey, PersistentDataType.INTEGER, bounces);
            if (FunFactory.createItem("RECONDART").isSimilar(event.getConsumable())) {
                container.set(Recon.scannerKey, PersistentDataType.INTEGER, 2);
            } else {
                container.set(Sova.shockDartKey, PersistentDataType.INTEGER, 2);
            }
        }
    }

    @EventHandler(priority= EventPriority.HIGH, ignoreCancelled = true)
    public void onProjectileHitEvent(ProjectileHitEvent event) {
        Projectile entity = event.getEntity();
        if (entity.getShooter() instanceof Player && (entity.getType() == EntityType.ARROW || entity.getType() == EntityType.SPECTRAL_ARROW)) {
            PersistentDataContainer container = entity.getPersistentDataContainer();
            if (container.has(bounceKey)) {
                EntityShootBowEvent prevEvent = entityShootBowEventMap.get(entity);
                int prevBounceRate = container.getOrDefault(bounceKey, PersistentDataType.INTEGER, -1);
                AbstractArrow arrow = (AbstractArrow) entity;
                LivingEntity shooter = (LivingEntity) entity.getShooter();
                if (prevBounceRate <= 0) {
                    entityShootBowEventMap.remove(entity);

                    if (container.has(Sova.shockDartKey)) {
                        int radius = container.getOrDefault(Sova.shockDartKey, PersistentDataType.INTEGER, Sova.shockDartRadius);
                        Location start = event.getEntity().getLocation();
                        for (double phi = 0; phi <= Math.PI; phi += Math.PI / 8) {
                            for (double theta = 0; theta <= 2 * Math.PI; theta += Math.PI / 8) {
                                double x = Math.cos(theta) * Math.sin(phi);
                                double y = Math.cos(phi);
                                double z = Math.sin(theta) * Math.sin(phi);
                                Vector dir = new Vector(x, y, z);
                                Effects.spawnDust(start.clone().add(dir.multiply(radius)), Color.BLUE);
                            }
                        }

                        arrow.getWorld().getNearbyEntities(arrow.getLocation(), radius, radius, radius).forEach(en -> {
                            if (!(en instanceof LivingEntity))
                                return;

                            ((LivingEntity) en).damage(5, DamageSource.builder(DamageType.ARROW)
                                    .withDamageLocation(arrow.getLocation())
                                    .withDirectEntity(arrow)
                                    .withCausingEntity(shooter).build());
                        });
                    }
                }

                if (prevBounceRate > 0) {
                    Vector arrowVector = entity.getVelocity();
                    final double magnitude = Math.sqrt(Math.pow(arrowVector.getX(), 2) + Math.pow(arrowVector.getY(), 2) + Math.pow(arrowVector.getZ(), 2));
                    BlockFace blockFace = getBlockFace(event, entity, arrowVector);
                    if (blockFace != null) {
                        if (blockFace == BlockFace.SELF) {
                            blockFace = BlockFace.UP;
                        }
                        entity.remove();
                        double speed = magnitude * 0.6D;
                        if (speed < 0.3D)
                            return;
                        Vector N = new Vector(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ());
                        double dotProduct = arrowVector.dot(N);
                        Vector u = N.multiply(dotProduct).multiply(2);
                        Class<? extends AbstractArrow> arrowClass = Arrow.class;
                        if (arrow instanceof SpectralArrow) {
                            arrowClass = SpectralArrow.class;
                        }
                        AbstractArrow newArrow = entity.getWorld().spawnArrow(entity.getLocation(), arrowVector.subtract(u), (float) speed, BowRebound.SPREAD, arrowClass);
                        newArrow.getPersistentDataContainer().set(bounceKey, PersistentDataType.INTEGER, prevBounceRate - 1);
                        newArrow.setShooter(shooter);
                        newArrow.setLastDamageCause(entity.getLastDamageCause());
                        newArrow.setDamage(arrow.getDamage() * BowRebound.AMPLIFIER);
                        newArrow.setCritical(arrow.isCritical());
                        newArrow.setKnockbackStrength(arrow.getKnockbackStrength());
                        newArrow.setPickupStatus(arrow.getPickupStatus());
                        newArrow.setFireTicks(entity.getFireTicks());
                        if (container.has(Recon.scannerKey)) {
                            int scans = container.getOrDefault(Recon.scannerKey, PersistentDataType.INTEGER, 0);
                            newArrow.getPersistentDataContainer().set(Recon.scannerKey, PersistentDataType.INTEGER, scans);
                        }
                        if (container.has(Sova.shockDartKey)) {
                            container.set(Sova.shockDartKey, PersistentDataType.INTEGER, container.getOrDefault(Sova.shockDartKey, PersistentDataType.INTEGER, Sova.shockDartRadius));
                        }
                        EntityShootBowEvent newEvent = new EntityShootBowEvent(
                                shooter,
                                prevEvent.getBow(),
                                prevEvent.getConsumable(),
                                newArrow,
                                prevEvent.getHand(),
                                prevEvent.getForce(),
                                prevEvent.shouldConsumeItem());
                        entityShootBowEventMap.put(newArrow, prevEvent);
                        Addons.INSTANCE.getServer().getPluginManager().callEvent(newEvent);
                    }
                }

                if (container.has(Recon.scannerKey)) {
                    Sova sova = Addons.INSTANCE.getAgentManager().setAgent((Player) entity.getShooter(), Sova.class);
                    sova.recon(entity.getLocation(), entity);
                }
            }
        }
    }

    @Nullable
    public static BlockFace getBlockFace(ProjectileHitEvent event, Projectile entity, Vector arrowVector) {
        Location hitLoc = entity.getLocation();
        BlockIterator b = new BlockIterator(hitLoc.getWorld(), hitLoc.toVector(), arrowVector, 0, 3);
        Block blockBefore = event.getEntity().getLocation().getBlock();
        Block nextBlock = b.next();
        while (b.hasNext() && !nextBlock.getType().isSolid()) {
            blockBefore = nextBlock;
            nextBlock = b.next();
        }
        return nextBlock.getFace(blockBefore);
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
                item.setAmount(item.getAmount() - 1);
            } else if (is(item, "PHOENIXWALL")) {
                Phoenix phoenix = Addons.INSTANCE.getAgentManager().setAgent(player, Phoenix.class);
                phoenix.wall(player, player.getLocation());
                item.setAmount(item.getAmount() - 1);
            } else if (is(item, "PHOENIXFLASH")) {
                Phoenix phoenix = Addons.INSTANCE.getAgentManager().setAgent(player, Phoenix.class);
                phoenix.flash(player, player.getLocation(), false);
                item.setAmount(item.getAmount() - 1);
            } else if (is(item, "JETTBLADES")) {
                Jett jett = Addons.INSTANCE.getAgentManager().setAgent(player, Jett.class);
                jett.activateBlades();
                item.setAmount(item.getAmount() - 1);
            }
            return;
        }
        if (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_AIR) {
            if (is(e.getItem(), "SWORD")) {
                e.setUseItemInHand(Event.Result.DENY);
                throwSword(player, player.getLocation().add(0, player.getEyeHeight() * 0.5, 0), item, 0.75f, true);
            } else if (is(e.getItem(), "SOVASCAN")) {
                ItemMeta meta = e.getItem().getItemMeta();
                PersistentDataContainer container = meta.getPersistentDataContainer();
                int bounces = 0;
                if (container.has(Recon.scannerKey)) {
                    bounces = container.get(Recon.scannerKey, PersistentDataType.INTEGER);
                }
                bounces++;
                if (bounces > 2) {
                    bounces = 0;
                }
                container.set(Recon.scannerKey, PersistentDataType.INTEGER, bounces);
                meta.setDisplayName(color("&3Sova Recon Dart (" + bounces + ")"));
                e.getItem().setItemMeta(meta);
            } else if (is(item, "PHOENIXFLASH")) {
                Phoenix phoenix = Addons.INSTANCE.getAgentManager().setAgent(player, Phoenix.class);
                phoenix.flash(player, player.getLocation(), true);
                item.setAmount(item.getAmount() - 1);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onClickEntity(PlayerInteractAtEntityEvent e) {
        if (e.getRightClicked() instanceof ArmorStand) {
            ArmorStand as = (ArmorStand) e.getRightClicked();
            if (thrownKunais.containsKey(as)) {
                e.setCancelled(true);
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

    private boolean is(ItemStack item, String s) {
        if (item == null) return false;
        return FunFactory.isItem(item, s);
    }

    public static ArmorStand spawnStand(World world, Location loc) {
        final Vector dir = loc.getDirection();
        final ArmorStand as = world.spawn(loc.add(dir), ArmorStand.class);
        as.teleportAsync(loc);
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
        Addons.scheduler.runTask(() -> {
            final Location origin = loc.clone();
            final Vector dir = loc.getDirection().multiply(speed);
            final ArmorStand as = spawnStand(player.getWorld(), loc);
            as.setItemInHand(item);
            thrownKunais.put(as, Addons.scheduler.runTaskTimer(()  -> {
                as.teleportAsync(loc.add(dir));
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
            }, player, 0L, 1L));
        }, player);
    }
}
