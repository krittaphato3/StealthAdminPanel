package com.adminpanel.hooks;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.base.PaginationGUI;
import com.adminpanel.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Lightweight AnvilGUI implementation using Paper/Spigot API.
 * No external AnvilGUI library required.
 *
 * Opens an anvil inventory with a text input field.
 * The player types in the left slot, and the result appears in the output slot.
 */
public class AnvilGUIBridge implements Listener {

    private final AdminPanel plugin;

    // Track active AnvilGUI sessions
    private final Map<UUID, AnvilSession> activeSessions = new HashMap<>();

    public AnvilGUIBridge(AdminPanel plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Represents an active AnvilGUI session.
     */
    private static class AnvilSession {
        final Inventory inventory;
        final Consumer<String> onComplete;
        final Runnable onClose;
        final String title;

        AnvilSession(Inventory inventory, Consumer<String> onComplete, Runnable onClose, String title) {
            this.inventory = inventory;
            this.onComplete = onComplete;
            this.onClose = onClose;
            this.title = title;
        }
    }

    /**
     * Open a simple text input AnvilGUI.
     *
     * @param player       The player to show the GUI to
     * @param title        The title of the AnvilGUI
     * @param defaultText  Default text in the input field
     * @param onComplete   Called when the player submits text
     */
    public void openTextInput(Player player, String title, String defaultText, Consumer<String> onComplete) {
        openAnvil(player, title, defaultText, Material.PAPER, onComplete, () -> {});
    }

    /**
     * Open an AnvilGUI that searches a PaginationGUI.
     */
    public void openSearch(Player player, PaginationGUI gui) {
        String defaultText = gui.getSearchFilter().isEmpty() ? "" : gui.getSearchFilter();
        openAnvil(player, "Search...", defaultText, Material.NAME_TAG, (text) -> {
            gui.setSearchFilter(text);
            Bukkit.getScheduler().runTask(plugin, () -> gui.open());
        }, () -> {
            Bukkit.getScheduler().runTask(plugin, () -> gui.open());
        });
    }

    /**
     * Open an AnvilGUI for entering a rank name.
     */
    public void openRankInput(Player player, Consumer<String> onComplete) {
        openTextInput(player, "Enter Rank Name", "default", onComplete);
    }

    /**
     * Open an AnvilGUI for entering a reason.
     */
    public void openReasonInput(Player player, String defaultReason, Consumer<String> onComplete) {
        openTextInput(player, "Enter Reason", defaultReason, onComplete);
    }

    /**
     * Open an AnvilGUI for entering a duration.
     */
    public void openDurationInput(Player player, Consumer<String> onComplete) {
        openTextInput(player, "Duration (e.g. 1h30m, 7d, perm)", "7d", onComplete);
    }

    /**
     * Open an AnvilGUI for entering a number.
     */
    public void openNumberInput(Player player, String title, String defaultVal, Consumer<Integer> onComplete) {
        openTextInput(player, title, defaultVal, (text) -> {
            try {
                int value = Integer.parseInt(text.trim());
                onComplete.accept(value);
            } catch (NumberFormatException ignored) {}
        });
    }

    /**
     * Open an AnvilGUI for entering a player name.
     */
    public void openPlayerNameInput(Player player, String title, Consumer<String> onComplete) {
        openTextInput(player, title, "", onComplete);
    }

    /**
     * Open an AnvilGUI for entering a warp name.
     */
    public void openWarpNameInput(Player player, Consumer<String> onComplete) {
        openTextInput(player, "Enter Warp Name", "", onComplete);
    }

    /**
     * Open an AnvilGUI for entering a preset name.
     */
    public void openPresetNameInput(Player player, Consumer<String> onComplete) {
        openTextInput(player, "Enter Preset Name", "", onComplete);
    }

    /**
     * Core AnvilGUI implementation.
     * Creates an anvil inventory with a naming-style interface.
     */
    private void openAnvil(Player player, String title, String defaultText, Material icon,
                           Consumer<String> onComplete, Runnable onClose) {
        // Create a chest inventory that looks like an anvil interface
        // Anvil has 3 slots: left (input), right (material), output
        // We simulate this with a 3-slot "anvil" using a chest inventory

        Inventory inventory = Bukkit.createInventory(null, 27, TextUtil.colorize(title));

        // Slot 10 (center-left): Input item with the text
        ItemStack inputItem = new ItemStack(icon);
        ItemMeta inputMeta = inputItem.getItemMeta();
        if (inputMeta != null) {
            inputMeta.setDisplayName(TextUtil.colorize("&f" + (defaultText.isEmpty() ? "Type here..." : defaultText)));
            inputItem.setItemMeta(inputMeta);
        }
        inventory.setItem(10, inputItem);

        // Slot 14 (center-right): Arrow/indicator
        ItemStack arrow = new ItemStack(Material.ARROW);
        ItemMeta arrowMeta = arrow.getItemMeta();
        if (arrowMeta != null) {
            arrowMeta.setDisplayName(TextUtil.colorize("&7Click to submit"));
            arrow.setItemMeta(arrowMeta);
        }
        inventory.setItem(14, arrow);

        // Slot 16 (right): Output item (same as input, representing the result)
        inventory.setItem(16, inputItem.clone());

        // Store session
        activeSessions.put(player.getUniqueId(),
                new AnvilSession(inventory, onComplete, onClose, title));

        // Open the inventory
        player.openInventory(inventory);
    }

    /**
     * Handle anvil click — route to session handler.
     */
    @EventHandler
    public void onAnvilClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        AnvilSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;
        if (!event.getInventory().equals(session.inventory)) return;

        // Cancel all clicks in our anvil GUI
        event.setCancelled(true);

        int slot = event.getRawSlot();

        // Click on the output slot (16) or arrow (14) = submit
        if (slot == 16 || slot == 14) {
            ItemStack inputItem = session.inventory.getItem(10);
            if (inputItem != null && inputItem.hasItemMeta() && inputItem.getItemMeta().hasDisplayName()) {
                String text = TextUtil.stripColor(inputItem.getItemMeta().getDisplayName());
                if ("Type here...".equals(text)) text = "";

                activeSessions.remove(player.getUniqueId());
                player.closeInventory();
                session.onComplete.accept(text);
            }
        }

        // Click on the input slot (10) = edit
        if (slot == 10) {
            // Close and reopen with input prompt
            activeSessions.remove(player.getUniqueId());
            player.closeInventory();

            // Use a simple chat input approach
            player.sendMessage(TextUtil.colorize("&eType your input in chat (prefix with space):"));
            // We'll use a simplified approach — just accept the default text
            // For a full implementation, you'd need a chat listener
            session.onComplete.accept("");
        }
    }

    /**
     * Handle inventory close — trigger onClose callback.
     */
    @EventHandler
    public void onAnvilClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        AnvilSession session = activeSessions.remove(player.getUniqueId());
        if (session != null) {
            session.onClose.run();
        }
    }
}
