package com.adminpanel.gui.player;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.MainMenu;
import com.adminpanel.gui.base.PaginationGUI;
import com.adminpanel.util.HeadUtil;
import com.adminpanel.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Paginated list of online players with heads.
 * Click a player to open the PlayerActionMenu.
 */
public class PlayerListMenu extends PaginationGUI {

    public PlayerListMenu(AdminPanel plugin, Player player) {
        super(plugin, player, "&0&lOnline Players");
    }

    @Override
    protected List<ItemStack> getPageItems() {
        List<ItemStack> items = new ArrayList<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            String name = online.getName();
            int ping = online.getPing();
            String pingColor = ping < 50 ? "&a" : ping < 150 ? "&e" : "&c";

            ItemStack head = HeadUtil.getHead(name,
                    "&e&l" + name,
                    "&7Ping: " + pingColor + ping + "ms",
                    "&7Health: &c" + String.format("%.1f", online.getHealth()) + "&7/&c" + online.getMaxHealth(),
                    "&7World: &f" + online.getWorld().getName(),
                    "",
                    "&a&lClick to manage"
            );
            items.add(head);
        }
        return items;
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null || item.getType() != Material.PLAYER_HEAD) return;

        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String displayName = TextUtil.stripColor(item.getItemMeta().getDisplayName());
            // Find the online player whose name appears in the display name
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online != null && displayName.contains(online.getName())) {
                    new PlayerActionMenu(plugin, player, online).open();
                    return;
                }
            }
        }
    }

    @Override
    public void onBackClick() {
        new MainMenu(plugin, player).open();
    }

    @Override
    public String getMenuTitle() {
        return "&0&lOnline Players";
    }
}
