package com.adminpanel.gui.staff;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.base.PaginationGUI;
import com.adminpanel.hooks.AnvilGUIBridge;
import com.adminpanel.util.ItemBuilder;
import com.adminpanel.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Player notes — view, add, and delete notes on a player profile.
 */
public class PlayerNotesMenu extends PaginationGUI {

    private final OfflinePlayer target;

    public PlayerNotesMenu(AdminPanel plugin, Player player, OfflinePlayer target) {
        super(plugin, player, "&0&lNotes: " + (target.getName() != null ? target.getName() : target.getUniqueId().toString().substring(0, 8)));
        this.target = target;
    }

    /**
     * Overloaded constructor for Player object.
     */
    public PlayerNotesMenu(AdminPanel plugin, Player player, Player target) {
        this(plugin, player, (OfflinePlayer) target);
    }

    @Override
    protected List<ItemStack> getPageItems() {
        List<ItemStack> items = new ArrayList<>();

        // Add note button
        items.add(new ItemBuilder(Material.LIME_DYE)
                .name("&a&l+ Add Note")
                .lore("&7Add a note to this player's profile")
                .build());

        // Load notes
        List<Map<String, Object>> notes = plugin.getNoteManager().getNotes(target.getUniqueId());
        for (Map<String, Object> note : notes) {
            int id = ((Number) note.get("id")).intValue();
            String author = (String) note.get("author_name");
            String content = (String) note.get("note");
            long createdAt = ((Number) note.get("created_at")).longValue();

            items.add(new ItemBuilder(Material.PAPER)
                    .name("&eNote #" + id + " by &f" + author)
                    .lore(
                            "&7" + content,
                            "",
                            "&7Date: &f" + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
                                    .format(new java.util.Date(createdAt)),
                            "",
                            "&c&lClick to delete")
                    .build());
        }

        if (notes.isEmpty() && items.size() == 1) {
            items.add(new ItemBuilder(Material.BARRIER)
                    .name("&7&lNo notes yet")
                    .lore("&7Click + to add a note")
                    .build());
        }

        return items;
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null || item.getType() == Material.AIR) return;

        if (item.getType() == Material.LIME_DYE) {
            // Add note
            player.closeInventory();
            new AnvilGUIBridge(plugin).openTextInput(player, "Enter note", "", (note) -> {
                org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                    plugin.getNoteManager().addNote(
                            target.getName() != null ? target.getName() : target.getUniqueId().toString(),
                            player, note);
                    player.sendMessage(TextUtil.colorize("&aNote added to &e" + target.getName()));
                    plugin.getAuditManager().log(player, "NOTE_ADD",
                            target.getName() != null ? target.getName() : "Unknown", note);
                    refresh();
                });
            });
            return;
        }

        if (item.getType() == Material.PAPER && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String name = TextUtil.stripColor(item.getItemMeta().getDisplayName());
            // Extract note ID
            if (name.startsWith("Note #")) {
                try {
                    int id = Integer.parseInt(name.split("#")[1].split(" ")[0]);
                    plugin.getNoteManager().deleteNote(id);
                    player.sendMessage(TextUtil.colorize("&cNote #" + id + " deleted"));
                    plugin.getAuditManager().log(player, "NOTE_DELETE",
                            target.getName() != null ? target.getName() : "Unknown",
                            "Deleted note #" + id);
                    refresh();
                } catch (Exception ignored) {}
            }
        }
    }

    @Override
    public String getMenuTitle() {
        return "&0&lNotes: " + (target.getName() != null ? target.getName() : "Unknown");
    }
}
