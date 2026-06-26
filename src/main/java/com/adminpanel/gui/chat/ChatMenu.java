package com.adminpanel.gui.chat;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.MainMenu;
import com.adminpanel.gui.base.SubMenu;
import com.adminpanel.util.ColorUtil;
import com.adminpanel.util.ItemBuilder;
import com.adminpanel.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Chat management hub — global mute, slow mode, staff chat, mute list, filter.
 */
public class ChatMenu extends SubMenu {

    public ChatMenu(AdminPanel plugin, Player player) {
        super(plugin, player, "&0&lChat Management", 4);
    }

    @Override
    protected void buildMenu() {
        boolean globalMute = plugin.getChatManager().isGlobalMute();
        int slowMode = plugin.getChatManager().getSlowModeCooldown();

        // Global Mute toggle
        setItem(10, Material.RED_WOOL,
                globalMute ? "&a&l✔ Global Mute: ON" : "&c&l✘ Global Mute: OFF",
                "&7Toggle global chat mute",
                "&7Status: " + ColorUtil.stateIndicator(globalMute));

        // Slow Mode
        setItem(12, Material.CLOCK,
                "&e&lSlow Mode",
                "&7Current cooldown: &f" + slowMode + "s",
                "&7Click to change cooldown");

        // Staff Chat Toggle
        boolean staffChatOn = plugin.getChatManager().isStaffChatToggled(player.getUniqueId());
        setItem(14, Material.EMERALD,
                staffChatOn ? "&a&l✔ Staff Chat: ON" : "&c&l✘ Staff Chat: OFF",
                "&7Toggle staff-only chat mode",
                "&7Your messages will go to staff chat");

        // Mute List
        int mutedCount = plugin.getChatManager().getMutedCount();
        setItem(16, Material.BARRIER,
                "&c&lMute List",
                "&7Currently muted: &e" + mutedCount,
                "&7View & manage muted players");

        // Chat Filter
        setItem(28, Material.BOOK,
                "&9&lChat Filter",
                "&7Manage chat filter patterns",
                "&7Auto-mute for rule violations");

        addBackButton();
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null || item.getType() == Material.AIR) return;

        switch (slot) {
            case 10 -> {
                // Toggle Global Mute
                boolean newState = plugin.getChatManager().toggleGlobalMute();
                player.sendMessage(TextUtil.colorize(newState ? "&cGlobal mute ENABLED" : "&aGlobal mute DISABLED"));
                plugin.getAuditManager().log(player, "CHAT_GLOBAL_MUTE", "Server",
                        newState ? "Enabled" : "Disabled");
                refresh();
            }
            case 12 -> {
                // Change Slow Mode — cycle through common values
                int current = plugin.getChatManager().getSlowModeCooldown();
                int next = switch (current) {
                    case 0 -> 3;
                    case 3 -> 5;
                    case 5 -> 10;
                    case 10 -> 30;
                    case 30 -> 60;
                    default -> 0;
                };
                plugin.getChatManager().setSlowModeCooldown(next);
                player.sendMessage(TextUtil.colorize("&eSlow mode set to &c" + next + "s"));
                plugin.getAuditManager().log(player, "CHAT_SLOW_MODE", "Server", "Cooldown: " + next + "s");
                refresh();
            }
            case 14 -> {
                // Toggle Staff Chat
                boolean toggled = plugin.getChatManager().toggleStaffChat(player.getUniqueId());
                player.sendMessage(TextUtil.colorize(toggled ?
                        "&aStaff chat ENABLED — your messages go to staff only" :
                        "&cStaff chat DISABLED — normal chat restored"));
                refresh();
            }
            case 16 -> new MuteListMenu(plugin, player).open();
            case 28 -> new FilterMenu(plugin, player).open();
            case 45 -> new MainMenu(plugin, player).open();
        }
    }
}
