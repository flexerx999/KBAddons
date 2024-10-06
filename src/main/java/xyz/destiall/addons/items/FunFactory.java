package xyz.destiall.addons.items;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.destiall.addons.Addons;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

public class FunFactory implements Listener {
    private static final HashMap<String, ItemStack> items = new HashMap<>();
    private static final HashMap<UUID, InventoryMenu> viewers = new HashMap<>();
    public static void init() {
        items.clear();
        viewers.clear();
        newItem("BRIDGE", Material.GLASS, "&d&lBridge Block");
        newItem("QUAKE", Material.IRON_HOE, "&r&lQuake Gun");
        newItem("OP_QUAKE", Material.DIAMOND_HOE, "&r&lOP Quake Gun");
        newItem("SWORD", Material.IRON_SWORD, true, "&f&lThrowing Sword");
        newItem("JETT", Material.FEATHER, "&fBladestorm", "&fRight click to activate");
        newItem("REBOUND", Material.BOW, "&e&lRebound Bow");
        newItem("NEONWALL", Material.IRON_BARS, "&9&lNeon Wall");
        newItem("PHOENIXWALL", Material.MAGMA_BLOCK, "&6&lPhoenix Wall");
        newItem("PHOENIXFLASH", Material.MAGMA_CREAM, "&6&lPhoenix Flash");
        // newItem("GRAPPLE", Material.FISHING_ROD, "&7&lGrappling Hook");
        Bukkit.getPluginManager().registerEvents(new FunFactory(), Addons.INSTANCE);
    }

    public static ItemStack createItem(String name) {
        ItemStack item = items.get(name);
        if (item == null) return null;
        return item.clone();
    }

    public static boolean isItem(ItemStack stack, String name) {
        ItemStack item = items.get(name);
        if (item == null) return false;
        return item.isSimilar(stack);
    }

    public static void openGUI(Player player) {
        if (viewers.containsKey(player.getUniqueId())) return;
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
        if (view == null) return;
        if (e.getView() == view.getInventory()) {
            if (e.getClickedInventory() != e.getWhoClicked().getInventory()) {
                e.setCancelled(true);
                ItemStack item = e.getClickedInventory().getItem(e.getSlot());
                if (item != null) {
                    e.getWhoClicked().getInventory().addItem(item.clone());
                }
            }
        }
    }

    private static String color(String s) {
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
        if (display != null) meta.setDisplayName(color(display));
        if (lore != null) meta.setLore(Arrays.stream(lore).map(FunFactory::color).collect(Collectors.toList()));
        if (unbreakable) meta.setUnbreakable(true);
        item.setItemMeta(meta);
        items.put(key, item);
    }
}
