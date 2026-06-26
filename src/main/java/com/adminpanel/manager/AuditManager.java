package com.adminpanel.manager;

import com.adminpanel.AdminPanel;

import java.util.List;
import java.util.Map;

/**
 * Manages the audit log: records all admin actions for accountability.
 */
public class AuditManager {

    private final AdminPanel plugin;
    private final DataManager dataManager;

    public AuditManager(AdminPanel plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    /**
     * Log an admin action.
     *
     * @param adminUUID  UUID of the admin performing the action
     * @param adminName  Name of the admin
     * @param action     Action type (e.g., "BAN", "KICK", "TELEPORT", "ITEM_GIVE")
     * @param target     Target of the action (player name, etc.)
     * @param details    Additional details
     */
    public void log(String adminUUID, String adminName, String action, String target, String details) {
        dataManager.logAction(adminUUID, adminName, action, target, details);
    }

    /**
     * Log an admin action (convenience for online players).
     */
    public void log(org.bukkit.entity.Player admin, String action, String target, String details) {
        log(admin.getUniqueId().toString(), admin.getName(), action, target, details);
    }

    /**
     * Get audit log entries (paginated).
     */
    public List<Map<String, Object>> getLog(int page, int pageSize) {
        return dataManager.getAuditLog(page, pageSize);
    }

    /**
     * Search audit log by keyword.
     */
    public List<Map<String, Object>> search(String keyword, int page, int pageSize) {
        return dataManager.searchAuditLog(keyword, page, pageSize);
    }
}
