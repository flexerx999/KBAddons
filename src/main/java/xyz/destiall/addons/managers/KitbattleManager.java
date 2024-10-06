package xyz.destiall.addons.managers;

import me.wazup.kitbattle.Kitbattle;
import me.wazup.kitbattle.abilities.Ability;
import me.wazup.kitbattle.abilities.AbilityManager;
import org.bukkit.configuration.file.FileConfiguration;
import xyz.destiall.addons.abilities.Boost;
import xyz.destiall.addons.abilities.Chemist;
import xyz.destiall.addons.abilities.Egg;
import xyz.destiall.addons.abilities.Freeze;
import xyz.destiall.addons.abilities.Gun;
import xyz.destiall.addons.abilities.Rebound;
import xyz.destiall.addons.abilities.Scorpion;
import xyz.destiall.addons.abilities.Soldier;
import xyz.destiall.addons.abilities.Thrower;
import xyz.destiall.addons.abilities.Vampire;

import java.io.File;

public class KitbattleManager {
    private KitbattleManager() {}

    public static void init() {
        registerAbility(new Chemist());
        registerAbility(new Vampire());
        registerAbility(new Thrower());
        registerAbility(new Freeze());
        registerAbility(new Scorpion());
        registerAbility(new Egg());
        registerAbility(new Rebound());
        registerAbility(new Gun());
        registerAbility(new Soldier());
        registerAbility(new Boost());
        AbilityManager.getInstance().loadAbilitiesConfig();
        AbilityManager.getInstance().updateKitAbilities();
        FileConfiguration config = Kitbattle.getInstance().fileManager.getConfig("abilities.yml");
        try {
            config.save(new File(Kitbattle.getInstance().getDataFolder(), "abilities.yml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void registerAbility(Ability ability) {
        AbilityManager.getInstance().registerAbility(ability);
    }
}
