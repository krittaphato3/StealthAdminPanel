package com.adminpanel.gui.server;

import com.adminpanel.AdminPanel;
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
 * Active players — paginated list with quick actions (teleport, kick).
 */
public class ActivePlayersMenu extends PaginationGUI {

    public ActivePlayersMenu(AdminPanel plugin, Player player) {
        super(plugin, player, "&0&lActive Players");
    }

    @Override
    protected List<ItemStack> getPageItems() {
        List<ItemStack> items = new ArrayList<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            items.add(HeadUtil.getHead(online.getName(),
                    "&e&l" + online.getName(),
                    "&7Ping: &f" + online.getPing() + "ms",
                    "&7World: &f" + online.getWorld().getName(),
                    "&7Health: &c" + String.format("%.1f", online.getHealth()),
                    "",
                    "&a&lClick to teleport",
                    "&c&lShift-click to kick"));
        }
        return items;
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null || item.getType() != Material.PLAYER_HEAD) return;

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                    && TextUtil.stripColor(item.getItemMeta().getDisplayName()).contains(online.getName())) {

                if (player.isSneaking()) {
                    // Kick
                    online.kickPlayer(TextUtil.colorize("&cKicked by admin."));
                    player.sendMessage(TextUtil.colorize("&aKicked &e" + online.getName()));
                    plugin.getAuditManager().log(player, "KICK", online.getName(), "Kicked via active players");
                    refresh();
                } else {
                    // Teleport
                    player.teleport(online.getLocation());
                    player.sendMessage(TextUtil.colorize("&aTeleported to &e" + online.getName()));
                }
                return;
            }
        }
    }

    @Override
    public void onBackClick() {
        new ServerMenu(plugin, player).open();
    }

    @Override
    public String getMenuTitle() {
        return "&0&lActive Players";
    }
}
