package xyz.destiall.addons.abilities;

import me.wazup.kitbattle.PlayerData;
import me.wazup.kitbattle.abilities.Ability;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.destiall.addons.Addons;
import xyz.destiall.addons.utils.Scheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WeaponForge extends Ability {
    private int cooldown;
    private int duration;
    private final Material activationMaterial = Material.IRON_SWORD;

    // Track enhanced weapons: Player UUID -> (ItemStack, Original Sharpness Level)
    private static final Map<UUID, Map.Entry<ItemStack, Integer>> enhancedWeapons = new HashMap<>();

    @Override
    public String getName() {
        return "WeaponForge";
    }

    @Override
    public void load(FileConfiguration file) {
        if (!file.contains("Abilities.WeaponForge.Cooldown")) {
            file.set("Abilities.WeaponForge.Cooldown", 20);
        }
        cooldown = file.getInt("Abilities.WeaponForge.Cooldown");

        if (!file.contains("Abilities.WeaponForge.Duration")) {
            file.set("Abilities.WeaponForge.Duration", 10);
        }
        duration = file.getInt("Abilities.WeaponForge.Duration");
    }

    @Override
    public Material getActivationMaterial() {
        return activationMaterial;
    }

    @Override
    public EntityType getActivationProjectile() {
        return null;
    }

    @Override
    public boolean isAttackActivated() {
        return false;
    }

    @Override
    public boolean isAttackReceiveActivated() {
        return false;
    }

    @Override
    public boolean isDamageActivated() {
        return false;
    }

    @Override
    public boolean isEntityInteractionActivated() {
        return true; // Enable right-click detection
    }

    @Override
    public boolean execute(Player player, PlayerData data, Event event) {
        if (data.hasCooldown(player, "WeaponForge")) {
            return false;
        }

        // Check if player already has enhanced weapon
        if (enhancedWeapons.containsKey(player.getUniqueId())) {
            return false;
        }

        ItemStack heldItem = player.getInventory().getItemInMainHand();

        // Check if held item is a sword or axe
        if (!isValidWeapon(heldItem.getType())) {
            return false;
        }

        data.setCooldown(player, "WeaponForge", cooldown, true);

        // Get current sharpness level
        ItemMeta meta = heldItem.getItemMeta();
        int currentSharpness = meta.hasEnchant(Enchantment.SHARPNESS) ? meta.getEnchantLevel(Enchantment.SHARPNESS) : 0;
        int newSharpness = currentSharpness + 1;

        // Store original sharpness level
        enhancedWeapons.put(player.getUniqueId(),
                new HashMap.SimpleEntry<>(heldItem.clone(), currentSharpness));

        // Apply enhanced sharpness
        meta.addEnchant(Enchantment.SHARPNESS, newSharpness, true);
        heldItem.setItemMeta(meta);

        // Update player's inventory
        player.getInventory().setItemInMainHand(heldItem);

        // Schedule removal of enhancement
        new Scheduler.TaskRunnable() {
            @Override
            public void run() {
                removeEnhancement(player);
            }
        }.runTaskLater(Addons.scheduler, duration * 20L);

        return true;
    }

    private boolean isValidWeapon(Material material) {
        switch (material) {
            case WOODEN_SWORD:
            case STONE_SWORD:
            case IRON_SWORD:
            case GOLDEN_SWORD:
            case DIAMOND_SWORD:
            case NETHERITE_SWORD:
            case WOODEN_AXE:
            case STONE_AXE:
            case IRON_AXE:
            case GOLDEN_AXE:
            case DIAMOND_AXE:
            case NETHERITE_AXE:
                return true;
            default:
                return false;
        }
    }

    public static void removeEnhancement(Player player) {
        Map.Entry<ItemStack, Integer> weaponData = enhancedWeapons.remove(player.getUniqueId());

        if (weaponData == null) {
            return;
        }

        ItemStack originalWeapon = weaponData.getKey();
        int originalSharpness = weaponData.getValue();
        ItemStack currentWeapon = player.getInventory().getItemInMainHand();

        // Check if player still has the same weapon type
        if (currentWeapon.getType() == originalWeapon.getType()) {
            ItemMeta meta = currentWeapon.getItemMeta();

            if (originalSharpness == 0) {
                // Remove sharpness entirely if weapon originally had none
                meta.removeEnchant(Enchantment.SHARPNESS);
            } else {
                // Restore original sharpness level
                meta.addEnchant(Enchantment.SHARPNESS, originalSharpness, true);
            }

            currentWeapon.setItemMeta(meta);
            player.getInventory().setItemInMainHand(currentWeapon);
        }
    }

    // Clean up enhancements when players leave
    public static void cleanupPlayer(UUID playerUUID) {
        enhancedWeapons.remove(playerUUID);
    }
}