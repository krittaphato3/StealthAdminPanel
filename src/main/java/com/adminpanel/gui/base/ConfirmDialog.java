package com.adminpanel.gui.base;

import com.adminpanel.AdminPanel;
import com.adminpanel.util.ItemBuilder;
import com.adminpanel.util.SoundUtil;
import com.adminpanel.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 * Reusable confirmation dialog for destructive actions.
 *
 * Layout (single row, 9 slots):
 * - Slot 0-2: Filler
 * - Slot 3: Confirm button (green)
 * - Slot 4: Info display (shows what will happen)
 * - Slot 5: Cancel button (red)
 * - Slot 6-8: Filler
 *
 * Usage:
 *   new ConfirmDialog(plugin, player, "Ban Player", "Ban Steve for hacking?", () -> {
 *       // confirmed action
 *   }, () -> {
 *       // cancelled action
 *   }).open();
 */
public class ConfirmDialog implements InventoryHolder {

    private final AdminPanel plugin;
    private final Player player;
    private final Inventory inventory;
    private final Runnable onConfirm;
    private final Runnable onCancel;
    private final String title;

    public ConfirmDialog(AdminPanel plugin, Player player, String title, String description,
                         Runnable onConfirm, Runnable onCancel) {
        this.plugin = plugin;
        this.player = player;
        this.title = title;
        this.onConfirm = onConfirm;
        this.onCancel = onCancel;
        this.inventory = Bukkit.createInventory(this, 27, TextUtil.colorize("&0&l" + title));

        build(description);
    }

    private void build(String description) {
        // Filler slots
        ItemStack filler = ItemBuilder.placeholder();
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, filler);
        }

        // Slot 4: Description
        inventory.setItem(4, new ItemBuilder(Material.PAPER)
                .name("&e&l" + title)
                .lore(description.split("\n"))
                .build());

        // Slot 3: Confirm (green)
        inventory.setItem(3, new ItemBuilder(Material.LIME_WOOL)
                .name("&a&l✔ Confirm")
                .lore("&7Click to confirm this action")
                .build());

        // Slot 5: Cancel (red)
        inventory.setItem(5, new ItemBuilder(Material.RED_WOOL)
                .name("&c&l✘ Cancel")
                .lore("&7Click to cancel")
                .build());
    }

    public void open() {
        SoundUtil.playSearch(player);
        player.openInventory(inventory);
    }

    /**
     * Handle a click in this dialog. Called by GUIClickListener.
     */
    public void handleClick(int slot) {
        if (slot == 3) {
            // Confirm
            SoundUtil.playSuccess(player);
            player.closeInventory();
            Bukkit.getScheduler().runTask(plugin, onConfirm);
        } else if (slot == 5) {
            // Cancel
            SoundUtil.playError(player);
            player.closeInventory();
            if (onCancel != null) {
                Bukkit.getScheduler().runTask(plugin, onCancel);
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
