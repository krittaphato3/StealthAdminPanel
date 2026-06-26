package com.adminpanel.gui.item;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.base.SubMenu;
import com.adminpanel.hooks.AnvilGUIBridge;
import com.adminpanel.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Display editor — edit item name, lore, custom model data, item flags.
 */
public class DisplayMenu extends SubMenu {

    private final ItemStack editingItem;

    public DisplayMenu(AdminPanel plugin, Player player, ItemStack item) {
        super(plugin, player, "&0&lDisplay Editor", 4);
        this.editingItem = item;
    }

    @Override
    protected void buildMenu() {
        ItemMeta meta = editingItem.getItemMeta();

        // Name
        String currentName = meta != null && meta.hasDisplayName() ?
                TextUtil.stripColor(meta.getDisplayName()) : "None";
        setItem(10, Material.NAME_TAG,
                "&6&lEdit Name",
                "&7Current: &f" + currentName,
                "&7Supports &-color codes & &#RRGGBB");

        // Lore
        setItem(12, Material.BOOK,
                "&9&lEdit Lore",
                "&7Current lines: &f" + (meta != null && meta.hasLore() ? meta.getLore().size() : 0),
                "&7Click to edit lore line by line");

        // Custom Model Data
        int cmd = meta != null && meta.hasCustomModelData() ? meta.getCustomModelData() : 0;
        setItem(14, Material.PAPER,
                "&e&lCustom Model Data",
                "&7Current: &f" + cmd,
                "&7Click to set");

        // Unbreakable toggle
        boolean unbreakable = meta != null && meta.isUnbreakable();
        setItem(19, Material.BEDROCK,
                unbreakable ? "&a&lUnbreakable: ON" : "&c&lUnbreakable: OFF",
                "&7Click to toggle");

        // Item Flags display
        setItem(20, Material.BARRIER,
                "&c&lHidden Flags",
                "&7Currently hiding: &f" + getHiddenFlags());

        // Reset all flags
        setItem(21, Material.BARRIER,
                "&c&lReset All Flags",
                "&7Remove all item flags",
                "&7Show all item info");

        addBackButton();
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null || item.getType() == Material.AIR) return;

        ItemMeta meta = editingItem.getItemMeta();

        switch (slot) {
            case 10 -> {
                // Edit name
                player.closeInventory();
                new AnvilGUIBridge(plugin).openTextInput(player, "Enter item name", "", (text) -> {
                    org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                        ItemMeta m = editingItem.getItemMeta();
                        if (m != null) {
                            m.setDisplayName(TextUtil.colorize(text));
                            editingItem.setItemMeta(m);
                            player.sendMessage(TextUtil.colorize("&aItem name set to: &f" + text));
                        }
                        refresh();
                    });
                });
            }
            case 12 -> {
                // Edit lore — simple: enter full lore separated by |
                player.closeInventory();
                new AnvilGUIBridge(plugin).openTextInput(player, "Lore (separate lines with |)", "", (text) -> {
                    org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                        ItemMeta m = editingItem.getItemMeta();
                        if (m != null) {
                            List<String> lore = new ArrayList<>();
                            for (String line : text.split("\\|")) {
                                lore.add(TextUtil.colorize(line.trim()));
                            }
                            m.setLore(lore);
                            editingItem.setItemMeta(m);
                            player.sendMessage(TextUtil.colorize("&aLore updated! (" + lore.size() + " lines)"));
                        }
                        refresh();
                    });
                });
            }
            case 14 -> {
                // Custom Model Data
                player.closeInventory();
                new AnvilGUIBridge(plugin).openNumberInput(player, "Custom Model Data", "0", value -> {
                    org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                        ItemMeta m = editingItem.getItemMeta();
                        if (m != null) {
                            m.setCustomModelData(value);
                            editingItem.setItemMeta(m);
                            player.sendMessage(TextUtil.colorize("&aCustom Model Data set to &e" + value));
                        }
                        refresh();
                    });
                });
            }
            case 19 -> {
                // Toggle unbreakable
                if (meta != null) {
                    meta.setUnbreakable(!meta.isUnbreakable());
                    if (meta.isUnbreakable()) {
                        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                    } else {
                        meta.removeItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                    }
                    editingItem.setItemMeta(meta);
                    player.sendMessage(TextUtil.colorize("&aUnbreakable: " + meta.isUnbreakable()));
                }
                refresh();
            }
            case 21 -> {
                // Reset all flags
                if (meta != null) {
                    meta.removeItemFlags(ItemFlag.values());
                    editingItem.setItemMeta(meta);
                    player.sendMessage(TextUtil.colorize("&aAll item flags removed"));
                }
                refresh();
            }
        }
    }

    @Override
    protected void onBackClick() {
        new ItemEditorMenu(plugin, player).open();
    }

    private String getHiddenFlags() {
        ItemMeta meta = editingItem.getItemMeta();
        if (meta == null) return "None";
        var flags = meta.getItemFlags();
        if (flags.isEmpty()) return "Nothing";
        StringBuilder sb = new StringBuilder();
        for (ItemFlag flag : flags) {
            sb.append(flag.name()).append(", ");
        }
        return sb.substring(0, sb.length() - 2);
    }
}
