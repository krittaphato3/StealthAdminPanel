package com.adminpanel.gui.player;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.base.SubMenu;
import com.adminpanel.hooks.AnvilGUIBridge;
import com.adminpanel.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Rank management menu for a selected player.
 * Allows setting ranks via Vault or through AnvilGUI input.
 */
public class RankMenu extends SubMenu {

    private final Player target;

    public RankMenu(AdminPanel plugin, Player player, Player target) {
        super(plugin, player, "&0&lRank: " + target.getName(), 3);
        this.target = target;
    }

    @Override
    protected void buildMenu() {
        setItem(10, Material.NAME_TAG,
                "&e&lSet Custom Rank",
                "&7Type a rank name to set",
                "&7Current rank: &f" + getCurrentRank());

        setItem(13, Material.EMERALD_BLOCK,
                "&a&lQuick Ranks",
                "&7Common rank presets");

        setItem(16, Material.BARRIER,
                "&c&lRemove Rank",
                "&7Reset to default group");

        addBackButton();
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null || item.getType() == Material.AIR) return;

        switch (slot) {
            case 10 -> {
                // Custom rank via AnvilGUI
                player.closeInventory();
                new AnvilGUIBridge(plugin).openRankInput(player, rank -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (plugin.getVaultHook().hasPermissions()) {
                            plugin.getVaultHook().setGroup(target, target.getWorld().getName(), rank);
                            player.sendMessage(TextUtil.colorize(
                                    "&aSet " + target.getName() + "'s rank to &e" + rank));
                            plugin.getAuditManager().log(player, "RANK_SET", target.getName(),
                                    "Rank: " + rank);
                        } else {
                            player.sendMessage(TextUtil.colorize("&cVault permissions not available!"));
                        }
                    });
                });
            }
            case 16 -> {
                // Remove rank (set to default)
                if (plugin.getVaultHook().hasPermissions()) {
                    plugin.getVaultHook().setGroup(target, target.getWorld().getName(), "default");
                    player.sendMessage(TextUtil.colorize(
                            "&aReset " + target.getName() + "'s rank to default"));
                    plugin.getAuditManager().log(player, "RANK_RESET", target.getName(), "Reset to default");
                } else {
                    player.sendMessage(TextUtil.colorize("&cVault permissions not available!"));
                }
            }
        }
    }

    @Override
    protected void onBackClick() {
        new PlayerActionMenu(plugin, player, target).open();
    }

    private String getCurrentRank() {
        if (plugin.getVaultHook().hasPermissions()) {
            return plugin.getVaultHook().getGroup(target, target.getWorld().getName());
        }
        return "Unknown";
    }
}
