package xyz.destiall.addons;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import xyz.destiall.addons.commands.AddonCommand;
import xyz.destiall.addons.items.BowRebound;
import xyz.destiall.addons.items.FunFactory;
import xyz.destiall.addons.listeners.BowListener;
import xyz.destiall.addons.listeners.EggListener;
import xyz.destiall.addons.listeners.FlagListener;
import xyz.destiall.addons.listeners.FunListener;
import xyz.destiall.addons.managers.BlockManager;
import xyz.destiall.addons.managers.CombatLogManager;
import xyz.destiall.addons.managers.FlagManager;
import xyz.destiall.addons.managers.HologramManager;
import xyz.destiall.addons.managers.KitbattleManager;
import xyz.destiall.addons.utils.Effects;
import xyz.destiall.addons.utils.Scheduler;
import xyz.destiall.addons.valorant.Agent;
import xyz.destiall.addons.valorant.AgentManager;

public final class Addons extends JavaPlugin {
    public static Addons INSTANCE;
    public static boolean WG;
    public static boolean KB;
    public static boolean SP;
    public static boolean CL;
    public static boolean HL;

    public static Scheduler scheduler;

    private AgentManager agentManager;

    @Override
    public void onLoad() {
        INSTANCE = this;
        PluginManager pm = getServer().getPluginManager();
        Plugin pl = pm.getPlugin("WorldGuard");
        if (pl != null && !pm.isPluginEnabled(pl)) {
            FlagManager.init(pl);
        }
        Effects.setup(this);
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        scheduler = new Scheduler(this);
        PluginManager pm = getServer().getPluginManager();
        KB = pm.getPlugin("KitBattle") != null;
        SP = pm.getPlugin("StrikePractice") != null;
        CL = pm.getPlugin("CombatLogger") != null;
        HL = pm.getPlugin("HolographicDisplays") != null;
        FunFactory.init();
        BlockManager.init();
        agentManager = new AgentManager(this);

        registerEvents(new FunListener(this));

        if (KB) {
            KitbattleManager.init();
            registerEvents(new EggListener());
            registerEvents(new BowListener());
        }
        if (WG) registerEvents(new FlagListener());
        if (CL) CombatLogManager.init();
        if (HL) HologramManager.init();

        getServer().getPluginCommand("addons").setExecutor(new AddonCommand());
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        FileConfiguration config = getConfig();
        if (!KB) {
            BowRebound.AMPLIFIER = (float) config.getDouble("rebound-bow-damage-amplifier", 1.2);
            BowRebound.SPREAD = (float) config.getDouble("rebound-bow-spread", 0);
            BowRebound.BOUNCES = config.getInt("rebound-bow-bounces", 2);
        }
    }

    @Override
    public void onDisable() {
        BlockManager.disable();
        HandlerList.unregisterAll(this);
        scheduler.cancelTasks();
        agentManager.getAgentMap().values().forEach(Agent::unset);
        Messenger messenger = getServer().getMessenger();
        messenger.unregisterIncomingPluginChannel(this);
        messenger.unregisterOutgoingPluginChannel(this);
    }

    public AgentManager getAgentManager() {
        return agentManager;
    }

    private void registerEvents(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, this);
    }

    public static float lerp(float v0, float v1, float t) {
        return (1 - t) * v0 + t * v1;
    }
}
