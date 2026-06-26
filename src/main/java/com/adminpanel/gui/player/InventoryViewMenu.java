package com.adminpanel.gui.player;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.base.SubMenu;
import com.adminpanel.util.ItemBuilder;
import com.adminpanel.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * View and manage a player's inventory and armor.
 * Layout:
 * - Slots 0-8: Target's armor (helmet, chest, legs, boots + filler)
 * - Slots 9-35: Target's inventory (3 rows of 9)
 * - Slots 36-44: Target's hotbar
 * - Slot 45: Back button
 * - Slots 46-53: Filler / your own hand
 */
public class InventoryViewMenu extends SubMenu {

    private final Player target;
    private boolean takeMode = false;

    public InventoryViewMenu(AdminPanel plugin, Player player, Player target) {
        super(plugin, player, "&0&l" + target.getName() + "'s Inventory", 6);
        this.target = target;
    }

    @Override
    protected void buildMenu() {
        ItemStack[] armor = target.getInventory().getArmorContents();

        // Armor row (top)
        setItem(0, ItemBuilder.placeholder());
        setItem(1, armor[3] != null ? armor[3] : new ItemStack(Material.AIR)); // Helmet
        setItem(2, armor[2] != null ? armor[2] : new ItemStack(Material.AIR)); // Chestplate
        setItem(3, armor[1] != null ? armor[1] : new ItemStack(Material.AIR)); // Leggings
        setItem(4, armor[0] != null ? armor[0] : new ItemStack(Material.AIR)); // Boots
        setItem(5, ItemBuilder.placeholder());

        // Offhand
        ItemStack offhand = target.getInventory().getItemInOffHand();
        setItem(6, offhand.getType() != Material.AIR ? offhand : ItemBuilder.placeholder());

        // Ender chest
        setItem(8, new ItemBuilder(Material.ENDER_CHEST)
                .name("&5&lEnder Chest")
                .lore("&7Click to view ender chest")
                .build());

        // Inventory (slots 9-35)
        ItemStack[] contents = target.getInventory().getContents();
        for (int i = 0; i < 27 && i + 9 < 36; i++) {
            setItem(i + 9, contents[i] != null ? contents[i] : ItemBuilder.placeholder());
        }

        // Hotbar (slots 36-44)
        for (int i = 27; i < 36 && i < contents.length; i++) {
            setItem(i + 9, contents[i] != null ? contents[i] : ItemBuilder.placeholder());
        }

        // Control row
        setItem(45, new ItemBuilder(Material.ARROW)
                .name("&c&l← Back")
                .build());

        // Take mode toggle
        setItem(49, new ItemBuilder(takeMode ? Material.LIME_DYE : Material.RED_DYE)
                .name(takeMode ? "&a&lTake Mode: ON" : "&c&lTake Mode: OFF")
                .lore("&7When ON, clicking items",
                      "&7will take them to your inventory")
                .build());

        // Info
        setItem(50, new ItemBuilder(Material.PAPER)
                .name("&e&l" + target.getName())
                .lore("&7Inventory contents shown above",
                      "&7Click items to interact")
                .build());
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null) return;

        // Back button
        if (slot == 45) {
            new PlayerActionMenu(plugin, player, target).open();
            return;
        }

        // Take mode toggle
        if (slot == 49) {
            takeMode = !takeMode;
            refresh();
            return;
        }

        // Ender chest view
        if (slot == 8) {
            player.closeInventory();
            Bukkit.getScheduler().runTask(plugin, () ->
                    player.openInventory(target.getEnderChest()));
            return;
        }

        // If in take mode, take the item from the target's inventory
        if (takeMode && item.getType() != Material.AIR && !isPlaceholder(item)) {
            // Determine which inventory slot this corresponds to
            int targetSlot = slotToInventorySlot(slot);
            if (targetSlot >= 0) {
                ItemStack taken = target.getInventory().getItem(targetSlot);
                if (taken != null) {
                    target.getInventory().setItem(targetSlot, null);
                    player.getInventory().addItem(taken);
                    player.sendMessage(TextUtil.colorize(
                            "&aTook " + taken.getType().name() + " from " + target.getName()));
                    plugin.getAuditManager().log(player, "ITEM_TAKE", target.getName(),
                            "Took " + taken.getType().name());
                    refresh();
                }
            }
        }
    }

    /**
     * Convert GUI slot to player inventory slot.
     */
    private int slotToInventorySlot(int guiSlot) {
        // GUI slots 9-35 → inventory slots 0-26
        if (guiSlot >= 9 && guiSlot <= 35) return guiSlot - 9;
        // GUI slots 36-44 → inventory slots 27-35
        if (guiSlot >= 36 && guiSlot <= 44) return guiSlot - 9;
        // Armor slots
        if (guiSlot == 1) return 39; // Helmet
        if (guiSlot == 2) return 38; // Chestplate
        if (guiSlot == 3) return 37; // Leggings
        if (guiSlot == 4) return 36; // Boots
        if (guiSlot == 6) return 40; // Offhand
        return -1;
    }

    private boolean isPlaceholder(ItemStack item) {
        if (item == null) return true;
        if (item.getType() == Material.BLACK_STAINED_GLASS_PANE) return true;
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return TextUtil.stripColor(item.getItemMeta().getDisplayName()).isEmpty();
        }
        return false;
    }
}
