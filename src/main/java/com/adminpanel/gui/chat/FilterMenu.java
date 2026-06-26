package com.adminpanel.gui.chat;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.base.PaginationGUI;
import com.adminpanel.hooks.AnvilGUIBridge;
import com.adminpanel.util.ItemBuilder;
import com.adminpanel.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Chat filter management — view, add, and remove regex filter patterns.
 */
public class FilterMenu extends PaginationGUI {

    public FilterMenu(AdminPanel plugin, Player player) {
        super(plugin, player, "&0&lChat Filter");
    }

    @Override
    protected List<ItemStack> getPageItems() {
        List<ItemStack> items = new ArrayList<>();

        // Add filter button
        items.add(new ItemBuilder(Material.LIME_DYE)
                .name("&a&l+ Add New Filter")
                .lore("&7Add a new chat filter pattern")
                .build());

        // Load existing filters
        List<Map<String, Object>> filters = plugin.getDataManager().getChatFilters();
        for (Map<String, Object> filter : filters) {
            int id = ((Number) filter.get("id")).intValue();
            String pattern = (String) filter.get("pattern");
            String action = (String) filter.get("action");
            String reason = (String) filter.get("reason");

            Material icon = switch (action.toLowerCase()) {
                case "mute" -> Material.RED_WOOL;
                case "warn" -> Material.YELLOW_WOOL;
                case "kick" -> Material.ORANGE_WOOL;
                default -> Material.PAPER;
            };

            items.add(new ItemBuilder(icon)
                    .name("&eFilter #" + id + ": &f" + pattern)
                    .lore(
                            "&7Action: &c" + action,
                            "&7Reason: &f" + reason,
                            "",
                            "&c&lClick to remove")
                    .build());
        }

        return items;
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null || item.getType() == Material.AIR) return;

        if (item.getType() == Material.LIME_DYE) {
            // Add new filter
            player.closeInventory();
            new AnvilGUIBridge(plugin).openTextInput(player, "Enter regex pattern", "", (text) -> {
                // After entering pattern, ask for action
                Bukkit.getScheduler().runTask(plugin, () -> {
                    // Default action: mute
                    plugin.getDataManager().addChatFilter(text, "mute", "Chat filter violation");
                    player.sendMessage(TextUtil.colorize("&aAdded filter pattern: &f" + text));
                    plugin.getAuditManager().log(player, "FILTER_ADD", "Chat", "Pattern: " + text);
                    Bukkit.getScheduler().runTask(plugin, () -> refresh());
                });
            });
            return;
        }

        // Remove existing filter
        for (Map<String, Object> filter : plugin.getDataManager().getChatFilters()) {
            int id = ((Number) filter.get("id")).intValue();
            String pattern = (String) filter.get("pattern");

            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                    && TextUtil.stripColor(item.getItemMeta().getDisplayName()).contains("Filter #" + id)) {
                plugin.getDataManager().deleteChatFilter(id);
                player.sendMessage(TextUtil.colorize("&cRemoved filter: &f" + pattern));
                plugin.getAuditManager().log(player, "FILTER_REMOVE", "Chat", "Pattern: " + pattern);
                refresh();
                return;
            }
        }
    }

    @Override
    public String getMenuTitle() {
        return "&0&lChat Filter";
    }

}
