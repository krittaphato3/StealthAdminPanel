package com.adminpanel.gui.item;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.base.PaginationGUI;
import com.adminpanel.hooks.AnvilGUIBridge;
import com.adminpanel.util.ItemBuilder;
import com.adminpanel.util.TextUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Command bind editor — bind commands to execute when an item is used.
 *
 * Supports:
 * - Right-click: Execute command on use
 * - Arrow hit: Execute command on hit entity
 * - Melee hit: Execute command on hit entity
 *
 * Placeholders: %player%, %target%
 */
public class CommandBindMenu extends PaginationGUI {

    private final ItemStack editingItem;

    public CommandBindMenu(AdminPanel plugin, Player player, ItemStack item) {
        super(plugin, player, "&0&lCommand Binding");
        this.editingItem = item;
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

        // Show existing binds
        try {
            NBTItem nbtItem = new NBTItem(editingItem);

            String useCmd = nbtItem.hasKey("AdminPanel:Command") ?
                    nbtItem.getString("AdminPanel:Command") : null;
            if (useCmd != null) {
                items.add(new ItemBuilder(Material.GREEN_DYE)
                        .name("&a&lUse Command")
                        .lore(
                                "&7Command: &f" + truncate(useCmd, 40),
                                "&7Trigger: Right-click",
                                "",
                                "&c&lClick to remove")
                        .build());
            }

            String hitCmd = nbtItem.hasKey("AdminPanel:HitCommand") ?
                    nbtItem.getString("AdminPanel:HitCommand") : null;
            if (hitCmd != null) {
                items.add(new ItemBuilder(Material.RED_DYE)
                        .name("&c&lHit Command")
                        .lore(
                                "&7Command: &f" + truncate(hitCmd, 40),
                                "&7Trigger: Entity hit (melee/ranged)",
                                "",
                                "&c&lClick to remove")
                        .build());
            }

            String joinCmd = nbtItem.hasKey("AdminPanel:JoinCommand") ?
                    nbtItem.getString("AdminPanel:JoinCommand") : null;
            if (joinCmd != null) {
                items.add(new ItemBuilder(Material.BLUE_DYE)
                        .name("&9&lJoin Command")
                        .lore(
                                "&7Command: &f" + truncate(joinCmd, 40),
                                "&7Trigger: Player join (if holding item)",
                                "",
                                "&c&lClick to remove")
                        .build());
            }
        } catch (Exception e) {
            items.add(new ItemBuilder(Material.BARRIER)
                    .name("&c&lError reading NBT")
                    .lore("&7" + e.getMessage())
                    .build());
        }

        return items;
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null || item.getType() == Material.AIR) return;

        if (item.getType() == Material.LIME_DYE) {
            // Bind new command — ask for trigger type first
            player.closeInventory();
            // Default to "use" command
            new AnvilGUIBridge(plugin).openTextInput(player, "Enter command", "/say Hello %player%!", (cmd, event) -> {
                org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                    try {
                        NBTItem nbtItem = new NBTItem(editingItem);
                        nbtItem.setString("AdminPanel:Command", cmd);
                        editingItem.setItemMeta(nbtItem.getItem().getItemMeta());
                        player.sendMessage(TextUtil.colorize("&aBound command: &f" + cmd));
                        plugin.getAuditManager().log(player, "ITEM_BIND_CMD",
                                editingItem.getType().name(), cmd);
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
                NBTItem nbtItem = new NBTItem(editingItem);
                if (name.contains("Use Command")) {
                    nbtItem.removeKey("AdminPanel:Command");
                    player.sendMessage(TextUtil.colorize("&cRemoved use command"));
                } else if (name.contains("Hit Command")) {
                    nbtItem.removeKey("AdminPanel:HitCommand");
                    player.sendMessage(TextUtil.colorize("&cRemoved hit command"));
                } else if (name.contains("Join Command")) {
                    nbtItem.removeKey("AdminPanel:JoinCommand");
                    player.sendMessage(TextUtil.colorize("&cRemoved join command"));
                }
                editingItem.setItemMeta(nbtItem.getItem().getItemMeta());
            } catch (Exception e) {
                player.sendMessage(TextUtil.colorize("&cError: " + e.getMessage()));
            }
            refresh();
        }
    }

    private String truncate(String s, int maxLen) {
        return s.length() > maxLen ? s.substring(0, maxLen) + "..." : s;
    }

    @Override
    public String getMenuTitle() {
        return "&0&lCommand Binding";
    }
}
