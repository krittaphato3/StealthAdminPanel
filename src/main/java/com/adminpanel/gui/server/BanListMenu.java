package com.adminpanel.gui.server;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.base.PaginationGUI;
import com.adminpanel.util.ItemBuilder;
import com.adminpanel.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

/**
 * Server ban list — paginated, searchable.
 * Shows all banned players with unban option.
 */
public class BanListMenu extends PaginationGUI {

    public BanListMenu(AdminPanel plugin, Player player) {
        super(plugin, player, "&0&lBan List");
    }

    @Override
    protected List<ItemStack> getPageItems() {
        List<ItemStack> items = new ArrayList<>();

        for (org.bukkit.BanList banList : Bukkit.getBanLists()) {
            for (org.bukkit.BanEntry entry : banList.getEntries()) {
                String name = entry.getTarget();
                String reason = entry.getReason();
                Date expires = entry.getExpirationDate();
                Date created = entry.getCreated();

                String expiryStr = expires != null ?
                        "&7Expires: &e" + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(expires) :
                        "&7Expires: &cPermanent";

                items.add(new ItemBuilder(Material.RED_WOOL)
                        .name("&c&l" + name)
                        .lore(
                                "&7Reason: &f" + (reason != null ? reason : "None"),
                                expiryStr,
                                "&7Banned: &f" + (created != null ?
                                        new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(created) : "Unknown"),
                                "",
                                "&a&lClick to unban")
                        .build());
            }
        }

        if (items.isEmpty()) {
            items.add(new ItemBuilder(Material.LIME_DYE)
                    .name("&a&lNo banned players")
                    .lore("&7The ban list is empty.")
                    .build());
        }

        return items;
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null || item.getType() != Material.RED_WOOL) return;

        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String targetName = TextUtil.stripColor(item.getItemMeta().getDisplayName());
            // Unban
            for (org.bukkit.BanList banList : Bukkit.getBanLists()) {
                banList.pardon(targetName);
            }
            // Also unban from plugin database
            plugin.getPunishmentManager().unbanPlayer(targetName);
            player.sendMessage(TextUtil.colorize("&aUnbanned &e" + targetName));
            plugin.getAuditManager().log(player, "UNBAN", targetName, "Unbanned via panel");
            refresh();
        }
    }

    @Override
    public String getMenuTitle() {
        return "&0&lBan List";
    }
}
