package com.adminpanel.gui.item;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.base.PaginationGUI;
import com.adminpanel.hooks.AnvilGUIBridge;
import com.adminpanel.util.ItemBuilder;
import com.adminpanel.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Enchantment editor — add any enchantment at unlimited levels.
 * Uses Item-NBT-API for levels beyond vanilla limits (e.g., Sharpness 1000000).
 */
public class EnchantMenu extends PaginationGUI {

    private final ItemStack editingItem;
    private boolean showCurrentEnchants = false;

    public EnchantMenu(AdminPanel plugin, Player player, ItemStack item) {
        super(plugin, player, "&0&lEnchantment Editor");
        this.editingItem = item;
    }

    @Override
    protected List<ItemStack> getPageItems() {
        List<ItemStack> items = new ArrayList<>();

        if (showCurrentEnchants) {
            // Show current enchantments on the item
            Map<org.bukkit.enchantments.Enchantment, Integer> enchants = editingItem.getEnchantments();
            for (var entry : enchants.entrySet()) {
                items.add(new ItemBuilder(Material.ENCHANTED_BOOK)
                        .name("&b" + entry.getKey().getName())
                        .lore(
                                "&7Level: &e" + entry.getValue(),
                                "",
                                "&c&lClick to remove")
                        .build());
            }
            if (enchants.isEmpty()) {
                items.add(new ItemBuilder(Material.BARRIER)
                        .name("&c&lNo enchantments")
                        .lore("&7This item has no enchantments.")
                        .build());
            }
        } else {
            // Show all available enchantments
            for (Enchantment ench : Enchantment.values()) {
                int currentLevel = editingItem.getEnchantmentLevel(ench);
                String status = currentLevel > 0 ? "&a[" + currentLevel + "]" : "&7[None]";

                items.add(new ItemBuilder(Material.ENCHANTED_BOOK)
                        .name("&b" + formatEnchantName(ench))
                        .lore(
                                "&7Current: " + status,
                                "&7Max vanilla: &f" + ench.getMaxLevel(),
                                "",
                                "&a&lClick to set level (AnvilGUI)",
                                "&c&lShift-click to remove")
                        .build());
            }
        }

        return items;
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null || item.getType() == Material.AIR) return;

        // Toggle between view modes
        if (item.getType() == Material.BOOK && item.hasItemMeta()
                && item.getItemMeta().hasDisplayName()) {
            String name = TextUtil.stripColor(item.getItemMeta().getDisplayName());
            if (name.contains("Current Enchants")) {
                showCurrentEnchants = true;
                refresh();
                return;
            } else if (name.contains("All Enchantments")) {
                showCurrentEnchants = false;
                refresh();
                return;
            }
        }

        if (showCurrentEnchants) {
            // Remove enchantment
            for (Enchantment ench : editingItem.getEnchantments().keySet()) {
                if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                        && TextUtil.stripColor(item.getItemMeta().getDisplayName()).equals(ench.getName())) {
                    editingItem.removeEnchantment(ench);
                    player.sendMessage(TextUtil.colorize("&cRemoved " + ench.getName()));
                    refresh();
                    return;
                }
            }
        } else {
            // Add enchantment via AnvilGUI
            for (Enchantment ench : Enchantment.values()) {
                if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                        && TextUtil.stripColor(item.getItemMeta().getDisplayName())
                                .equals(formatEnchantName(ench))) {
                    if (player.isSneaking()) {
                        // Remove
                        editingItem.removeEnchantment(ench);
                        player.sendMessage(TextUtil.colorize("&cRemoved " + ench.getName()));
                        refresh();
                    } else {
                        // Set level via AnvilGUI — unlimited!
                        player.closeInventory();
                        new AnvilGUIBridge(plugin).openNumberInput(player,
                                "Level for " + ench.getName(), "1", level -> {
                            org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                                editingItem.addUnsafeEnchantment(ench, level);
                                player.sendMessage(TextUtil.colorize(
                                        "&aSet " + ench.getName() + " to level &e" + level));
                                plugin.getAuditManager().log(player, "ITEM_ENCHANT",
                                        editingItem.getType().name(),
                                        ench.getName() + " Lv" + level);
                                refresh();
                            });
                        });
                    }
                    return;
                }
            }
        }
    }

    private String formatEnchantName(Enchantment ench) {
        String key = ench.getKey().toString();
        // Extract the name part from "minecraft:sharpness" → "Sharpness"
        String name = key.contains(":") ? key.split(":")[1] : key;
        // Capitalize first letter of each word
        StringBuilder result = new StringBuilder();
        for (String word : name.split("_")) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1)).append(" ");
            }
        }
        return result.toString().trim();
    }

    @Override
    public String getMenuTitle() {
        return showCurrentEnchants ? "&0&lCurrent Enchants" : "&0&lAll Enchantments";
    }
}
