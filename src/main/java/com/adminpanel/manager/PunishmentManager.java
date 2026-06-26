package com.adminpanel.manager;

import com.adminpanel.AdminPanel;
import com.adminpanel.util.DurationParser;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

/**
 * Manages punishments: bans, mutes, warnings.
 * All actions are dispatched through the console for stealth logging.
 */
public class PunishmentManager {

    private final AdminPanel plugin;
    private final DataManager dataManager;

    public PunishmentManager(AdminPanel plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    /**
     * Ban a player (temporary or permanent).
     * Dispatches through console for stealth logging.
     */
    public boolean banPlayer(String targetName, String reason, long durationMs, String issuerName) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        UUID targetUUID = target.getUniqueId();

        // Record in database
        dataManager.addPunishment(targetUUID.toString(), targetName, "ban", reason,
                "", issuerName, durationMs);

        // Dispatch via console for stealth
        if (durationMs == -1) {
            // Permanent ban
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ban " + targetName + " " + reason);
        } else {
            // Temp ban via tempban command (Spigot/Paper)
            long seconds = durationMs / 1000;
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    "tempban " + targetName + " " + reason + " " + seconds + "s");
        }

        return true;
    }

    /**
     * Unban a player.
     */
    public boolean unbanPlayer(String targetName) {
        // Deactivate active bans in database
        var punishments = dataManager.getActivePunishments(
                Bukkit.getOfflinePlayer(targetName).getUniqueId().toString());
        for (var p : punishments) {
            if ("ban".equals(p.get("type"))) {
                dataManager.deactivatePunishment((int) p.get("id"));
            }
        }

        // Dispatch via console
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pardon " + targetName);
        return true;
    }

    /**
     * Mute a player (temporary or permanent).
     * Uses the plugin's ChatManager for tracking.
     */
    public boolean mutePlayer(String targetName, String reason, long durationMs, String issuerName) {
        UUID targetUUID = Bukkit.getOfflinePlayer(targetName).getUniqueId();

        // Record in database
        dataManager.addPunishment(targetUUID.toString(), targetName, "mute", reason,
                "", issuerName, durationMs);

        // Track in ChatManager
        plugin.getChatManager().mutePlayer(targetUUID);

        // If online, notify them
        Player target = Bukkit.getPlayerExact(targetName);
        if (target != null) {
            String durationStr = durationMs == -1 ? "permanently" :
                    "for " + DurationParser.format(durationMs);
            target.sendMessage(com.adminpanel.util.TextUtil.colorize(
                    "&cYou have been muted " + durationStr + ". Reason: " + reason));
        }

        return true;
    }

    /**
     * Unmute a player.
     */
    public boolean unmutePlayer(String targetName) {
        UUID targetUUID = Bukkit.getOfflinePlayer(targetName).getUniqueId();

        // Deactivate in database
        var punishments = dataManager.getActivePunishments(targetUUID.toString());
        for (var p : punishments) {
            if ("mute".equals(p.get("type"))) {
                dataManager.deactivatePunishment((int) p.get("id"));
            }
        }

        // Remove from ChatManager
        plugin.getChatManager().unmutePlayer(targetUUID);

        return true;
    }

    /**
     * Issue a warning strike to a player.
     * Auto-bans if the configured threshold is reached.
     */
    public int warnPlayer(String targetName, String reason, String issuerName) {
        UUID targetUUID = Bukkit.getOfflinePlayer(targetName).getUniqueId();

        // Record warning
        dataManager.addPunishment(targetUUID.toString(), targetName, "warn", reason,
                "", issuerName, -1);

        int totalWarnings = dataManager.countWarnings(targetUUID.toString());

        // Check auto-ban threshold
        int threshold = plugin.getConfig().getInt("punishment.auto-ban-after-warns", 3);
        if (threshold > 0 && totalWarnings >= threshold) {
            // Auto temp ban
            String defaultBan = plugin.getConfig().getString("punishment.default-temp-ban", "7d");
            long duration = DurationParser.parse(defaultBan);
            banPlayer(targetName, "Auto-ban: " + totalWarnings + " warnings reached", duration, "System");

            // Reset warnings by deactivating them
            var warnings = dataManager.getActivePunishments(targetUUID.toString());
            for (var w : warnings) {
                if ("warn".equals(w.get("type"))) {
                    dataManager.deactivatePunishment((int) w.get("id"));
                }
            }

            return -1; // Signal auto-ban occurred
        }

        return totalWarnings;
    }

    /**
     * Check if a player has an active punishment of a given type.
     */
    public boolean hasActivePunishment(UUID playerUUID, String type) {
        return dataManager.hasActivePunishment(playerUUID.toString(), type);
    }

    /**
     * Check if a player is banned (via server ban list).
     */
    public boolean isBanned(String targetName) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(targetName);
        return player.isBanned();
    }

    /**
     * Get warning count for a player.
     */
    public int getWarningCount(UUID playerUUID) {
        return dataManager.countWarnings(playerUUID.toString());
    }
}
