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
 * Warn menu — select a player to issue a warning strike.
 * Auto-bans after configurable threshold.
 */
public class WarnMenu extends PaginationGUI {

    public WarnMenu(AdminPanel plugin, Player player) {
        super(plugin, player, "&0&lWarn Player");
    }

    @Override
    protected List<ItemStack> getPageItems() {
        List<ItemStack> items = new ArrayList<>();
        int threshold = plugin.getConfig().getInt("punishment.auto-ban-after-warns", 3);

        for (Player online : Bukkit.getOnlinePlayers()) {
            int warns = plugin.getDataManager().countWarnings(online.getUniqueId().toString());
            String warnColor = warns == 0 ? "&a" : warns < threshold ? "&e" : "&c";

            items.add(HeadUtil.getHead(online.getName(),
                    "&e&l" + online.getName(),
                    "&7Warnings: " + warnColor + warns + "&7/&e" + threshold,
                    "",
                    warns >= threshold ? "&c&l⚠ AT THRESHOLD" : "&e&lClick to warn"));
        }
        return items;
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null || item.getType() != Material.PLAYER_HEAD) return;

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                    && TextUtil.stripColor(item.getItemMeta().getDisplayName()).contains(online.getName())) {
                player.closeInventory();
                new AnvilGUIBridge(plugin).openReasonInput(player, "Rule violation", reason -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        int result = plugin.getPunishmentManager().warnPlayer(
                                online.getName(), reason, player.getName());
                        if (result == -1) {
                            plugin.getAuditManager().log(player, "WARN_AUTO_BAN", online.getName(),
                                    "Auto-banned after threshold. Reason: " + reason);
                            player.sendMessage(TextUtil.colorize(
                                    "&cAuto-banned &e" + online.getName() + " &cafter reaching warning threshold!"));
                        } else {
                            plugin.getAuditManager().log(player, "WARN", online.getName(),
                                    "Warning #" + result + ". Reason: " + reason);
                            player.sendMessage(TextUtil.colorize(
                                    "&eWarned &c" + online.getName() + " &e(Warning #" + result + ")"));
                        }
                    });
                });
                return;
            }
        }
    }

    @Override
    public void onBackClick() {
        new PunishmentMenu(plugin, player).open();
    }

    @Override
    public String getMenuTitle() {
        return "&0&lWarn Player";
    }
}
