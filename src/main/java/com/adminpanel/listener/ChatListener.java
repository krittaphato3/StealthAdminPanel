package com.adminpanel.listener;

import com.adminpanel.AdminPanel;
import com.adminpanel.manager.ChatManager;
import com.adminpanel.manager.DataManager;
import com.adminpanel.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Handles chat-related features:
 * - Global mute enforcement
 * - Slow mode enforcement
 * - Staff chat routing
 * - Chat filter enforcement
 */
public class ChatListener implements Listener {

    private final AdminPanel plugin;
    private final ChatManager chatManager;

    public ChatListener(AdminPanel plugin, ChatManager chatManager) {
        this.plugin = plugin;
        this.chatManager = chatManager;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        // === Staff Chat ===
        if (chatManager.isStaffChatToggled(player.getUniqueId())) {
            event.setCancelled(true);
            chatManager.sendStaffMessage(player, message);

            // Also log to console for staff chat
            String format = plugin.getConfig().getString("chat.staff-chat-format",
                    "&8[&bStaff&8] &e%player%&7: &f%message%");
            String logMessage = TextUtil.stripColor(
                    format.replace("%player%", player.getName()).replace("%message%", message));
            plugin.getLogger().info("[StaffChat] " + logMessage);
            return;
        }

        // === Global Mute ===
        if (chatManager.isGlobalMute() && !player.hasPermission("adminpanel.chat")) {
            event.setCancelled(true);
            player.sendMessage(TextUtil.colorize("&cChat is currently muted."));
            return;
        }

        // === Slow Mode ===
        if (!chatManager.canSendMessage(player) && !player.hasPermission("adminpanel.chat")) {
            event.setCancelled(true);
            long remaining = chatManager.getRemainingCooldown(player);
            player.sendMessage(TextUtil.colorize(
                    "&cSlow mode active. Wait &e" + remaining + " &cseconds."));
            return;
        }

        // Record message for slow mode tracking
        chatManager.recordMessage(player);

        // === Chat Filter ===
        if (!player.hasPermission("adminpanel.chat")) {
            DataManager dm = plugin.getDataManager();
            List<Map<String, Object>> filters = dm.getChatFilters();
            for (Map<String, Object> filter : filters) {
                String patternStr = (String) filter.get("pattern");
                String action = (String) filter.get("action");
                String reason = (String) filter.get("reason");

                try {
                    Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
                    if (pattern.matcher(message).find()) {
                        event.setCancelled(true);
                        handleFilterAction(player, action, reason);
                        return;
                    }
                } catch (Exception ignored) {
                    // Invalid regex pattern — skip
                }
            }
        }
    }

    /**
     * Handle a chat filter violation.
     */
    private void handleFilterAction(Player player, String action, String reason) {
        switch (action.toLowerCase()) {
            case "mute" -> {
                chatManager.mutePlayer(player.getUniqueId());
                player.sendMessage(TextUtil.colorize("&cYou have been muted. Reason: " + reason));
            }
            case "warn" -> {
                player.sendMessage(TextUtil.colorize("&cChat filter warning: " + reason));
            }
            case "kick" -> {
                player.kickPlayer(TextUtil.stripColor(reason));
            }
            default -> {
                player.sendMessage(TextUtil.colorize("&cMessage blocked: " + reason));
            }
        }
    }
}
