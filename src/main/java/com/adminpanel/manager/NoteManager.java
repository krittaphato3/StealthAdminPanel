package com.adminpanel.manager;

import com.adminpanel.AdminPanel;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

/**
 * Manages player notes: add, view, delete notes on player profiles.
 */
public class NoteManager {

    private final AdminPanel plugin;
    private final DataManager dataManager;

    public NoteManager(AdminPanel plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    /**
     * Add a note to a player's profile.
     */
    public int addNote(String targetName, Player author, String note) {
        OfflinePlayer target = org.bukkit.Bukkit.getOfflinePlayer(targetName);
        return dataManager.addNote(
                target.getUniqueId().toString(),
                targetName,
                author.getUniqueId().toString(),
                author.getName(),
                note
        );
    }

    /**
     * Get all notes for a player.
     */
    public List<Map<String, Object>> getNotes(String targetName) {
        OfflinePlayer target = org.bukkit.Bukkit.getOfflinePlayer(targetName);
        return dataManager.getNotes(target.getUniqueId().toString());
    }

    /**
     * Get all notes for a player by UUID.
     */
    public List<Map<String, Object>> getNotes(java.util.UUID targetUUID) {
        return dataManager.getNotes(targetUUID.toString());
    }

    /**
     * Delete a note by ID.
     */
    public boolean deleteNote(int noteId) {
        return dataManager.deleteNote(noteId);
    }
}
