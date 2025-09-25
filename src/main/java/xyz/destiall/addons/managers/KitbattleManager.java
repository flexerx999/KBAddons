package xyz.destiall.addons.managers;

import me.wazup.kitbattle.Kitbattle;
import me.wazup.kitbattle.abilities.Ability;
import me.wazup.kitbattle.abilities.AbilityManager;
import org.bukkit.configuration.file.FileConfiguration;
import xyz.destiall.addons.abilities.*;

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
        registerAbility(new Rage());
        registerAbility(new Leap());
        registerAbility(new SizeChange());
        registerAbility(new Ender());
        registerAbility(new FireTrail());
        registerAbility(new WeaponForge());
        registerAbility(new TemporalSplit());
        registerAbility(new LaunchFirework());
        registerAbility(new IceTrail());
        registerAbility(new SoundBlast());
        registerAbility(new Randomiser());

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