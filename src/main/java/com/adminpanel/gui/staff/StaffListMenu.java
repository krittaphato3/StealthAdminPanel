package com.adminpanel.gui.staff;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.MainMenu;
import com.adminpanel.gui.base.SubMenu;
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
 * Staff online list — shows all staff members with teleport-to buttons.
 */
public class StaffListMenu extends PaginationGUI {

    public StaffListMenu(AdminPanel plugin, Player player) {
        super(plugin, player, "&0&lStaff Online");
    }

    @Override
    protected List<ItemStack> getPageItems() {
        List<ItemStack> items = new ArrayList<>();

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("adminpanel.staff") || online.hasPermission("adminpanel.use")) {
                String group = "Unknown";
                if (plugin.getVaultHook().hasPermissions()) {
                    group = plugin.getVaultHook().getGroup(online, online.getWorld().getName());
                }

                items.add(HeadUtil.getHead(online.getName(),
                        "&a&l" + online.getName(),
                        "&7Group: &e" + group,
                        "&7Ping: &f" + online.getPing() + "ms",
                        "&7World: &f" + online.getWorld().getName(),
                        "",
                        "&a&lClick to teleport to staff member"));
            }
        }

        if (items.isEmpty()) {
            items.add(new org.bukkit.inventory.ItemStack(Material.BARRIER));
        }

        return items;
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null || item.getType() != Material.PLAYER_HEAD) return;

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                    && TextUtil.stripColor(item.getItemMeta().getDisplayName()).contains(online.getName())) {
                player.teleport(online.getLocation());
                player.sendMessage(TextUtil.colorize("&aTeleported to &e" + online.getName()));
                return;
            }
        }
    }

    @Override
    public String getMenuTitle() {
        return "&0&lStaff Online";
    }
}
