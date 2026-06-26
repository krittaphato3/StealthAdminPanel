package com.adminpanel.gui.preset;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.MainMenu;
import com.adminpanel.gui.base.PaginationGUI;
import com.adminpanel.hooks.AnvilGUIBridge;
import com.adminpanel.util.ItemBuilder;
import com.adminpanel.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Preset management — create and manage announcement templates and ban reason presets.
 */
public class PresetMenu extends PaginationGUI {

    public PresetMenu(AdminPanel plugin, Player player) {
        super(plugin, player, "&0&lPresets & Templates");
    }

    @Override
    protected List<ItemStack> getPageItems() {
        List<ItemStack> items = new ArrayList<>();

        // Create new preset button
        items.add(new ItemBuilder(Material.LIME_DYE)
                .name("&a&l+ Create New Preset")
                .lore("&7Type: &fannouncement &7or &fban_reason",
                      "&7Enter name and content")
                .build());

        // Announcement presets
        List<Map<String, Object>> announcements = plugin.getPresetManager().getAnnouncementPresets();
        for (Map<String, Object> preset : announcements) {
            String name = (String) preset.get("name");
            String content = (String) preset.get("content");

            items.add(new ItemBuilder(Material.BELL)
                    .name("&e&l[Announcement] " + name)
                    .lore(
                            "&7Content: &f" + truncate(content, 40),
                            "",
                            "&a&lClick to use",
                            "&c&lShift-click to delete")
                    .build());
        }

        // Ban reason presets
        List<Map<String, Object>> banReasons = plugin.getPresetManager().getBanReasonPresets();
        for (Map<String, Object> preset : banReasons) {
            String name = (String) preset.get("name");
            String content = (String) preset.get("content");

            items.add(new ItemBuilder(Material.RED_WOOL)
                    .name("&c&l[Ban Reason] " + name)
                    .lore(
                            "&7Reason: &f" + content,
                            "",
                            "&a&lClick to use",
                            "&c&lShift-click to delete")
                    .build());
        }

        if (items.size() == 1) {
            items.add(new ItemBuilder(Material.BARRIER)
                    .name("&7&lNo presets yet")
                    .lore("&7Click + to create a preset")
                    .build());
        }

        return items;
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null || item.getType() == Material.AIR) return;

        if (item.getType() == Material.LIME_DYE) {
            // Create new preset
            player.closeInventory();
            new AnvilGUIBridge(plugin).openTextInput(player, "Preset name", "", (name, event) -> {
                org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                    // Ask for type
                    new AnvilGUIBridge(plugin).openTextInput(player, "Type (announcement/ban_reason)", "announcement", (type, event2) -> {
                        org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                            new AnvilGUIBridge(plugin).openTextInput(player, "Content", "", (content, event3) -> {
                                org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                                    plugin.getPresetManager().save(type, name, content);
                                    player.sendMessage(TextUtil.colorize(
                                            "&aCreated preset: &f" + name + " &a(" + type + ")"));
                                    plugin.getAuditManager().log(player, "PRESET_CREATE", name,
                                            "Type: " + type);
                                    refresh();
                                });
                            });
                        });
                    });
                });
            });
            return;
        }

        // Handle preset actions
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = TextUtil.stripColor(item.getItemMeta().getDisplayName());

            // Extract preset info
            String type = displayName.startsWith("[Announcement]") ? "announcement" : "ban_reason";
            String name = displayName.replace("[Announcement] ", "").replace("[Ban Reason] ", "");

            if (player.isSneaking()) {
                // Delete
                plugin.getPresetManager().delete(type, name);
                player.sendMessage(TextUtil.colorize("&cDeleted preset: &f" + name));
                plugin.getAuditManager().log(player, "PRESET_DELETE", name, "Type: " + type);
                refresh();
            } else {
                // Use preset
                List<Map<String, Object>> presets = plugin.getPresetManager().getPresets(type);
                for (Map<String, Object> p : presets) {
                    if (name.equals(p.get("name"))) {
                        String content = (String) p.get("content");
                        player.closeInventory();
                        // Broadcast the announcement
                        String prefix = plugin.getConfig().getString("announcement.prefix", "&6&l[Admin] &r");
                        org.bukkit.Bukkit.broadcastMessage(TextUtil.colorize(prefix + content));
                        player.sendMessage(TextUtil.colorize("&aAnnouncement sent!"));
                        plugin.getAuditManager().log(player, "ANNOUNCEMENT", "Server", content);
                        return;
                    }
                }
            }
        }
    }

    private String truncate(String s, int maxLen) {
        return s != null && s.length() > maxLen ? s.substring(0, maxLen) + "..." : s;
    }

    @Override
    public String getMenuTitle() {
        return "&0&lPresets & Templates";
    }
}
