package com.adminpanel.hooks;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.base.PaginationGUI;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Bridge/wrapper for AnvilGUI operations.
 * Provides convenient methods for opening Anvil input dialogs.
 */
public class AnvilGUIBridge {

    private final AdminPanel plugin;

    public AnvilGUIBridge(AdminPanel plugin) {
        this.plugin = plugin;
    }

    /**
     * Open a simple text input AnvilGUI.
     *
     * @param player    The player to show the GUI to
     * @param title     The title of the AnvilGUI
     * @param defaultText Default text in the input field
     * @param onComplete Called when the player submits text (text, slot)
     */
    public void openTextInput(Player player, String title, String defaultText,
                               BiConsumer<String, AnvilGUI.TextResponse> onComplete) {
        new AnvilGUI.Builder()
                .plugin(plugin)
                .title(title)
                .text(defaultText)
                .itemLeft(new org.bukkit.inventory.ItemStack(org.bukkit.Material.PAPER))
                .handler((event) -> {
                    onComplete.accept(event.getName(), event);
                })
                .close((player1) -> {})
                .open(player);
    }

    /**
     * Open an AnvilGUI that opens a PaginationGUI with the search result.
     *
     * @param player   The player
     * @param gui      The PaginationGUI to filter after search
     */
    public void openSearch(Player player, PaginationGUI gui) {
        new AnvilGUI.Builder()
                .plugin(plugin)
                .title("Search...")
                .text(gui.getSearchFilter().isEmpty() ? "" : gui.getSearchFilter())
                .itemLeft(new org.bukkit.inventory.ItemStack(org.bukkit.Material.NAME_TAG))
                .handler((event) -> {
                    String searchText = event.getName();
                    gui.setSearchFilter(searchText);
                    // Re-open the GUI after a tick (must close Anvil first)
                    org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> gui.open());
                })
                .close((player1) -> {
                    // If they close without clicking, re-open the GUI
                    org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> gui.open());
                })
                .open(player);
    }

    /**
     * Open an AnvilGUI for entering a rank name.
     *
     * @param player     The player
     * @param onComplete Called with the entered rank name
     */
    public void openRankInput(Player player, Consumer<String> onComplete) {
        new AnvilGUI.Builder()
                .plugin(plugin)
                .title("Enter Rank Name")
                .text("default")
                .itemLeft(new org.bukkit.inventory.ItemStack(org.bukkit.Material.NAME_TAG))
                .handler((event) -> {
                    String rankName = event.getName().trim();
                    if (!rankName.isEmpty()) {
                        onComplete.accept(rankName);
                    }
                })
                .close((player1) -> {})
                .open(player);
    }

    /**
     * Open an AnvilGUI for entering a reason.
     *
     * @param player     The player
     * @param defaultReason Default text
     * @param onComplete Called with the entered reason
     */
    public void openReasonInput(Player player, String defaultReason, Consumer<String> onComplete) {
        new AnvilGUI.Builder()
                .plugin(plugin)
                .title("Enter Reason")
                .text(defaultReason)
                .itemLeft(new org.bukkit.inventory.ItemStack(org.bukkit.Material.PAPER))
                .handler((event) -> {
                    String reason = event.getName().trim();
                    if (!reason.isEmpty()) {
                        onComplete.accept(reason);
                    }
                })
                .close((player1) -> {})
                .open(player);
    }

    /**
     * Open an AnvilGUI for entering a duration.
     *
     * @param player     The player
     * @param onComplete Called with the entered duration string
     */
    public void openDurationInput(Player player, Consumer<String> onComplete) {
        new AnvilGUI.Builder()
                .plugin(plugin)
                .title("Duration (e.g. 1h30m, 7d, perm)")
                .text("7d")
                .itemLeft(new org.bukkit.inventory.ItemStack(org.bukkit.Material.CLOCK))
                .handler((event) -> {
                    String duration = event.getName().trim();
                    if (!duration.isEmpty()) {
                        onComplete.accept(duration);
                    }
                })
                .close((player1) -> {})
                .open(player);
    }

    /**
     * Open an AnvilGUI for entering a number (enchant level, amount, etc.).
     *
     * @param player     The player
     * @param title      The GUI title
     * @param defaultVal Default value
     * @param onComplete Called with the entered number
     */
    public void openNumberInput(Player player, String title, String defaultVal, Consumer<Integer> onComplete) {
        new AnvilGUI.Builder()
                .plugin(plugin)
                .title(title)
                .text(defaultVal)
                .itemLeft(new org.bukkit.inventory.ItemStack(org.bukkit.Material.PAPER))
                .handler((event) -> {
                    try {
                        int value = Integer.parseInt(event.getName().trim());
                        onComplete.accept(value);
                    } catch (NumberFormatException ignored) {}
                })
                .close((player1) -> {})
                .open(player);
    }

    /**
     * Open an AnvilGUI for entering a player name.
     *
     * @param player     The player
     * @param title      The GUI title
     * @param onComplete Called with the entered player name
     */
    public void openPlayerNameInput(Player player, String title, Consumer<String> onComplete) {
        new AnvilGUI.Builder()
                .plugin(plugin)
                .title(title)
                .text("")
                .itemLeft(new org.bukkit.inventory.ItemStack(org.bukkit.Material.PLAYER_HEAD))
                .handler((event) -> {
                    String name = event.getName().trim();
                    if (!name.isEmpty()) {
                        onComplete.accept(name);
                    }
                })
                .close((player1) -> {})
                .open(player);
    }

    /**
     * Open an AnvilGUI for entering a warp name.
     *
     * @param player     The player
     * @param onComplete Called with the entered warp name
     */
    public void openWarpNameInput(Player player, Consumer<String> onComplete) {
        new AnvilGUI.Builder()
                .plugin(plugin)
                .title("Enter Warp Name")
                .text("")
                .itemLeft(new org.bukkit.inventory.ItemStack(org.bukkit.Material.ENDER_PEARL))
                .handler((event) -> {
                    String name = event.getName().trim();
                    if (!name.isEmpty()) {
                        onComplete.accept(name);
                    }
                })
                .close((player1) -> {})
                .open(player);
    }

    /**
     * Open an AnvilGUI for entering a preset name.
     *
     * @param player     The player
     * @param onComplete Called with the entered preset name
     */
    public void openPresetNameInput(Player player, Consumer<String> onComplete) {
        new AnvilGUI.Builder()
                .plugin(plugin)
                .title("Enter Preset Name")
                .text("")
                .itemLeft(new org.bukkit.inventory.ItemStack(org.bukkit.Material.BOOK))
                .handler((event) -> {
                    String name = event.getName().trim();
                    if (!name.isEmpty()) {
                        onComplete.accept(name);
                    }
                })
                .close((player1) -> {})
                .open(player);
    }
}
