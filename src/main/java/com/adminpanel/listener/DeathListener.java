package com.adminpanel.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks player death drops for the /ap restore command.
 * When a player dies, their dropped items are saved in memory.
 * An admin can then restore those items with /ap restore <player>.
 */
public class DeathListener implements Listener {

    // UUID -> dropped items (survives until server restart or manual restore)
    private static final Map<UUID, List<ItemStack>> deathDrops = new ConcurrentHashMap<>();
    private static final Map<UUID, String> deathWorld = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> deathTime = new ConcurrentHashMap<>();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        List<ItemStack> drops = event.getDrops();

        if (drops.isEmpty()) return;

        // Copy the drops list (Bukkit clears the original list after the event)
        List<ItemStack> savedDrops = new ArrayList<>();
        for (ItemStack item : drops) {
            if (item != null) {
                savedDrops.add(item.clone());
            }
        }

        deathDrops.put(player.getUniqueId(), savedDrops);
        deathWorld.put(player.getUniqueId(), player.getWorld().getName());
        deathTime.put(player.getUniqueId(), System.currentTimeMillis());

        // Clear the drops so they don't actually drop
        drops.clear();
    }

    /**
     * Get and remove saved drops for a player (one-time restore).
     */
    public static List<ItemStack> retrieveDrops(UUID playerUUID) {
        return deathDrops.remove(playerUUID);
    }

    /**
     * Check if a player has saved drops.
     */
    public static boolean hasDrops(UUID playerUUID) {
        return deathDrops.containsKey(playerUUID) && !deathDrops.get(playerUUID).isEmpty();
    }

    /**
     * Get the world name where the player died.
     */
    public static String getDeathWorld(UUID playerUUID) {
        return deathWorld.getOrDefault(playerUUID, "Unknown");
    }

    /**
     * Get the timestamp of the death.
     */
    public static long getDeathTime(UUID playerUUID) {
        return deathTime.getOrDefault(playerUUID, 0L);
    }

    /**
     * Get all players with saved drops (for the list view).
     */
    public static Map<UUID, List<ItemStack>> getAllSavedDrops() {
        return deathDrops;
    }

    /**
     * Clear saved drops for a player.
     */
    public static void clearDrops(UUID playerUUID) {
        deathDrops.remove(playerUUID);
        deathWorld.remove(playerUUID);
        deathTime.remove(playerUUID);
    }
}
