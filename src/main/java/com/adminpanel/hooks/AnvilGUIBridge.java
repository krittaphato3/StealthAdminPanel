package com.adminpanel.hooks;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.base.PaginationGUI;
import com.adminpanel.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Lightweight AnvilGUI replacement using chat-based text input.
 *
 * Since we can't use the external AnvilGUI library (repo unreachable),
 * this provides text input via a chat prompt system:
 * 1. Player clicks a search/input button → inventory closes → prompt appears in chat
 * 2. Player types in chat → input is captured → callback fires
 * 3. If player sends another command or goes idle, input is cancelled
 *
 * This is a singleton — registered once in AdminPanel.onEnable().
 * All instances use the shared static input map.
 */
public class AnvilGUIBridge implements Listener {

    private static AnvilGUIBridge instance;
    private final AdminPanel plugin;

    // Active input sessions: UUID → Consumer<String>
    private static final Map<UUID, Consumer<String>> pendingInputs = new ConcurrentHashMap<>();

    // Track who is in an input session so we can intercept chat
    private static final Map<UUID, Long> inputTimestamps = new ConcurrentHashMap<>();

    // Timeout for input sessions (30 seconds)
    private static final long INPUT_TIMEOUT_MS = 30_000;

    public AnvilGUIBridge(AdminPanel plugin) {
        this.plugin = plugin;
        instance = this;
    }

    /**
     * Get the singleton instance.
     */
    public static AnvilGUIBridge getInstance() {
        return instance;
    }

    /**
     * Open a text input prompt via chat.
     *
     * @param player      The player to prompt
     * @param title       Title shown in chat
     * @param defaultText Default suggestion shown in chat
     * @param onComplete  Called with the entered text on main thread
     */
    public void openTextInput(Player player, String title, String defaultText, Consumer<String> onComplete) {
        // Close any open inventory
        player.closeInventory();

        // Show prompt in chat
        player.sendMessage(TextUtil.colorize("&6&l" + title));
        player.sendMessage(TextUtil.colorize("&7Type your input in chat. &eDefault: &f" + defaultText));
        player.sendMessage(TextUtil.colorize("&7Type &c/cancel &7to abort."));

        // Register the input session
        pendingInputs.put(player.getUniqueId(), onComplete);
        inputTimestamps.put(player.getUniqueId(), System.currentTimeMillis());
    }

    /**
     * Open a search prompt that filters a PaginationGUI.
     */
    public void openSearch(Player player, PaginationGUI gui) {
        player.closeInventory();

        String currentFilter = gui.getSearchFilter();
        player.sendMessage(TextUtil.colorize("&6&lSearch"));
        player.sendMessage(TextUtil.colorize("&7Current filter: &f" + (currentFilter.isEmpty() ? "None" : currentFilter)));
        player.sendMessage(TextUtil.colorize("&7Type your search term in chat."));
        player.sendMessage(TextUtil.colorize("&7Type &c/cancel &7to clear filter and go back."));

        pendingInputs.put(player.getUniqueId(), (text) -> {
            gui.setSearchFilter(text);
            Bukkit.getScheduler().runTask(plugin, () -> gui.open());
        });
        inputTimestamps.put(player.getUniqueId(), System.currentTimeMillis());
    }

    /**
     * Open a rank input prompt.
     */
    public void openRankInput(Player player, Consumer<String> onComplete) {
        openTextInput(player, "Enter Rank Name", "default", onComplete);
    }

    /**
     * Open a reason input prompt.
     */
    public void openReasonInput(Player player, String defaultReason, Consumer<String> onComplete) {
        openTextInput(player, "Enter Reason", defaultReason, onComplete);
    }

    /**
     * Open a duration input prompt.
     */
    public void openDurationInput(Player player, Consumer<String> onComplete) {
        openTextInput(player, "Duration (e.g. 1h30m, 7d, perm)", "7d", onComplete);
    }

    /**
     * Open a number input prompt.
     */
    public void openNumberInput(Player player, String title, String defaultVal, Consumer<Integer> onComplete) {
        openTextInput(player, title, defaultVal, (text) -> {
            try {
                int value = Integer.parseInt(text.trim());
                onComplete.accept(value);
            } catch (NumberFormatException e) {
                player.sendMessage(TextUtil.colorize("&cInvalid number: " + text));
            }
        });
    }

    /**
     * Open a player name input prompt.
     */
    public void openPlayerNameInput(Player player, String title, Consumer<String> onComplete) {
        openTextInput(player, title, "", onComplete);
    }

    /**
     * Open a warp name input prompt.
     */
    public void openWarpNameInput(Player player, Consumer<String> onComplete) {
        openTextInput(player, "Enter Warp Name", "", onComplete);
    }

    /**
     * Open a preset name input prompt.
     */
    public void openPresetNameInput(Player player, Consumer<String> onComplete) {
        openTextInput(player, "Enter Preset Name", "", onComplete);
    }

    /**
     * Check if a player is currently in an input session.
     */
    public static boolean isInputActive(UUID playerUUID) {
        return pendingInputs.containsKey(playerUUID);
    }

    /**
     * Handle chat messages — capture input from players in an input session.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!pendingInputs.containsKey(uuid)) return;

        // Check timeout
        Long timestamp = inputTimestamps.get(uuid);
        if (timestamp != null && System.currentTimeMillis() - timestamp > INPUT_TIMEOUT_MS) {
            pendingInputs.remove(uuid);
            inputTimestamps.remove(uuid);
            player.sendMessage(TextUtil.colorize("&cInput timed out."));
            return;
        }

        String message = event.getMessage().trim();
        event.setCancelled(true); // Don't broadcast the input

        // Handle cancel
        if (message.equalsIgnoreCase("/cancel") || message.equalsIgnoreCase("cancel")) {
            pendingInputs.remove(uuid);
            inputTimestamps.remove(uuid);
            player.sendMessage(TextUtil.colorize("&cInput cancelled."));
            return;
        }

        // Capture input and fire callback on main thread
        Consumer<String> callback = pendingInputs.remove(uuid);
        inputTimestamps.remove(uuid);

        if (callback != null) {
            String finalMessage = message;
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    callback.accept(finalMessage);
                } catch (Exception e) {
                    player.sendMessage(TextUtil.colorize("&cError processing input: " + e.getMessage()));
                }
            });
        }
    }

    /**
     * Handle commands — cancel input if player types a command instead.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!pendingInputs.containsKey(uuid)) return;

        String message = event.getMessage().trim();

        // Allow /cancel to cancel input
        if (message.equalsIgnoreCase("/cancel")) {
            event.setCancelled(true);
            pendingInputs.remove(uuid);
            inputTimestamps.remove(uuid);
            player.sendMessage(TextUtil.colorize("&cInput cancelled."));
            return;
        }

        // Any other command cancels the input
        pendingInputs.remove(uuid);
        inputTimestamps.remove(uuid);
        player.sendMessage(TextUtil.colorize("&cInput cancelled."));
    }

    /**
     * Clean up sessions for disconnected players.
     */
    public static void cleanup(UUID playerUUID) {
        pendingInputs.remove(playerUUID);
        inputTimestamps.remove(playerUUID);
    }
}
