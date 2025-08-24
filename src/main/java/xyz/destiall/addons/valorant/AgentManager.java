package xyz.destiall.addons.valorant;

import com.Zrips.CMI.events.CMIArmorChangeEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.scheduler.BukkitTask;
import xyz.destiall.addons.Addons;
import xyz.destiall.addons.valorant.common.Flasher;
import xyz.destiall.addons.valorant.common.Recon;
import xyz.destiall.addons.valorant.common.Stunner;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AgentManager implements Listener {
    private final Map<UUID, Agent> agentMap;
    private final Addons plugin;

    private final NamespacedKey suppressedKey = new NamespacedKey(Addons.INSTANCE, "suppressed");

    public AgentManager(Addons plugin) {
        this.plugin = plugin;
        this.agentMap = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public <A extends Agent> A setAgent(Player player, Class<A> clazz) {
        if (agentMap.containsKey(player.getUniqueId())) {
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
        return null;
    }

    @EventHandler
    public void onRemove(CMIArmorChangeEvent e) {
        if (e.getPlayer().getPersistentDataContainer().has(Flasher.flashedKey)) {
            e.setCancelled(true);
        }
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
        agent.getTasks().forEach(BukkitTask::cancel);
        agent.getTasks().clear();
        HandlerList.unregisterAll(agent);
    }
}
