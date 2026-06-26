package com.adminpanel.gui.server;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.base.PaginationGUI;
import com.adminpanel.util.ItemBuilder;
import com.adminpanel.util.TextUtil;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

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

        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        for (Object obj : banList.getEntries()) {
            BanEntry<?> entry = (BanEntry<?>) obj;
            String name = entry.getTarget();
            String reason = entry.getReason();

            items.add(new ItemBuilder(Material.RED_WOOL)
                    .name("&c&l" + name)
                    .lore(
                            "&7Reason: &f" + (reason != null ? reason : "None"),
                            "",
                            "&a&lClick to unban")
                    .build());
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
            Bukkit.getBanList(BanList.Type.NAME).pardon(targetName);
            // Also unban from plugin database
            plugin.getPunishmentManager().unbanPlayer(targetName);
            player.sendMessage(TextUtil.colorize("&aUnbanned &e" + targetName));
            plugin.getAuditManager().log(player, "UNBAN", targetName, "Unbanned via panel");
            refresh();
        }
    }

    @Override
    public void onBackClick() {
        new ServerMenu(plugin, player).open();
    }

    @Override
    public String getMenuTitle() {
        return "&0&lBan List";
    }
}
