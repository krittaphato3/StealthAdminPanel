package com.adminpanel.manager;

import com.adminpanel.AdminPanel;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * SQLite database manager.
 * Handles all database operations for punishments, audit logs, player notes,
 * sessions, warps, presets, and chat filters.
 */
public class DataManager {

    private final AdminPanel plugin;
    private Connection connection;
    private final String dbPath;

    public DataManager(AdminPanel plugin) {
        this.plugin = plugin;
        this.dbPath = new File(plugin.getDataFolder(), "data.db").getAbsolutePath();
    }

    /**
     * Initialize database connection and create tables.
     */
    public void initialize() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            connection.setAutoCommit(true);
            createTables();
            plugin.getLogger().info("Database initialized successfully.");
        } catch (ClassNotFoundException | SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize database!", e);
        }
    }

    /**
     * Create all required tables if they don't exist.
     */
    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Punishments table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS punishments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    target_uuid TEXT NOT NULL,
                    target_name TEXT NOT NULL,
                    type TEXT NOT NULL,
                    reason TEXT DEFAULT '',
                    issuer_uuid TEXT DEFAULT '',
                    issuer_name TEXT DEFAULT 'Console',
                    duration INTEGER DEFAULT -1,
                    created_at INTEGER NOT NULL,
                    expires_at INTEGER DEFAULT -1,
                    active INTEGER DEFAULT 1
                )
            """);

            // Audit log table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS audit_log (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    admin_uuid TEXT NOT NULL,
                    admin_name TEXT NOT NULL,
                    action TEXT NOT NULL,
                    target TEXT DEFAULT '',
                    details TEXT DEFAULT '',
                    timestamp INTEGER NOT NULL
                )
            """);

            // Player notes table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS player_notes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    target_uuid TEXT NOT NULL,
                    target_name TEXT NOT NULL,
                    author_uuid TEXT DEFAULT '',
                    author_name TEXT DEFAULT '',
                    note TEXT NOT NULL,
                    created_at INTEGER NOT NULL
                )
            """);

            // Sessions table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS sessions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    player_uuid TEXT NOT NULL,
                    player_name TEXT NOT NULL,
                    ip TEXT DEFAULT '',
                    join_time INTEGER NOT NULL,
                    leave_time INTEGER DEFAULT 0
                )
            """);

            // Warps table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS warps (
                    name TEXT PRIMARY KEY,
                    world TEXT NOT NULL,
                    x REAL NOT NULL,
                    y REAL NOT NULL,
                    z REAL NOT NULL,
                    yaw REAL DEFAULT 0,
                    pitch REAL DEFAULT 0,
                    creator TEXT DEFAULT ''
                )
            """);

            // Presets table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS presets (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    type TEXT NOT NULL,
                    name TEXT NOT NULL,
                    content TEXT NOT NULL,
                    UNIQUE(type, name)
                )
            """);

            // Chat filter table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS chat_filter (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    pattern TEXT NOT NULL,
                    action TEXT DEFAULT 'mute',
                    reason TEXT DEFAULT 'Chat filter violation'
                )
            """);
        }
    }

    /**
     * Get the database connection. Creates a new one if closed.
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to get database connection", e);
        }
        return connection;
    }

    /**
     * Close the database connection.
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Database connection closed.");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Error closing database", e);
        }
    }

    // ===========================
    //  PUNISHMENT METHODS
    // ===========================

    /**
     * Add a punishment to the database.
     */
    public int addPunishment(String targetUUID, String targetName, String type,
                              String reason, String issuerUUID, String issuerName,
                              long duration) {
        long now = System.currentTimeMillis();
        long expiresAt = duration == -1 ? -1 : now + duration;

        String sql = "INSERT INTO punishments (target_uuid, target_name, type, reason, issuer_uuid, issuer_name, duration, created_at, expires_at, active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 1)";

        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, targetUUID);
            ps.setString(2, targetName);
            ps.setString(3, type);
            ps.setString(4, reason);
            ps.setString(5, issuerUUID);
            ps.setString(6, issuerName);
            ps.setLong(7, duration);
            ps.setLong(8, now);
            ps.setLong(9, expiresAt);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to add punishment", e);
        }
        return -1;
    }

    /**
     * Get all punishments (paginated, no filter).
     */
    public List<Map<String, Object>> getAllPunishments(int page, int pageSize) {
        int offset = page * pageSize;
        return queryList("SELECT * FROM punishments ORDER BY created_at DESC LIMIT ? OFFSET ?", pageSize, offset);
    }

    /**
     * Get active punishments for a player.
     */
    public List<Map<String, Object>> getActivePunishments(String targetUUID) {
        return queryList(
            "SELECT * FROM punishments WHERE target_uuid = ? AND active = 1 AND (expires_at = -1 OR expires_at > ?)",
            targetUUID, System.currentTimeMillis()
        );
    }

    /**
     * Get all punishments for a player (paginated).
     */
    public List<Map<String, Object>> getPunishments(String targetUUID, int page, int pageSize) {
        int offset = page * pageSize;
        return queryList(
            "SELECT * FROM punishments WHERE target_uuid = ? ORDER BY created_at DESC LIMIT ? OFFSET ?",
            targetUUID, pageSize, offset
        );
    }

    /**
     * Get all active punishments (for server ban list).
     */
    public List<Map<String, Object>> getAllActivePunishments(String type) {
        if (type != null) {
            return queryList(
                "SELECT * FROM punishments WHERE type = ? AND active = 1 AND (expires_at = -1 OR expires_at > ?)",
                type, System.currentTimeMillis()
            );
        }
        return queryList(
            "SELECT * FROM punishments WHERE active = 1 AND (expires_at = -1 OR expires_at > ?)",
            System.currentTimeMillis()
        );
    }

    /**
     * Deactivate a punishment.
     */
    public boolean deactivatePunishment(int id) {
        return executeUpdate("UPDATE punishments SET active = 0 WHERE id = ?", id);
    }

    /**
     * Check if a player has an active punishment of a given type.
     */
    public boolean hasActivePunishment(String targetUUID, String type) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT COUNT(*) FROM punishments WHERE target_uuid = ? AND type = ? AND active = 1 AND (expires_at = -1 OR expires_at > ?)")) {
            ps.setString(1, targetUUID);
            ps.setString(2, type);
            ps.setLong(3, System.currentTimeMillis());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Count active warnings for a player.
     */
    public int countWarnings(String targetUUID) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT COUNT(*) FROM punishments WHERE target_uuid = ? AND type = 'warn' AND active = 1")) {
            ps.setString(1, targetUUID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            return 0;
        }
    }

    // ===========================
    //  AUDIT LOG METHODS
    // ===========================

    /**
     * Log an admin action.
     */
    public void logAction(String adminUUID, String adminName, String action, String target, String details) {
        String sql = "INSERT INTO audit_log (admin_uuid, admin_name, action, target, details, timestamp) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, adminUUID);
            ps.setString(2, adminName);
            ps.setString(3, action);
            ps.setString(4, target);
            ps.setString(5, details);
            ps.setLong(6, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to log audit action", e);
        }
    }

    /**
     * Get audit log entries (paginated).
     */
    public List<Map<String, Object>> getAuditLog(int page, int pageSize) {
        int offset = page * pageSize;
        return queryList("SELECT * FROM audit_log ORDER BY timestamp DESC LIMIT ? OFFSET ?", pageSize, offset);
    }

    /**
     * Search audit log by keyword.
     */
    public List<Map<String, Object>> searchAuditLog(String keyword, int page, int pageSize) {
        int offset = page * pageSize;
        String like = "%" + keyword + "%";
        return queryList(
            "SELECT * FROM audit_log WHERE action LIKE ? OR target LIKE ? OR details LIKE ? OR admin_name LIKE ? ORDER BY timestamp DESC LIMIT ? OFFSET ?",
            like, like, like, like, pageSize, offset
        );
    }

    // ===========================
    //  PLAYER NOTE METHODS
    // ===========================

    /**
     * Add a note to a player's profile.
     */
    public int addNote(String targetUUID, String targetName, String authorUUID, String authorName, String note) {
        String sql = "INSERT INTO player_notes (target_uuid, target_name, author_uuid, author_name, note, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, targetUUID);
            ps.setString(2, targetName);
            ps.setString(3, authorUUID);
            ps.setString(4, authorName);
            ps.setString(5, note);
            ps.setLong(6, System.currentTimeMillis());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to add note", e);
        }
        return -1;
    }

    /**
     * Get notes for a player.
     */
    public List<Map<String, Object>> getNotes(String targetUUID) {
        return queryList("SELECT * FROM player_notes WHERE target_uuid = ? ORDER BY created_at DESC", targetUUID);
    }

    /**
     * Delete a note by ID.
     */
    public boolean deleteNote(int id) {
        return executeUpdate("DELETE FROM player_notes WHERE id = ?", id);
    }

    // ===========================
    //  SESSION METHODS
    // ===========================

    /**
     * Record a player session join.
     */
    public void recordJoin(String playerUUID, String playerName, String ip) {
        String sql = "INSERT INTO sessions (player_uuid, player_name, ip, join_time, leave_time) VALUES (?, ?, ?, ?, 0)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, playerUUID);
            ps.setString(2, playerName);
            ps.setString(3, ip);
            ps.setLong(4, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to record session join", e);
        }
    }

    /**
     * Record a player session leave.
     */
    public void recordLeave(String playerUUID) {
        String sql = "UPDATE sessions SET leave_time = ? WHERE player_uuid = ? AND leave_time = 0";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setLong(1, System.currentTimeMillis());
            ps.setString(2, playerUUID);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to record session leave", e);
        }
    }

    /**
     * Get session history for a player.
     */
    public List<Map<String, Object>> getSessions(String playerUUID) {
        return queryList("SELECT * FROM sessions WHERE player_uuid = ? ORDER BY join_time DESC LIMIT 50", playerUUID);
    }

    /**
     * Get all sessions with a specific IP (alt detection).
     */
    public List<Map<String, Object>> getSessionsByIP(String ip) {
        return queryList("SELECT DISTINCT player_uuid, player_name, ip FROM sessions WHERE ip = ?", ip);
    }

    /**
     * Get the last known IP for a player.
     */
    public String getLastIP(String playerUUID) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT ip FROM sessions WHERE player_uuid = ? AND ip != '' ORDER BY join_time DESC LIMIT 1")) {
            ps.setString(1, playerUUID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("ip");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get last IP", e);
        }
        return "Unknown";
    }

    /**
     * Get total playtime for a player in milliseconds.
     */
    public long getTotalPlaytime(String playerUUID) {
        try (PreparedStatement ps = getConnection().prepareStatement(
                "SELECT SUM(CASE WHEN leave_time = 0 THEN ? - join_time ELSE leave_time - join_time END) as total FROM sessions WHERE player_uuid = ?")) {
            ps.setLong(1, System.currentTimeMillis());
            ps.setString(2, playerUUID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("total");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get playtime", e);
        }
        return 0;
    }

    // ===========================
    //  WARP METHODS
    // ===========================

    /**
     * Save a warp.
     */
    public boolean saveWarp(String name, String world, double x, double y, double z, float yaw, float pitch, String creator) {
        String sql = "INSERT OR REPLACE INTO warps (name, world, x, y, z, yaw, pitch, creator) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, name.toLowerCase());
            ps.setString(2, world);
            ps.setDouble(3, x);
            ps.setDouble(4, y);
            ps.setDouble(5, z);
            ps.setFloat(6, yaw);
            ps.setFloat(7, pitch);
            ps.setString(8, creator);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save warp", e);
            return false;
        }
    }

    /**
     * Get all warps.
     */
    public List<Map<String, Object>> getAllWarps() {
        return queryList("SELECT * FROM warps ORDER BY name");
    }

    /**
     * Get a warp by name.
     */
    public Map<String, Object> getWarp(String name) {
        List<Map<String, Object>> results = queryList("SELECT * FROM warps WHERE name = ?", name.toLowerCase());
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Delete a warp.
     */
    public boolean deleteWarp(String name) {
        return executeUpdate("DELETE FROM warps WHERE name = ?", name.toLowerCase());
    }

    // ===========================
    //  PRESET METHODS
    // ===========================

    /**
     * Save a preset.
     */
    public boolean savePreset(String type, String name, String content) {
        String sql = "INSERT OR REPLACE INTO presets (type, name, content) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, type);
            ps.setString(2, name);
            ps.setString(3, content);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Get presets by type.
     */
    public List<Map<String, Object>> getPresets(String type) {
        return queryList("SELECT * FROM presets WHERE type = ?", type);
    }

    /**
     * Delete a preset.
     */
    public boolean deletePreset(String type, String name) {
        return executeUpdate("DELETE FROM presets WHERE type = ? AND name = ?", type, name);
    }

    // ===========================
    //  CHAT FILTER METHODS
    // ===========================

    /**
     * Add a chat filter pattern.
     */
    public int addChatFilter(String pattern, String action, String reason) {
        String sql = "INSERT INTO chat_filter (pattern, action, reason) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, pattern);
            ps.setString(2, action);
            ps.setString(3, reason);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to add chat filter", e);
        }
        return -1;
    }

    /**
     * Get all chat filter patterns.
     */
    public List<Map<String, Object>> getChatFilters() {
        return queryList("SELECT * FROM chat_filter");
    }

    /**
     * Delete a chat filter.
     */
    public boolean deleteChatFilter(int id) {
        return executeUpdate("DELETE FROM chat_filter WHERE id = ?", id);
    }

    // ===========================
    //  GENERIC HELPER METHODS
    // ===========================

    /**
     * Execute a query and return results as a list of maps.
     */
    private List<Map<String, Object>> queryList(String sql, Object... params) {
        List<Map<String, Object>> results = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int cols = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= cols; i++) {
                        row.put(meta.getColumnLabel(i), rs.getObject(i));
                    }
                    results.add(row);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Query failed: " + sql, e);
        }
        return results;
    }

    /**
     * Execute an update query.
     */
    private boolean executeUpdate(String sql, Object... params) {
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Update failed: " + sql, e);
            return false;
        }
    }
}
