package com.adminpanel.gui.item;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.base.PaginationGUI;
import com.adminpanel.hooks.AnvilGUIBridge;
import com.adminpanel.util.ItemBuilder;
import com.adminpanel.util.TextUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Advanced NBT editor — view and edit raw NBT tags on an item.
 * Uses Item-NBT-API for full NBT access.
 */
public class NBTMenu extends PaginationGUI {

    private final ItemStack editingItem;

    public NBTMenu(AdminPanel plugin, Player player, ItemStack item) {
        super(plugin, player, "&0&lNBT Editor");
        this.editingItem = item;
    }

    @Override
    protected List<ItemStack> getPageItems() {
        List<ItemStack> items = new ArrayList<>();

        // Add new tag button
        items.add(new ItemBuilder(Material.LIME_DYE)
                .name("&a&l+ Add New Tag")
                .lore("&7Add a string tag to the item")
                .build());

        // View raw NBT button
        items.add(new ItemBuilder(Material.BOOKSHELF)
                .name("&9&lView Raw NBT")
                .lore("&7Display all NBT tags")
                .build());

        try {
            NBTItem nbtItem = new NBTItem(editingItem);
            Set<String> keys = nbtItem.getKeys();

            for (String key : keys) {
                // Skip internal/boring keys
                if (key.equals("PublicBukkitValues") || key.startsWith("minecraft:")) continue;

                String value;
                try {
                    value = nbtItem.getString(key);
                    if (value == null || value.isEmpty()) {
                        value = String.valueOf(nbtItem.getType(key));
                    }
                } catch (Exception e) {
                    value = "complex";
                }

                items.add(new ItemBuilder(Material.PAPER)
                        .name("&e" + key)
                        .lore(
                                "&7Value: &f" + truncate(value, 40),
                                "",
                                "&c&lClick to remove",
                                "&a&lShift-click to edit")
                        .build());
            }
        } catch (Exception e) {
            items.add(new ItemBuilder(Material.BARRIER)
                    .name("&c&lError reading NBT")
                    .lore("&7" + e.getMessage())
                    .build());
        }

        return items;
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null || item.getType() == Material.AIR) return;

        if (item.getType() == Material.LIME_DYE) {
            // Add new tag
            player.closeInventory();
            new AnvilGUIBridge(plugin).openTextInput(player, "Tag Name", "", (tagName, event) -> {
                org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                    new AnvilGUIBridge(plugin).openTextInput(player, "Tag Value", "", (tagValue, event2) -> {
                        org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                            try {
                                NBTItem nbtItem = new NBTItem(editingItem);
                                nbtItem.setString(tagName, tagValue);
                                ItemStack result = nbtItem.getItem();
                                editingItem.setItemMeta(result.getItemMeta());
                                // Copy the NBT data
                                org.bukkit.inventory.meta.ItemMeta meta = editingItem.getItemMeta();
                                if (meta != null) {
                                    editingItem.setItemMeta(result.getItemMeta());
                                }
                                player.sendMessage(TextUtil.colorize(
                                        "&aAdded tag: &f" + tagName + " &a= &f" + tagValue));
                                plugin.getAuditManager().log(player, "ITEM_NBT_ADD",
                                        editingItem.getType().name(), tagName + "=" + tagValue);
                            } catch (Exception e) {
                                player.sendMessage(TextUtil.colorize("&cError: " + e.getMessage()));
                            }
                            refresh();
                        });
                    });
                });
            });
            return;
        }

        if (item.getType() == Material.BOOKSHELF) {
            // View raw NBT
            player.closeInventory();
            try {
                NBTItem nbtItem = new NBTItem(editingItem);
                String nbt = nbtItem.toString();
                // Send NBT in chat (split into lines if too long)
                player.sendMessage(TextUtil.colorize("&6&lRaw NBT Data:"));
                for (int i = 0; i < nbt.length(); i += 60) {
                    int end = Math.min(i + 60, nbt.length());
                    player.sendMessage(TextUtil.colorize("&7" + nbt.substring(i, end)));
                }
            } catch (Exception e) {
                player.sendMessage(TextUtil.colorize("&cError reading NBT: " + e.getMessage()));
            }
            return;
        }

        // Handle tag actions
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String tagName = TextUtil.stripColor(item.getItemMeta().getDisplayName());

            if (player.isSneaking()) {
                // Edit value
                player.closeInventory();
                new AnvilGUIBridge(plugin).openTextInput(player, "New value for " + tagName, "", (newValue, event) -> {
                    org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                        try {
                            NBTItem nbtItem = new NBTItem(editingItem);
                            nbtItem.setString(tagName, newValue);
                            editingItem.setItemMeta(nbtItem.getItem().getItemMeta());
                            player.sendMessage(TextUtil.colorize(
                                    "&aUpdated &f" + tagName + " &ato &f" + newValue));
                        } catch (Exception e) {
                            player.sendMessage(TextUtil.colorize("&cError: " + e.getMessage()));
                        }
                        refresh();
                    });
                });
            } else {
                // Remove tag
                try {
                    NBTItem nbtItem = new NBTItem(editingItem);
                    nbtItem.removeKey(tagName);
                    editingItem.setItemMeta(nbtItem.getItem().getItemMeta());
                    player.sendMessage(TextUtil.colorize("&cRemoved tag: &f" + tagName));
                    plugin.getAuditManager().log(player, "ITEM_NBT_REMOVE",
                            editingItem.getType().name(), tagName);
                } catch (Exception e) {
                    player.sendMessage(TextUtil.colorize("&cError: " + e.getMessage()));
                }
                refresh();
            }
        }
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "null";
        return s.length() > maxLen ? s.substring(0, maxLen) + "..." : s;
    }

    @Override
    public String getMenuTitle() {
        return "&0&lNBT Editor";
    }
}
