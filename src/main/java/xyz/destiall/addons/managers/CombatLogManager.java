package xyz.destiall.addons.managers;

import com.keurig.combatlogger.CombatLogger;
import org.bukkit.entity.Player;

public class CombatLogManager {
    private static CombatLogger cl;
    private CombatLogManager() {}

    public static void init() {
        cl = CombatLogger.getInstance();
    }

    public static boolean isCombatLogged(Player player) {
        return cl != null && cl.getCombatPlayer().getCombatLogged().containsKey(player.getUniqueId());
    }
}
