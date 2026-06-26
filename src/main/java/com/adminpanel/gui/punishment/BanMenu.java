package com.adminpanel.gui.punishment;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.base.PaginationGUI;
import com.adminpanel.hooks.AnvilGUIBridge;
import com.adminpanel.util.HeadUtil;
import com.adminpanel.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Ban menu — select a player to ban, then choose reason and duration.
 * Paginated list of online players.
 */
public class BanMenu extends PaginationGUI {

    public BanMenu(AdminPanel plugin, Player player) {
        super(plugin, player, "&0&lBan Player");
    }

    @Override
    protected List<ItemStack> getPageItems() {
        List<ItemStack> items = new ArrayList<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            items.add(HeadUtil.getHead(online.getName(),
                    "&c&l" + online.getName(),
                    "&7Ping: &f" + online.getPing() + "ms",
                    "",
                    "&c&lClick to ban this player"));
        }
        return items;
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null || item.getType() != Material.PLAYER_HEAD) return;

        // Find the target player
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                    && TextUtil.stripColor(item.getItemMeta().getDisplayName()).contains(online.getName())) {
                openBanConfirm(player, online);
                return;
            }
        }
    }

    private void openBanConfirm(Player admin, Player target) {
        admin.closeInventory();

        // Ask for reason first
        new AnvilGUIBridge(plugin).openReasonInput(admin, "Hacked", reason -> {
            // Then ask for duration
            Bukkit.getScheduler().runTask(plugin, () -> {
                new AnvilGUIBridge(plugin).openDurationInput(admin, durationStr -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        long duration = com.adminpanel.util.DurationParser.parse(durationStr);
                        plugin.getPunishmentManager().banPlayer(
                                target.getName(), reason, duration, admin.getName());
                        plugin.getAuditManager().log(admin, "BAN", target.getName(),
                                "Reason: " + reason + ", Duration: " + durationStr);
                        admin.sendMessage(TextUtil.colorize(
                                "&aBanned &e" + target.getName() + " &afor &e" + reason));
                    });
                });
            });
        });
    }

    @Override
    public void onBackClick() {
        new PunishmentMenu(plugin, player).open();
    }

    @Override
    public String getMenuTitle() {
        return "&0&lBan Player";
    }
}
