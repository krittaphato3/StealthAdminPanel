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
 * Mute menu — select a player to mute, then choose reason and duration.
 */
public class MuteMenu extends PaginationGUI {

    public MuteMenu(AdminPanel plugin, Player player) {
        super(plugin, player, "&0&lMute Player");
    }

    @Override
    protected List<ItemStack> getPageItems() {
        List<ItemStack> items = new ArrayList<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            boolean isMuted = plugin.getChatManager().isMuted(online.getUniqueId());
            items.add(HeadUtil.getHead(online.getName(),
                    (isMuted ? "&6&l" : "&e&l") + online.getName(),
                    "&7Ping: &f" + online.getPing() + "ms",
                    "&7Status: " + (isMuted ? "&cMUTED" : "&aActive"),
                    "",
                    "&e&lClick to mute/unmute"));
        }
        return items;
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null || item.getType() != Material.PLAYER_HEAD) return;

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                    && TextUtil.stripColor(item.getItemMeta().getDisplayName()).contains(online.getName())) {
                if (plugin.getChatManager().isMuted(online.getUniqueId())) {
                    // Unmute
                    plugin.getPunishmentManager().unmutePlayer(online.getName());
                    plugin.getAuditManager().log(player, "UNMUTE", online.getName(), "Unmuted via panel");
                    player.sendMessage(TextUtil.colorize("&aUnmuted &e" + online.getName()));
                    refresh();
                } else {
                    // Mute with reason + duration
                    openMuteConfirm(player, online);
                }
                return;
            }
        }
    }

    private void openMuteConfirm(Player admin, Player target) {
        admin.closeInventory();
        new AnvilGUIBridge(plugin).openReasonInput(admin, "Spam", reason -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                new AnvilGUIBridge(plugin).openDurationInput(admin, durationStr -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        long duration = com.adminpanel.util.DurationParser.parse(durationStr);
                        plugin.getPunishmentManager().mutePlayer(
                                target.getName(), reason, duration, admin.getName());
                        plugin.getAuditManager().log(admin, "MUTE", target.getName(),
                                "Reason: " + reason + ", Duration: " + durationStr);
                        admin.sendMessage(TextUtil.colorize(
                                "&aMuted &e" + target.getName() + " &afor &e" + reason));
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
        return "&0&lMute Player";
    }
}
