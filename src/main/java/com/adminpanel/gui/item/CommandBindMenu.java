package com.adminpanel.gui.item;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.base.PaginationGUI;
import com.adminpanel.hooks.AnvilGUIBridge;
import com.adminpanel.util.ItemBuilder;
import com.adminpanel.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Command bind editor — bind commands to execute when an item is used.
 *
 * Uses PersistentDataContainer for storing command bindings.
 * Supports:
 * - adminpanel:command → Execute on right-click
 * - adminpanel:hitcmd → Execute on entity hit (melee/ranged)
 *
 * Placeholders: %player%, %target%
 */
public class CommandBindMenu extends PaginationGUI {

    private final ItemStack editingItem;
    private final NamespacedKey useCmdKey;
    private final NamespacedKey hitCmdKey;

    public CommandBindMenu(AdminPanel plugin, Player player, ItemStack item) {
        super(plugin, player, "&0&lCommand Binding");
        this.editingItem = item;
        this.useCmdKey = new NamespacedKey(plugin, "command");
        this.hitCmdKey = new NamespacedKey(plugin, "hitcmd");
    }

    @Override
    protected List<ItemStack> getPageItems() {
        List<ItemStack> items = new ArrayList<>();

        // Add new bind button
        items.add(new ItemBuilder(Material.LIME_DYE)
                .name("&a&l+ Bind New Command")
                .lore("&7Bind a command to execute on use",
                      "&7Placeholders: &f%player% %target%")
                .build());

        try {
            ItemMeta meta = editingItem.getItemMeta();
            if (meta != null) {
                PersistentDataContainer container = meta.getPersistentDataContainer();

                // Use command
                if (container.has(useCmdKey, PersistentDataType.STRING)) {
                    String cmd = container.get(useCmdKey, PersistentDataType.STRING);
                    items.add(new ItemBuilder(Material.GREEN_DYE)
                            .name("&a&lUse Command")
                            .lore(
                                    "&7Command: &f" + truncate(cmd, 40),
                                    "&7Trigger: Right-click",
                                    "",
                                    "&c&lClick to remove")
                            .build());
                }

                // Hit command
                if (container.has(hitCmdKey, PersistentDataType.STRING)) {
                    String cmd = container.get(hitCmdKey, PersistentDataType.STRING);
                    items.add(new ItemBuilder(Material.RED_DYE)
                            .name("&c&lHit Command")
                            .lore(
                                    "&7Command: &f" + truncate(cmd, 40),
                                    "&7Trigger: Entity hit (melee/ranged)",
                                    "",
                                    "&c&lClick to remove")
                            .build());
                }
            }
        } catch (Exception e) {
            items.add(new ItemBuilder(Material.BARRIER)
                    .name("&c&lError reading tags")
                    .lore("&7" + e.getMessage())
                    .build());
        }

        return items;
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null || item.getType() == Material.AIR) return;

        if (item.getType() == Material.LIME_DYE) {
            // Bind new command
            player.closeInventory();
            new AnvilGUIBridge(plugin).openTextInput(player, "Enter command", "/say Hello %player%!", (cmd) -> {
                org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        ItemMeta meta = editingItem.getItemMeta();
                        if (meta != null) {
                            meta.getPersistentDataContainer().set(useCmdKey, PersistentDataType.STRING, cmd);
                            editingItem.setItemMeta(meta);
                            player.sendMessage(TextUtil.colorize("&aBound command: &f" + cmd));
                            plugin.getAuditManager().log(player, "ITEM_BIND_CMD",
                                    editingItem.getType().name(), cmd);
                        }
                    } catch (Exception e) {
                        player.sendMessage(TextUtil.colorize("&cError: " + e.getMessage()));
                    }
                    refresh();
                });
            });
            return;
        }

        // Remove existing bind
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String name = TextUtil.stripColor(item.getItemMeta().getDisplayName());
            try {
                ItemMeta meta = editingItem.getItemMeta();
                if (meta != null) {
                    if (name.contains("Use Command")) {
                        meta.getPersistentDataContainer().remove(useCmdKey);
                        player.sendMessage(TextUtil.colorize("&cRemoved use command"));
                    } else if (name.contains("Hit Command")) {
                        meta.getPersistentDataContainer().remove(hitCmdKey);
                        player.sendMessage(TextUtil.colorize("&cRemoved hit command"));
                    }
                    editingItem.setItemMeta(meta);
                }
            } catch (Exception e) {
                player.sendMessage(TextUtil.colorize("&cError: " + e.getMessage()));
            }
            refresh();
        }
    }

    private String truncate(String s, int maxLen) {
        return s != null && s.length() > maxLen ? s.substring(0, maxLen) + "..." : s;
    }

    @Override
    public void onBackClick() {
        new ItemEditorMenu(plugin, player).open();
    }

    @Override
    public String getMenuTitle() {
        return "&0&lCommand Binding";
    }
}
