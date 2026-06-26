package com.adminpanel.manager;

import com.adminpanel.AdminPanel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages chat-related state: global mute, slow mode, staff chat, and mutes.
 */
public class ChatManager {

    private final AdminPanel plugin;
    private boolean globalMute = false;
    private int slowModeCooldown = 0; // seconds, 0 = disabled
    private final Map<UUID, Long> lastMessageTime = new ConcurrentHashMap<>();
    private final Set<UUID> staffChatToggle = ConcurrentHashMap.newKeySet();
    private final Set<UUID> mutedPlayers = ConcurrentHashMap.newKeySet();

    public ChatManager(AdminPanel plugin) {
        this.plugin = plugin;
        this.slowModeCooldown = plugin.getConfig().getInt("chat.slow-mode-cooldown", 5);
    }

    // === Global Mute ===

    public boolean isGlobalMute() {
        return globalMute;
    }

    public void setGlobalMute(boolean state) {
        this.globalMute = state;
    }

    public boolean toggleGlobalMute() {
        globalMute = !globalMute;
        return globalMute;
    }

    // === Slow Mode ===

    public int getSlowModeCooldown() {
        return slowModeCooldown;
    }

    public void setSlowModeCooldown(int seconds) {
        this.slowModeCooldown = seconds;
    }

    public boolean isSlowModeActive() {
        return slowModeCooldown > 0;
    }

    /**
     * Check if a player can send a message (respects slow mode).
     * Returns true if allowed, false if on cooldown.
     */
    public boolean canSendMessage(Player player) {
        if (player.hasPermission("adminpanel.chat")) return true; // Staff bypass
        if (slowModeCooldown <= 0) return true;

        Long last = lastMessageTime.get(player.getUniqueId());
        if (last == null) return true;

        long elapsed = (System.currentTimeMillis() - last) / 1000;
        return elapsed >= slowModeCooldown;
    }

    /**
     * Get remaining cooldown for a player in seconds.
     */
    public long getRemainingCooldown(Player player) {
        Long last = lastMessageTime.get(player.getUniqueId());
        if (last == null) return 0;
        long elapsed = (System.currentTimeMillis() - last) / 1000;
        long remaining = slowModeCooldown - elapsed;
        return Math.max(0, remaining);
    }

    /**
     * Record that a player sent a message.
     */
    public void recordMessage(Player player) {
        lastMessageTime.put(player.getUniqueId(), System.currentTimeMillis());
    }

    // === Staff Chat ===

    public boolean isStaffChatToggled(UUID playerUUID) {
        return staffChatToggle.contains(playerUUID);
    }

    public boolean toggleStaffChat(UUID playerUUID) {
        if (staffChatToggle.contains(playerUUID)) {
            staffChatToggle.remove(playerUUID);
            return false;
        } else {
            staffChatToggle.add(playerUUID);
            return true;
        }
    }

    /**
     * Send a message to all online staff members.
     */
    public void sendStaffMessage(Player sender, String message) {
        String format = plugin.getConfig().getString("chat.staff-chat-format",
                "&8[&bStaff&8] &e%player%&7: &f%message%");
        String formatted = format
                .replace("%player%", sender.getName())
                .replace("%message%", message);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("adminpanel.staff") || player.hasPermission("adminpanel.chat")) {
                player.sendMessage(com.adminpanel.util.TextUtil.colorize(formatted));
            }
        }
    }

    // === Player Mutes ===

    public boolean isMuted(UUID playerUUID) {
        return mutedPlayers.contains(playerUUID);
    }

    public void mutePlayer(UUID playerUUID) {
        mutedPlayers.add(playerUUID);
    }

    public void unmutePlayer(UUID playerUUID) {
        mutedPlayers.remove(playerUUID);
    }

    /**
     * Get a list of currently muted player UUIDs.
     */
    public Set<UUID> getMutedPlayers() {
        return Collections.unmodifiableSet(mutedPlayers);
    }

    /**
     * Get the number of currently online muted players.
     */
    public int getMutedCount() {
        return mutedPlayers.size();
    }
}
