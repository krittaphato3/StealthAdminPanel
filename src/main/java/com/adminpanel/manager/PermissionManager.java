package com.adminpanel.manager;

import org.bukkit.entity.Player;

/**
 * Centralized permission node constants and helper methods.
 * All permission checks throughout the plugin reference this class.
 */
public final class PermissionManager {

    // Base
    public static final String USE = "adminpanel.use";

    // Player Control
    public static final String TROLL = "adminpanel.troll";
    public static final String INVSEE = "adminpanel.invsee";
    public static final String RANKS = "adminpanel.ranks";

    // World
    public static final String WORLD = "adminpanel.world";

    // Server
    public static final String SERVER = "adminpanel.server";

    // Economy
    public static final String ECONOMY = "adminpanel.economy";

    // Punishment
    public static final String PUNISH = "adminpanel.punish";

    // Chat
    public static final String CHAT = "adminpanel.chat";

    // Item Editor
    public static final String ITEM = "adminpanel.item";

    // Staff
    public static final String STAFF = "adminpanel.staff";

    // Warp
    public static final String WARP = "adminpanel.warp";

    // Notes
    public static final String NOTE = "adminpanel.note";

    // Config
    public static final String CONFIG = "adminpanel.config";

    // Audit Logs
    public static final String LOG = "adminpanel.log";

    // Announcements
    public static final String ANNOUNCE = "adminpanel.announce";

    // Monitoring
    public static final String MONITOR = "adminpanel.monitor";

    private PermissionManager() {}

    /**
     * Check if a player has a specific permission.
     */
    public static boolean has(Player player, String permission) {
        return player != null && player.hasPermission(permission);
    }

    /**
     * Check if a player has the base admin panel permission.
     */
    public static boolean hasBasePermission(Player player) {
        return has(player, USE);
    }

    /**
     * Check if a player can access player control features.
     */
    public static boolean canControlPlayers(Player player) {
        return has(player, TROLL) || has(player, INVSEE) || has(player, RANKS);
    }

    /**
     * Check if a player can access punishment features.
     */
    public static boolean canPunish(Player player) {
        return has(player, PUNISH);
    }

    /**
     * Check if a player can access economy features.
     */
    public static boolean canUseEconomy(Player player) {
        return has(player, ECONOMY);
    }

    /**
     * Check if a player can access chat management.
     */
    public static boolean canManageChat(Player player) {
        return has(player, CHAT);
    }

    /**
     * Check if a player can access world features.
     */
    public static boolean canManageWorld(Player player) {
        return has(player, WORLD);
    }

    /**
     * Check if a player can access server management.
     */
    public static boolean canManageServer(Player player) {
        return has(player, SERVER);
    }

    /**
     * Check if a player can edit items.
     */
    public static boolean canEditItems(Player player) {
        return has(player, ITEM);
    }

    /**
     * Check if a player can access staff features.
     */
    public static boolean canUseStaffFeatures(Player player) {
        return has(player, STAFF);
    }

    /**
     * Check if a player can manage warps.
     */
    public static boolean canManageWarps(Player player) {
        return has(player, WARP);
    }

    /**
     * Check if a player can use notes.
     */
    public static boolean canUseNotes(Player player) {
        return has(player, NOTE);
    }

    /**
     * Check if a player can edit config.
     */
    public static boolean canEditConfig(Player player) {
        return has(player, CONFIG);
    }

    /**
     * Check if a player can view audit logs.
     */
    public static boolean canViewLogs(Player player) {
        return has(player, LOG);
    }

    /**
     * Check if a player can send announcements.
     */
    public static boolean canAnnounce(Player player) {
        return has(player, ANNOUNCE);
    }

    /**
     * Check if a player can view monitoring data.
     */
    public static boolean canMonitor(Player player) {
        return has(player, MONITOR);
    }
}
