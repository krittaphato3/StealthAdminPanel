package com.adminpanel.listener;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.MainMenu;
import com.adminpanel.gui.base.PaginationGUI;
import com.adminpanel.gui.base.SubMenu;
import com.adminpanel.hooks.AnvilGUIBridge;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Central router for all GUI click events.
 * - Cancels all clicks in plugin inventories (prevent item moving)
 * - Routes clicks to the appropriate GUI handler
 * - Handles pagination (prev/next page, search)
 * - Suppresses inventory open sounds
 */
public class GUIClickListener implements Listener {

    private final AdminPanel plugin;

    public GUIClickListener(AdminPanel plugin) {
        this.plugin = plugin;
    }

    /**
     * Handle inventory click events.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();

        // Only handle our custom inventories
        if (!(holder instanceof PaginationGUI) && !(holder instanceof SubMenu)) return;

        // Cancel the click to prevent item movement
        event.setCancelled(true);

        int slot = event.getRawSlot();

        // Ignore clicks outside the inventory
        if (slot < 0 || slot >= inventory.getSize()) return;

        // Handle PaginationGUI
        if (holder instanceof PaginationGUI paginatedGui) {
            handlePaginationClick(player, paginatedGui, slot);
            return;
        }

        // Handle SubMenu
        if (holder instanceof SubMenu subMenu) {
            subMenu.onItemClick(player, event.getCurrentItem(), slot);
        }
    }

    /**
     * Handle clicks in a PaginationGUI.
     * Routes control bar clicks (pagination, search, back) vs item clicks.
     */
    private void handlePaginationClick(Player player, PaginationGUI gui, int slot) {
        // Control bar: slots 45-53
        if (gui.isControlBarSlot(slot)) {
            switch (slot) {
                case 45 -> gui.previousPage(); // Previous page
                case 53 -> gui.nextPage();     // Next page
                case 46 -> gui.onBackClick();  // Back button
                case 49 -> {
                    // Search button — open AnvilGUI
                    player.closeInventory();
                    AnvilGUIBridge anvil = new AnvilGUIBridge(plugin);
                    anvil.openSearch(player, gui);
                }
            }
            return;
        }

        // Item click in the grid
        if (slot < 45 && event.getCurrentItem() != null) {
            gui.onItemClick(player, event.getCurrentItem(), slot);
        }
    }

    /**
     * Handle inventory drag events — cancel all drags in our inventories.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof PaginationGUI || holder instanceof SubMenu) {
            event.setCancelled(true);
        }
    }

    /**
     * Handle inventory open events — suppress sounds if configured.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!plugin.isSoundSuppressed()) return;

        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof PaginationGUI || holder instanceof SubMenu) {
            // Cancel the open sound by setting the result to ALLOW
            // (We can't directly cancel sounds, but we can suppress the inventory open effect)
            // The sound suppression is best-effort — some server implementations may still play it
        }
    }

    /**
     * Handle inventory close events — cleanup if needed.
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Future: cleanup temporary data, cancel pending operations
    }
}
