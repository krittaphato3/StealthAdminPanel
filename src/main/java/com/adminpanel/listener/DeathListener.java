package com.adminpanel.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
 * Tracks player death drops for /ap restore.
 *
 * Flow:
 * 1. Player dies → items drop naturally on ground
 * 2. Items are tracked in memory (index-based)
 * 3. When someone picks up items, we record WHO took WHAT and HOW MUCH
 * 4. Admin runs /ap restore → shows full report with warnings
 * 5. Admin must run /ap restore confirm to actually restore
 * 6. Restoring creates duplicates if items were already picked up
 */
public class DeathListener implements Listener {

    // UUID (dead player) -> list of original drops
    private static final Map<UUID, List<ItemStack>> deathDrops = new ConcurrentHashMap<>();
    private static final Map<UUID, String> deathWorld = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> deathTime = new ConcurrentHashMap<>();

    // UUID (dead player) -> list of pickup records
    private static final Map<UUID, List<PickupRecord>> pickupRecords = new ConcurrentHashMap<>();

    // UUID (dead player) -> set of indices that were destroyed (lava, void, cactus)
    private static final Map<UUID, Set<Integer>> destroyedIndices = new ConcurrentHashMap<>();

    // Pending restore confirmations: UUID (admin) -> target UUID
    private static final Map<UUID, UUID> pendingRestores = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        List<ItemStack> drops = event.getDrops();

        if (drops.isEmpty()) return;

        // Copy the drops
        List<ItemStack> savedDrops = new ArrayList<>();
        for (ItemStack item : drops) {
            if (item != null) {
                savedDrops.add(item.clone());
            }
        }

        deathDrops.put(player.getUniqueId(), savedDrops);
        deathWorld.put(player.getUniqueId(), player.getWorld().getName());
        deathTime.put(player.getUniqueId(), System.currentTimeMillis());
        pickupRecords.put(player.getUniqueId(), new ArrayList<>());
        destroyedIndices.put(player.getUniqueId(), ConcurrentHashMap.newKeySet());

        // Items DROP naturally — not cleared
    }

    /**
     * Track when someone picks up items from death drops.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event) {
        Player picker = event.getPlayer();
        ItemStack pickedUp = event.getItem().getItemStack();

        for (Map.Entry<UUID, List<ItemStack>> entry : deathDrops.entrySet()) {
            UUID deadUUID = entry.getKey();
            if (deadUUID.equals(picker.getUniqueId())) continue;

            List<ItemStack> originalDrops = entry.getValue();
            List<PickupRecord> records = pickupRecords.get(deadUUID);
            if (records == null) continue;

            for (int i = 0; i < originalDrops.size(); i++) {
                ItemStack original = originalDrops.get(i);
                if (original.isSimilar(pickedUp) && pickedUp.getAmount() <= original.getAmount()) {
                    // Check if already fully picked up
                    int alreadyTaken = 0;
                    for (PickupRecord r : records) {
                        if (r.itemIndex == i) alreadyTaken += r.amount;
                    }

                    int available = original.getAmount() - alreadyTaken;
                    int toRecord = Math.min(pickedUp.getAmount(), available);

                    if (toRecord > 0) {
                        records.add(new PickupRecord(i, picker.getUniqueId(), picker.getName(),
                                pickedUp.getType(), toRecord));
                    }
                    break;
                }
            }
        }
    }

    /**
     * Check if a specific item index was fully picked up.
     */
    public static boolean isFullyPickedUp(UUID deadUUID, int itemIndex) {
        List<PickupRecord> records = pickupRecords.get(deadUUID);
        if (records == null) return false;

        List<ItemStack> drops = deathDrops.get(deadUUID);
        if (drops == null || itemIndex >= drops.size()) return false;

        int totalTaken = 0;
        for (PickupRecord r : records) {
            if (r.itemIndex == itemIndex) totalTaken += r.amount;
        }
        return totalTaken >= drops.get(itemIndex).getAmount();
    }

    /**
     * Get pickup records for a specific item index.
     */
    public static List<PickupRecord> getPickupsForItem(UUID deadUUID, int itemIndex) {
        List<PickupRecord> records = pickupRecords.get(deadUUID);
        if (records == null) return List.of();

        List<PickupRecord> result = new ArrayList<>();
        for (PickupRecord r : records) {
            if (r.itemIndex == itemIndex) result.add(r);
        }
        return result;
    }

    /**
     * Get all drops for a player (for preview, doesn't remove).
     */
    public static List<ItemStack> previewDrops(UUID playerUUID) {
        return deathDrops.getOrDefault(playerUUID, List.of());
    }

    /**
     * Check if there's a pending restore for this admin.
     */
    public static UUID getPendingRestore(UUID adminUUID) {
        return pendingRestores.get(adminUUID);
    }

    /**
     * Set a pending restore confirmation.
     */
    public static void setPendingRestore(UUID adminUUID, UUID targetUUID) {
        pendingRestores.put(adminUUID, targetUUID);
    }

    /**
     * Clear a pending restore confirmation.
     */
    public static void clearPendingRestore(UUID adminUUID) {
        pendingRestores.remove(adminUUID);
    }

    /**
     * Execute the actual restore — returns items and removes from tracking.
     */
    public static List<ItemStack> executeRestore(UUID targetUUID) {
        List<ItemStack> drops = deathDrops.remove(targetUUID);
        pickupRecords.remove(targetUUID);
        destroyedIndices.remove(targetUUID);
        deathWorld.remove(targetUUID);
        deathTime.remove(targetUUID);
        return drops != null ? drops : List.of();
    }

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

    public static Set<Integer> getDestroyedIndices(UUID playerUUID) {
        return destroyedIndices.getOrDefault(playerUUID, Set.of());
    }

    /**
     * Record a pickup for a specific item (used by restore system).
     */
    public static void recordPickup(UUID deadUUID, int itemIndex, UUID pickerUUID, String pickerName,
                                     Material material, int amount) {
        List<PickupRecord> records = pickupRecords.computeIfAbsent(deadUUID, k -> new ArrayList<>());
        records.add(new PickupRecord(itemIndex, pickerUUID, pickerName, material, amount));
    }

    /**
     * Record that an item was destroyed.
     */
    public static void recordDestroyed(UUID deadUUID, int itemIndex) {
        destroyedIndices.computeIfAbsent(deadUUID, k -> ConcurrentHashMap.newKeySet()).add(itemIndex);
    }

    /**
     * A record of who picked up what from death drops.
     */
    public static class PickupRecord {
        public final int itemIndex;
        public final UUID pickerUUID;
        public final String pickerName;
        public final Material material;
        public final int amount;

        public PickupRecord(int itemIndex, UUID pickerUUID, String pickerName, Material material, int amount) {
            this.itemIndex = itemIndex;
            this.pickerUUID = pickerUUID;
            this.pickerName = pickerName;
            this.material = material;
            this.amount = amount;
        }

        @Override
        public String toString() {
            return pickerName + " took " + amount + "x " + material.name();
        }
    }
}
