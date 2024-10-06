package xyz.destiall.addons.valorant;

import com.Zrips.CMI.events.CMIArmorChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import xyz.destiall.addons.Addons;
import xyz.destiall.addons.valorant.common.Flasher;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AgentManager implements Listener {
    private final Map<UUID, Agent> agentMap;
    private final Addons plugin;

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

    public void unsetAgent(Player player) {
        Agent agent = agentMap.remove(player.getUniqueId());
        if (agent == null)
            return;

        HandlerList.unregisterAll(agent);
    }
}
