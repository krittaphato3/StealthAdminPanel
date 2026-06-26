package com.adminpanel.manager;

import com.adminpanel.AdminPanel;

import java.util.List;
import java.util.Map;

/**
 * Manages presets/templates: announcement templates, ban reason presets, etc.
 */
public class PresetManager {

    private final AdminPanel plugin;
    private final DataManager dataManager;

    public PresetManager(AdminPanel plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    /**
     * Save a preset.
     *
     * @param type    "announcement" or "ban_reason"
     * @param name    Preset name
     * @param content Preset content
     */
    public boolean save(String type, String name, String content) {
        return dataManager.savePreset(type, name, content);
    }

    /**
     * Get all presets of a given type.
     */
    public List<Map<String, Object>> getPresets(String type) {
        return dataManager.getPresets(type);
    }

    /**
     * Get announcement presets.
     */
    public List<Map<String, Object>> getAnnouncementPresets() {
        return getPresets("announcement");
    }

    /**
     * Get ban reason presets.
     */
    public List<Map<String, Object>> getBanReasonPresets() {
        return getPresets("ban_reason");
    }

    /**
     * Delete a preset.
     */
    public boolean delete(String type, String name) {
        return dataManager.deletePreset(type, name);
    }
}
