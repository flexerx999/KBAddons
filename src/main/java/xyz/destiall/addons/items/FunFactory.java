package xyz.destiall.addons.items;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import xyz.destiall.addons.Addons;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class FunFactory implements Listener {
    private static final HashMap<String, ItemStack> items = new HashMap<>();
    private static final HashMap<UUID, InventoryMenu> viewers = new HashMap<>();

    public static final NamespacedKey itemKey = new NamespacedKey(Addons.INSTANCE, "item-type");

    public static void init() {
        items.clear();
        viewers.clear();
        newItem("BRIDGE", Material.GLASS, "&dBridge Block", "&fDisappears after " + Addons.INSTANCE.getConfig().getInt("bridge-block-expiry-ms", 5000) + "ms");
        newItem("QUAKE", Material.IRON_HOE, "&rQuake Gun", "&fRight click to shoot");
        newItem("OP_QUAKE", Material.DIAMOND_HOE, "&bOP Quake Gun");
        newItem("SWORD", Material.IRON_SWORD, true, "&fThrowing Sword");
        newItem("JETTBLADES", Material.FEATHER, "&fBladestorm", "&fRight click to activate");
        newItem("REBOUND", Material.BOW, "&eRebound Bow");
        newItem("NEONWALL", Material.IRON_BARS, "&9Neon Wall", "&fRight click to activate");
        newItem("PHOENIXWALL", Material.MAGMA_BLOCK, "&6Phoenix Wall", "&fRight click to activate");
        newItem("PHOENIXFLASH", Material.MAGMA_CREAM, "&6Phoenix Flash", "&fLeft/Right click to throw");
        newItem("YORUFLASH", Material.HEART_OF_THE_SEA, "&bYoru Flash", "&fLeft click to throw");
        newItem("SOVASCAN", Material.BOW, "&3Sova Bow (0)", "&fLeft click to toggle bounces");
        newItem("RECONDART", Material.ARROW, "&3Recon Dart", "Use this arrow to scan surroundings");
        // newItem("GRAPPLE", Material.FISHING_ROD, "&7&lGrappling Hook");
        Bukkit.getPluginManager().registerEvents(new FunFactory(), Addons.INSTANCE);
    }

    public static ItemStack createItem(String name) {
        ItemStack item = items.get(name);
        return item.clone();
    }

    public static boolean isItem(ItemStack stack, String name) {
        ItemStack item = items.get(name);
        if (item == null)
            return false;

        ItemMeta meta = stack.getItemMeta();
        if (meta == null)
            return false;

        PersistentDataContainer cont = meta.getPersistentDataContainer();
        if (cont.has(itemKey))
           return name.equalsIgnoreCase(cont.get(itemKey, PersistentDataType.STRING));

        return item.isSimilar(stack);
    }

    public static void openGUI(Player player) {
        if (viewers.containsKey(player.getUniqueId()))
            return;

        Inventory gui = Bukkit.createInventory(null, 9 * 3, "Addons GUI");
        for (String name : items.keySet()) {
            gui.addItem(createItem(name));
        }
        viewers.put(player.getUniqueId(), new InventoryMenu(player.openInventory(gui), player));
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        viewers.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        viewers.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        InventoryMenu view = viewers.get(e.getWhoClicked().getUniqueId());
        if (view == null)
            return;

        if (e.getView() == view.getInventory()) {
            if (e.getClickedInventory() != e.getWhoClicked().getInventory()) {
                e.setCancelled(true);
                ItemStack item = Objects.requireNonNull(e.getClickedInventory()).getItem(e.getSlot());
                if (item != null) {
                    e.getWhoClicked().getInventory().addItem(item.clone());
                }
            }
        }
    }

    public static String color(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    private static void newItem(String key, Material material, boolean unbreakable, String display, String... lore) {
        newItem(key, material, 1, unbreakable, display, lore);
    }

    private static void newItem(String key, Material material, String display, String... lore) {
        newItem(key, material, 1, false, display, lore);
    }

    private static void newItem(String key, Material material, int amount, boolean unbreakable, String display, String... lore) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (display != null)
            meta.setDisplayName(color(display));

        if (lore != null)
            meta.setLore(Arrays.stream(lore).map(FunFactory::color).collect(Collectors.toList()));

        if (unbreakable)
            meta.setUnbreakable(true);

        meta.getPersistentDataContainer().set(itemKey, PersistentDataType.STRING, key);
        item.setItemMeta(meta);
        items.put(key, item);
    }
}
