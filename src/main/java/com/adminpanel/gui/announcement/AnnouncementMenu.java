package com.adminpanel.gui.announcement;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.MainMenu;
import com.adminpanel.gui.base.SubMenu;
import com.adminpanel.hooks.AnvilGUIBridge;
import com.adminpanel.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Announcement builder — create and send custom announcements.
 */
public class AnnouncementMenu extends SubMenu {

    public AnnouncementMenu(AdminPanel plugin, Player player) {
        super(plugin, player, "&0&lAnnouncements", 3);
    }

    @Override
    protected void buildMenu() {
        setItem(10, Material.BELL,
                "&e&lCustom Announcement",
                "&7Send a custom announcement",
                "&7Supports &-colors and &#RRGGBB hex",
                "&7Use | for new lines");

        setItem(12, Material.BOOK,
                "&6&lFrom Template",
                "&7Use a saved announcement template",
                "&7Manage templates in Presets menu");

        setItem(14, Material.PAPER,
                "&a&lQuick Broadcast",
                "&7Send a quick global message",
                "&7No frills, just the message");

        setItem(16, Material.BARRIER,
                "&c&lAlert Announcement",
                "&7Send a high-priority alert",
                "&7Uses red formatting");

        addBackButton();
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null || item.getType() == Material.AIR) return;

        switch (slot) {
            case 10 -> {
                // Custom announcement
                player.closeInventory();
                new AnvilGUIBridge(plugin).openTextInput(player,
                        "Announcement (use | for lines)", "Hello everyone!", (message) -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        String prefix = plugin.getConfig().getString("announcement.prefix", "&6&l[Admin] &r");
                        String[] lines = message.split("\\|");
                        for (String line : lines) {
                            Bukkit.broadcastMessage(TextUtil.colorize(prefix + line.trim()));
                        }
                        player.sendMessage(TextUtil.colorize("&aAnnouncement sent!"));
                        plugin.getAuditManager().log(player, "ANNOUNCEMENT", "Server",
                                "Custom: " + message);
                    });
                });
            }
            case 12 -> {
                // From template
                player.closeInventory();
                var templates = plugin.getPresetManager().getAnnouncementPresets();
                if (templates.isEmpty()) {
                    player.sendMessage(TextUtil.colorize("&cNo announcement templates found! Create some in Presets."));
                    return;
                }
                // Just send the first template for simplicity
                String content = (String) templates.get(0).get("content");
                String prefix = plugin.getConfig().getString("announcement.prefix", "&6&l[Admin] &r");
                Bukkit.broadcastMessage(TextUtil.colorize(prefix + content));
                player.sendMessage(TextUtil.colorize("&aTemplate announcement sent!"));
            }
            case 14 -> {
                // Quick broadcast
                player.closeInventory();
                new AnvilGUIBridge(plugin).openTextInput(player, "Quick message", "", (message) -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        Bukkit.broadcastMessage(TextUtil.colorize(message));
                        player.sendMessage(TextUtil.colorize("&aMessage broadcasted!"));
                        plugin.getAuditManager().log(player, "BROADCAST", "Server", message);
                    });
                });
            }
            case 16 -> {
                // Alert announcement
                player.closeInventory();
                new AnvilGUIBridge(plugin).openTextInput(player, "Alert message", "Server restart in 5 minutes!", (message) -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        String alertFormat = "&4&l⚠ ALERT ⚠ &c" + message;
                        Bukkit.broadcastMessage(TextUtil.colorize(alertFormat));
                        player.sendMessage(TextUtil.colorize("&cAlert sent!"));
                        plugin.getAuditManager().log(player, "ALERT", "Server", message);
                    });
                });
            }
            case 45 -> new MainMenu(plugin, player).open();
        }
    }
}
