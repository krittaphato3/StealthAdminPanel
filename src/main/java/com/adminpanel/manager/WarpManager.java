package com.adminpanel.manager;

import com.adminpanel.AdminPanel;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

/**
 * Manages warps: create, delete, teleport, list.
 * Warps are stored in the SQLite database.
 */
public class WarpManager {

    private final AdminPanel plugin;
    private final DataManager dataManager;

    public WarpManager(AdminPanel plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    /**
     * Create a warp at the player's current location.
     */
    public boolean createWarp(String name, Player creator) {
        Location loc = creator.getLocation();
        return dataManager.saveWarp(name,
                loc.getWorld().getName(),
                loc.getX(), loc.getY(), loc.getZ(),
                loc.getYaw(), loc.getPitch(),
                creator.getName());
    }

    /**
     * Teleport a player to a warp.
     */
    public boolean teleportTo(Player player, String warpName) {
        Map<String, Object> warp = dataManager.getWarp(warpName);
        if (warp == null) return false;

        World world = Bukkit.getWorld((String) warp.get("world"));
        if (world == null) return false;

        Location loc = new Location(world,
                (double) warp.get("x"),
                (double) warp.get("y"),
                (double) warp.get("z"),
                ((Number) warp.get("yaw")).floatValue(),
                ((Number) warp.get("pitch")).floatValue());

        player.teleport(loc);
        return true;
    }

    /**
     * Delete a warp.
     */
    public boolean deleteWarp(String name) {
        return dataManager.deleteWarp(name);
    }

    /**
     * Get all warps.
     */
    public List<Map<String, Object>> getAllWarps() {
        return dataManager.getAllWarps();
    }

    /**
     * Check if a warp exists.
     */
    public boolean warpExists(String name) {
        return dataManager.getWarp(name) != null;
    }
}
