package com.adminpanel.manager;

import com.adminpanel.AdminPanel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player session tracking: join/leave times, IP tracking, alt detection.
 */
public class SessionManager {

    private final AdminPanel plugin;
    private final DataManager dataManager;
    private final Set<UUID> trackedPlayers = ConcurrentHashMap.newKeySet();

    public SessionManager(AdminPanel plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    /**
     * Record a player join event.
     */
    public void onJoin(Player player) {
        String ip = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "Unknown";
        dataManager.recordJoin(player.getUniqueId().toString(), player.getName(), ip);
        trackedPlayers.add(player.getUniqueId());

        // Notify online admins about the join
        notifyAdmins(player, ip);
    }

    /**
     * Record a player leave event.
     */
    public void onLeave(Player player) {
        dataManager.recordLeave(player.getUniqueId().toString());
        trackedPlayers.remove(player.getUniqueId());
    }

    /**
     * Notify online staff about a player join (if they have the permission).
     */
    private void notifyAdmins(Player player, String ip) {
        String message = com.adminpanel.util.TextUtil.colorize(
                "&7[&a+&7] &e" + player.getName() + " &7joined &8(" + ip + "&8)");

        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission("adminpanel.monitor") && !staff.equals(player)) {
                staff.sendMessage(message);
            }
        }
    }

    /**
     * Get session history for a player.
     */
    public List<Map<String, Object>> getSessions(String playerUUID) {
        return dataManager.getSessions(playerUUID);
    }

    /**
     * Get the last known IP for a player.
     */
    public String getLastIP(String playerUUID) {
        return dataManager.getLastIP(playerUUID);
    }

    /**
     * Get total playtime for a player.
     */
    public long getPlaytime(String playerUUID) {
        return dataManager.getTotalPlaytime(playerUUID);
    }

    /**
     * Detect alt accounts (same IP, different UUIDs).
     */
    public List<Map<String, Object>> detectAlts(String playerUUID) {
        String ip = dataManager.getLastIP(playerUUID);
        if ("Unknown".equals(ip)) return Collections.emptyList();
        return dataManager.getSessionsByIP(ip);
    }

    /**
     * Detect alts by IP string.
     */
    public List<Map<String, Object>> detectAltsByIP(String ip) {
        return dataManager.getSessionsByIP(ip);
    }

    /**
     * Load active sessions (for startup recovery).
     */
    public void loadActiveSessions() {
        // Mark any sessions without a leave_time as left (server restart)
        // This is handled naturally — sessions with leave_time=0 represent current sessions
        plugin.getLogger().info("Session tracking initialized.");
    }

    /**
     * Check if a player is currently tracked (online).
     */
    public boolean isTracked(UUID playerUUID) {
        return trackedPlayers.contains(playerUUID);
    }
}
