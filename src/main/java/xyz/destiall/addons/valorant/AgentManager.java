package xyz.destiall.addons.valorant;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import xyz.destiall.addons.Addons;
import xyz.destiall.addons.utils.Pair;
import xyz.destiall.addons.utils.Scheduler;
import xyz.destiall.addons.valorant.common.Flasher;
import xyz.destiall.addons.valorant.common.Recon;
import xyz.destiall.addons.valorant.common.Stunner;
import xyz.destiall.addons.valorant.packet.BlockPacket;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class AgentManager implements Listener {
    private final Map<UUID, Agent> agentMap;
    private final Addons plugin;

    private final NamespacedKey suppressedKey = new NamespacedKey(Addons.INSTANCE, "suppressed");

    private final Map<UUID, Pair<Pair<Long, Long>, BlockPacket>> flashedMap;
    private final Map<UUID, Pair<Long, Long>> suppressedMap;

    public AgentManager(Addons plugin) {
        this.plugin = plugin;
        this.agentMap = new HashMap<>();
        this.flashedMap = new HashMap<>();
        this.suppressedMap = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        Addons.scheduler.runTaskTimer(() -> {
            Iterator<Map.Entry<UUID, Pair<Pair<Long, Long>, BlockPacket>>> iterator = flashedMap.entrySet().iterator();
            while (iterator.hasNext()) {
                long current = System.currentTimeMillis();
                Map.Entry<UUID, Pair<Pair<Long, Long>, BlockPacket>> entry = iterator.next();

                Entity entity = plugin.getServer().getEntity(entry.getKey());
                if (entity == null) {
                    iterator.remove();
                    continue;
                }
                LivingEntity livingEntity = (LivingEntity) entity;
                Pair<Pair<Long, Long>, BlockPacket> pair = entry.getValue();
                long time = pair.getKey().getKey();
                long duration = pair.getKey().getValue();
                BlockPacket packet = pair.getValue();
                if (time < current) {
                    iterator.remove();
                    packet.remove();
                }

                Addons.scheduler.runTask(() -> {
                    if (time < current) {
                        livingEntity.removePotionEffect(PotionEffectType.BLINDNESS);
                        if (livingEntity instanceof Player) {
                            Player player = (Player) livingEntity;
                            player.setExp(0);
                        }
                    } else {
                        packet.teleport(livingEntity.getEyeLocation());
                        if (livingEntity instanceof Player) {
                            Player player = (Player) livingEntity;
                            long diff = time - current;
                            player.setExp((float) diff / duration);
                        }
                    }
                }, livingEntity);
            }
        }, 0L, 1L);
    }

    public void setFlashed(UUID uuid, BlockPacket packet, double duration) {
        Pair<Pair<Long, Long>, BlockPacket> current = flashedMap.get(uuid);
        long dur = (long) (duration * 1000L);
        long flashExpiry = (long) (System.currentTimeMillis() + dur);
        if (current == null) {
            flashedMap.put(uuid, new Pair<>(new Pair<>(flashExpiry, dur), packet));
            return;
        }

        long currentTime = current.getKey().getKey();
        if (flashExpiry > currentTime) {
            current.getKey().setKey(flashExpiry);
        }
    }

    @SuppressWarnings("unchecked")
    public @NotNull <A extends Agent> A setAgent(Player player, Class<A> clazz) {
        if (agentMap.containsKey(player.getUniqueId())) {
            Agent a = agentMap.get(player.getUniqueId());
            if (a.getClass().equals(clazz)) {
                return (A) agentMap.get(player.getUniqueId());
            }
            unsetAgent(player);
        }
        try {
            Agent agent = clazz.getDeclaredConstructor(Player.class).newInstance(player);
            agentMap.put(player.getUniqueId(), agent);
            plugin.getServer().getPluginManager().registerEvents(agent, plugin);
            return (A) agent;
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RuntimeException("idk what happened here :(");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        PersistentDataContainer data = e.getPlayer().getPersistentDataContainer();
        data.remove(Recon.scannerKey);
        data.remove(Flasher.flashedKey);
        data.remove(Stunner.stunnedKey);

        data.remove(Sova.scannedKey);
        data.remove(Phoenix.phoenixFlashed);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        unsetAgent(e.getPlayer());
    }

    public Map<UUID, Agent> getAgentMap() {
        return agentMap;
    }

    public void unsetAgent(Player player) {
        Agent agent = agentMap.remove(player.getUniqueId());
        if (agent == null)
            return;

        agent.unset();
        agent.getTasks().forEach(Scheduler.Task::cancel);
        agent.getTasks().clear();
        HandlerList.unregisterAll(agent);
    }
}
