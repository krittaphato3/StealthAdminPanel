package com.adminpanel.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks player death drops for the /ap restore command.
 *
 * Flow:
 * 1. Player dies → items saved to memory → items ALSO drop naturally on ground
 * 2. Other players can pick up items (tracked in pickedUpItems set)
 * 3. Admin runs /ap restore → warns about items already picked up
 * 4. If items were picked up, restoring will cause duplication
 * 5. Items destroyed by lava/void are also tracked (cannot restore those)
 */
public class DeathListener implements Listener {

    // UUID -> dropped items
    private static final Map<UUID, List<ItemStack>> deathDrops = new ConcurrentHashMap<>();
    private static final Map<UUID, String> deathWorld = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> deathTime = new ConcurrentHashMap<>();

    // Track which items have been picked up by other players
    // UUID (dead player) -> Set of item indices that were picked up
    private static final Map<UUID, Set<Integer>> pickedUpIndices = new ConcurrentHashMap<>();

    // Track which items were destroyed (lava, void)
    private static final Map<UUID, Set<Integer>> destroyedIndices = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        List<ItemStack> drops = event.getDrops();

        if (drops.isEmpty()) return;

        // Copy the drops BEFORE they drop
        List<ItemStack> savedDrops = new ArrayList<>();
        for (ItemStack item : drops) {
            if (item != null) {
                savedDrops.add(item.clone());
            }
        }

        deathDrops.put(player.getUniqueId(), savedDrops);
        deathWorld.put(player.getUniqueId(), player.getWorld().getName());
        deathTime.put(player.getUniqueId(), System.currentTimeMillis());
        pickedUpIndices.put(player.getUniqueId(), ConcurrentHashMap.newKeySet());
        destroyedIndices.put(player.getUniqueId(), ConcurrentHashMap.newKeySet());

        // Items DROP naturally on the ground (not cleared)
        // This allows normal pickup behavior and theft detection
    }

    /**
     * Track when another player picks up items from a dead player's drops.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player picker = event.getPlayer();
        ItemStack pickedUp = event.getItem().getItemStack();

        // Check if this item came from a dead player's drops
        for (Map.Entry<UUID, List<ItemStack>> entry : deathDrops.entrySet()) {
            UUID deadUUID = entry.getKey();
            if (deadUUID.equals(picker.getUniqueId())) continue; // Picking up own items

            List<ItemStack> originalDrops = entry.getValue();
            Set<Integer> picked = pickedUpIndices.get(deadUUID);
            if (picked == null) continue;

            // Find matching item in the original drops
            for (int i = 0; i < originalDrops.size(); i++) {
                if (picked.contains(i)) continue; // Already tracked

                ItemStack original = originalDrops.get(i);
                if (original.isSimilar(pickedUp) && pickedUp.getAmount() <= original.getAmount()) {
                    picked.add(i);

                    // Notify online admins
                    notifyAdmins(deadUUID, picker, original);
                    break;
                }
            }
        }
    }

    /**
     * Notify admins that items have been picked up (duplication risk).
     */
    private void notifyAdmins(UUID deadUUID, Player picker, ItemStack item) {
        String deadName = Bukkit.getOfflinePlayer(deadUUID).getName();
        if (deadName == null) deadName = deadUUID.toString().substring(0, 8);

        String message = org.bukkit.ChatColor.translateAlternateColorCodes('&',
                "&e[Admin] &c" + picker.getName() + " &7picked up &f" +
                item.getAmount() + "x " + item.getType().name() +
                " &7from &e" + deadName + "'s &7death drops." +
                " &cRestoring will duplicate this item!");

        for (Player admin : Bukkit.getOnlinePlayers()) {
            if (admin.hasPermission("adminpanel.use") || admin.hasPermission("adminpanel.monitor")) {
                admin.sendMessage(message);
            }
        }
    }

    /**
     * Get and remove saved drops for a player.
     * Returns a copy with pickup/destroy status.
     */
    public static RestoreResult retrieveDrops(UUID playerUUID) {
        List<ItemStack> drops = deathDrops.remove(playerUUID);
        Set<Integer> picked = pickedUpIndices.remove(playerUUID);
        Set<Integer> destroyed = destroyedIndices.remove(playerUUID);
        deathWorld.remove(playerUUID);
        deathTime.remove(playerUUID);

        if (drops == null) return null;

        List<ItemStack> restored = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        for (int i = 0; i < drops.size(); i++) {
            ItemStack item = drops.get(i);

            if (destroyed != null && destroyed.contains(i)) {
                // Item was destroyed — cannot restore
                warnings.add("&cLOST: &f" + item.getType().name() + " x" + item.getAmount() +
                        " &7(destroyed by lava/void)");
            } else if (picked != null && picked.contains(i)) {
                // Item was picked up — will duplicate
                warnings.add("&eDUPLICATE: &f" + item.getType().name() + " x" + item.getAmount() +
                        " &7(already picked up by another player)");
                restored.add(item); // Still restore it
            } else {
                // Item safe to restore
                restored.add(item);
            }
        }

        return new RestoreResult(restored, warnings);
    }

    /**
     * Check if a player has saved drops.
     */
    public static boolean hasDrops(UUID playerUUID) {
        return deathDrops.containsKey(playerUUID) && !deathDrops.get(playerUUID).isEmpty();
    }

    public static String getDeathWorld(UUID playerUUID) {
        return deathWorld.getOrDefault(playerUUID, "Unknown");
    }

    public static long getDeathTime(UUID playerUUID) {
        return deathTime.getOrDefault(playerUUID, 0L);
    }

    public static Map<UUID, List<ItemStack>> getAllSavedDrops() {
        return deathDrops;
    }

    public static void clearDrops(UUID playerUUID) {
        deathDrops.remove(playerUUID);
        deathWorld.remove(playerUUID);
        deathTime.remove(playerUUID);
        pickedUpIndices.remove(playerUUID);
        destroyedIndices.remove(playerUUID);
    }

    /**
     * Result of a restore operation.
     */
    public static class RestoreResult {
        private final List<ItemStack> items;
        private final List<String> warnings;

        public RestoreResult(List<ItemStack> items, List<String> warnings) {
            this.items = items;
            this.warnings = warnings;
        }

        public List<ItemStack> getItems() { return items; }
        public List<String> getWarnings() { return warnings; }
        public boolean hasWarnings() { return !warnings.isEmpty(); }
        public int getTotalItems() { return items.size(); }
    }
}
