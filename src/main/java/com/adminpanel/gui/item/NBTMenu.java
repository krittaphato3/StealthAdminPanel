package com.adminpanel.gui.item;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.base.PaginationGUI;
import com.adminpanel.hooks.AnvilGUIBridge;
import com.adminpanel.util.ItemBuilder;
import com.adminpanel.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * PersistentDataContainer editor — view and edit custom tags on an item.
 * Uses the built-in Bukkit PersistentDataContainer API (no external dependency).
 */
public class NBTMenu extends PaginationGUI {

    private final ItemStack editingItem;

    public NBTMenu(AdminPanel plugin, Player player, ItemStack item) {
        super(plugin, player, "&0&lData Tag Editor");
        this.editingItem = item;
    }

    @Override
    protected List<ItemStack> getPageItems() {
        List<ItemStack> items = new ArrayList<>();

        // Add new tag button
        items.add(new ItemBuilder(Material.LIME_DYE)
                .name("&a&l+ Add New Tag")
                .lore("&7Add a custom data tag to the item")
                .build());

        // View all tags
        items.add(new ItemBuilder(Material.BOOKSHELF)
                .name("&9&lView All Tags")
                .lore("&7Display all custom data tags")
                .build());

        try {
            ItemMeta meta = editingItem.getItemMeta();
            if (meta != null) {
                PersistentDataContainer container = meta.getPersistentDataContainer();
                Set<NamespacedKey> keys = container.getKeys();

                for (NamespacedKey key : keys) {
                    // Show the key and its type
                    String keyStr = key.toString();
                    String type = "unknown";

                    if (container.has(key, PersistentDataType.STRING)) {
                        type = "string: " + truncate(container.get(key, PersistentDataType.STRING), 30);
                    } else if (container.has(key, PersistentDataType.INTEGER)) {
                        type = "int: " + container.get(key, PersistentDataType.INTEGER);
                    } else if (container.has(key, PersistentDataType.DOUBLE)) {
                        type = "double: " + container.get(key, PersistentDataType.DOUBLE);
                    } else if (container.has(key, PersistentDataType.BOOLEAN)) {
                        type = "boolean: " + container.get(key, PersistentDataType.BOOLEAN);
                    } else if (container.has(key, PersistentDataType.LONG)) {
                        type = "long: " + container.get(key, PersistentDataType.LONG);
                    }

                    items.add(new ItemBuilder(Material.PAPER)
                            .name("&e" + keyStr)
                            .lore(
                                    "&7Type: &f" + type,
                                    "",
                                    "&c&lClick to remove",
                                    "&a&lShift-click to edit")
                            .build());
                }
            }
        } catch (Exception e) {
            items.add(new ItemBuilder(Material.BARRIER)
                    .name("&c&lError reading tags")
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
            new AnvilGUIBridge(plugin).openTextInput(player, "Tag Key (e.g. myplugin:mykey)", "adminpanel:", (keyStr) -> {
                org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                    new AnvilGUIBridge(plugin).openTextInput(player, "Tag Value", "", (value) -> {
                        org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                            try {
                                ItemMeta meta = editingItem.getItemMeta();
                                if (meta != null) {
                                    NamespacedKey key = new NamespacedKey(plugin, keyStr.replace("adminpanel:", ""));
                                    meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, value);
                                    editingItem.setItemMeta(meta);
                                    player.sendMessage(TextUtil.colorize(
                                            "&aAdded tag: &f" + keyStr + " &a= &f" + value));
                                    plugin.getAuditManager().log(player, "ITEM_TAG_ADD",
                                            editingItem.getType().name(), keyStr + "=" + value);
                                }
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
            // View all tags
            player.closeInventory();
            ItemMeta meta = editingItem.getItemMeta();
            if (meta != null) {
                PersistentDataContainer container = meta.getPersistentDataContainer();
                Set<NamespacedKey> keys = container.getKeys();
                player.sendMessage(TextUtil.colorize("&6&lCustom Data Tags:"));
                if (keys.isEmpty()) {
                    player.sendMessage(TextUtil.colorize("&7No custom tags."));
                }
                for (NamespacedKey key : keys) {
                    String value = "complex";
                    if (container.has(key, PersistentDataType.STRING)) {
                        value = container.get(key, PersistentDataType.STRING);
                    } else if (container.has(key, PersistentDataType.INTEGER)) {
                        value = String.valueOf(container.get(key, PersistentDataType.INTEGER));
                    } else if (container.has(key, PersistentDataType.DOUBLE)) {
                        value = String.valueOf(container.get(key, PersistentDataType.DOUBLE));
                    } else if (container.has(key, PersistentDataType.BOOLEAN)) {
                        value = String.valueOf(container.get(key, PersistentDataType.BOOLEAN));
                    }
                    player.sendMessage(TextUtil.colorize("&7• &e" + key + " &7= &f" + value));
                }
            }
            return;
        }

        // Handle tag actions
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String keyStr = TextUtil.stripColor(item.getItemMeta().getDisplayName());

            if (player.isSneaking()) {
                // Edit value
                player.closeInventory();
                new AnvilGUIBridge(plugin).openTextInput(player, "New value for " + keyStr, "", (newValue) -> {
                    org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                        try {
                            ItemMeta meta = editingItem.getItemMeta();
                            if (meta != null) {
                                NamespacedKey key = new NamespacedKey(plugin, keyStr.replace("adminpanel:", ""));
                                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, newValue);
                                editingItem.setItemMeta(meta);
                                player.sendMessage(TextUtil.colorize(
                                        "&aUpdated &f" + keyStr + " &ato &f" + newValue));
                            }
                        } catch (Exception e) {
                            player.sendMessage(TextUtil.colorize("&cError: " + e.getMessage()));
                        }
                        refresh();
                    });
                });
            } else {
                // Remove tag
                try {
                    ItemMeta meta = editingItem.getItemMeta();
                    if (meta != null) {
                        NamespacedKey key = new NamespacedKey(plugin, keyStr.replace("adminpanel:", ""));
                        meta.getPersistentDataContainer().remove(key);
                        editingItem.setItemMeta(meta);
                        player.sendMessage(TextUtil.colorize("&cRemoved tag: &f" + keyStr));
                        plugin.getAuditManager().log(player, "ITEM_TAG_REMOVE",
                                editingItem.getType().name(), keyStr);
                    }
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
    public void onBackClick() {
        new ItemEditorMenu(plugin, player).open();
    }

    @Override
    public String getMenuTitle() {
        return "&0&lData Tag Editor";
    }
}
