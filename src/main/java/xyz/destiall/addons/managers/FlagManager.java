package xyz.destiall.addons.managers;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import org.bukkit.plugin.Plugin;
import xyz.destiall.addons.Addons;

import java.util.HashSet;

public class FlagManager {
    public static final String NO_MOVE_ITEMS = "no-move-items";
    public static final String REMOVE_PLACE_BLOCKs = "remove-place-blocks";
    public static final String SNOWBALL_BREAK_SNOW = "snowball-break-snow";
    public static final String DISABLE_FALLDMG = "disable-falldmg-over-20";
    public static final String DISABLE_ENTERING_COMBATLOG = "disable-entering-combatlog";
    public static final String DISABLE_CRAFTING = "disable-crafting";

    private static final HashSet<Flag<?>> flags = new HashSet<>();
    private static FlagRegistry registry;

    private FlagManager() {}

    public static <F> F getFlag(String flag, Class<F> clazz) {
        return clazz.cast(flags.stream().filter(f -> f.getName().equalsIgnoreCase(flag)).findFirst().orElse(null));
    }

    public static void init(Plugin plugin) {
        if (plugin instanceof WorldGuardPlugin) {
            Addons.WG = true;
            registry = WorldGuard.getInstance().getFlagRegistry();
            registerFlag(new IntegerFlag(REMOVE_PLACE_BLOCKs, RegionGroup.ALL));
            registerFlag(new StateFlag(NO_MOVE_ITEMS, false));
            registerFlag(new StateFlag(SNOWBALL_BREAK_SNOW, false));
            registerFlag(new StateFlag(DISABLE_FALLDMG, false));
            registerFlag(new StateFlag(DISABLE_CRAFTING, false));
            registerFlag(new StateFlag(DISABLE_ENTERING_COMBATLOG, false));

        }
    }

    private static void registerFlag(Flag<?> flag) {
        if (registry == null) return;
        try {
            registry.register(flag);
            flags.add(flag);
        } catch (FlagConflictException e) {
            Addons.INSTANCE.getLogger().warning("Another plugin is already using " + flag.getName() + " as a flag!");
        }
    }
}
