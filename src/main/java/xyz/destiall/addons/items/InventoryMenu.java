package xyz.destiall.addons.items;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.InventoryView;

public class InventoryMenu implements Listener {
    private final InventoryView inventory;
    private final Player player;

    public InventoryMenu(InventoryView inventory, Player player) {
        this.inventory = inventory;
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public InventoryView getInventory() {
        return inventory;
    }
}
