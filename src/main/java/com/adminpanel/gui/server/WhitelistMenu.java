package com.adminpanel.gui.server;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.base.PaginationGUI;
import com.adminpanel.hooks.AnvilGUIBridge;
import com.adminpanel.util.ColorUtil;
import com.adminpanel.util.ItemBuilder;
import com.adminpanel.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Whitelist management — toggle, add/remove players, view list.
 */
public class WhitelistMenu extends PaginationGUI {

    public WhitelistMenu(AdminPanel plugin, Player player) {
        super(plugin, player, "&0&lWhitelist");
    }

    @Override
    protected List<ItemStack> getPageItems() {
        List<ItemStack> items = new ArrayList<>();

        // Toggle button
        boolean wlEnabled = Bukkit.hasWhitelist();
        items.add(new ItemBuilder(wlEnabled ? Material.LIME_WOOL : Material.RED_WOOL)
                .name(wlEnabled ? "&a&l✔ Whitelist: ON" : "&c&l✘ Whitelist: OFF")
                .lore("&7Click to toggle whitelist")
                .build());

        // Add player button
        items.add(new ItemBuilder(Material.LIME_DYE)
                .name("&a&l+ Add Player")
                .lore("&7Add a player to the whitelist")
                .build());

        // List whitelisted players
        for (org.bukkit.OfflinePlayer wp : Bukkit.getWhitelistedPlayers()) {
            items.add(new ItemBuilder(Material.PAPER)
                    .name("&e" + (wp.getName() != null ? wp.getName() : wp.getUniqueId().toString().substring(0, 8)))
                    .lore(
                            "&7UUID: &f" + wp.getUniqueId(),
                            "",
                            "&c&lClick to remove from whitelist")
                    .build());
        }

        return items;
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null || item.getType() == Material.AIR) return;

        if (item.getType() == Material.LIME_WOOL || item.getType() == Material.RED_WOOL) {
            // Toggle whitelist
            boolean newState = !Bukkit.hasWhitelist();
            Bukkit.setWhitelist(newState);
            player.sendMessage(TextUtil.colorize(newState ? "&aWhitelist ENABLED" : "&cWhitelist DISABLED"));
            plugin.getAuditManager().log(player, "WHITELIST_TOGGLE", "Server",
                    newState ? "Enabled" : "Disabled");
            refresh();
        } else if (item.getType() == Material.LIME_DYE) {
            // Add player
            player.closeInventory();
            new AnvilGUIBridge(plugin).openPlayerNameInput(player, "Player to whitelist", name -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    @SuppressWarnings("deprecation")
                    org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(name);
                    target.setWhitelisted(true);
                    player.sendMessage(TextUtil.colorize("&aAdded &e" + name + " &ato whitelist"));
                    plugin.getAuditManager().log(player, "WHITELIST_ADD", name, "Added to whitelist");
                    Bukkit.getScheduler().runTask(plugin, () -> refresh());
                });
            });
        } else if (item.getType() == Material.PAPER) {
            // Remove player from whitelist
            String name = TextUtil.stripColor(item.getItemMeta().getDisplayName());
            @SuppressWarnings("deprecation")
            org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(name);
            target.setWhitelisted(false);
            player.sendMessage(TextUtil.colorize("&cRemoved &e" + name + " &cfrom whitelist"));
            plugin.getAuditManager().log(player, "WHITELIST_REMOVE", name, "Removed from whitelist");
            refresh();
        }
    }

    @Override
    public void onBackClick() {
        new ServerMenu(plugin, player).open();
    }

    @Override
    public String getMenuTitle() {
        return "&0&lWhitelist";
    }
}
