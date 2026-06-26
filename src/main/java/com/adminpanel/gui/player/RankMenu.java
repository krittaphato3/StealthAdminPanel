package com.adminpanel.gui.player;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.MainMenu;
import com.adminpanel.gui.base.SubMenu;
import com.adminpanel.gui.base.ConfirmDialog;
import com.adminpanel.hooks.AnvilGUIBridge;
import com.adminpanel.util.ItemBuilder;
import com.adminpanel.util.SoundUtil;
import com.adminpanel.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Rank management menu for a selected player.
 * Shows current rank, quick ranks from Vault, custom input, and reset.
 */
public class RankMenu extends SubMenu {

    private final Player target;

    public RankMenu(AdminPanel plugin, Player player, Player target) {
        super(plugin, player, "&0&lRank: " + target.getName(), 4);
        this.target = target;
    }

    @Override
    protected void buildMenu() {
        // Current rank display
        setItem(4, Material.NAME_TAG,
                "&e&lCurrent Rank",
                "&7" + target.getName() + "'s rank: &f" + getCurrentRank());

        // Custom rank input
        setItem(10, Material.EMERALD,
                "&a&lSet Custom Rank",
                "&7Type a rank name via chat input");

        // Quick ranks from Vault
        int slot = 19;
        if (plugin.getVaultHook().hasPermissions()) {
            try {
                String[] groups = plugin.getVaultHook().getPermission().getGroups();
                for (String group : groups) {
                    if (slot >= 27) break; // Max 8 quick ranks (row 3)
                    boolean isCurrent = group.equalsIgnoreCase(getCurrentRank());
                    Material icon = isCurrent ? Material.LIME_DYE : Material.PAPER;
                    String prefix = isCurrent ? "&a✔ " : "";

                    setItem(slot, icon,
                            prefix + "&e" + group,
                            isCurrent ? "&7Current rank" : "&7Click to set this rank");
                    slot++;
                }
            } catch (Exception e) {
                setItem(19, Material.BARRIER,
                        "&c&lNo groups found",
                        "&7Could not load Vault groups");
            }
        } else {
            setItem(19, Material.BARRIER,
                    "&c&lVault not available",
                    "&7Vault permissions not installed");
        }

        // Reset to default
        setItem(16, Material.BARRIER,
                "&c&lReset to Default",
                "&7Remove all rank groups");

        addBackButton();
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null || item.getType() == Material.AIR) return;

        switch (slot) {
            case 10 -> {
                // Custom rank via chat input
                player.closeInventory();
                new AnvilGUIBridge(plugin).openRankInput(player, rank -> {
                    if (plugin.getVaultHook().hasPermissions()) {
                        plugin.getVaultHook().setGroup(target, target.getWorld().getName(), rank);
                        SoundUtil.playSuccess(player);
                        player.sendMessage(TextUtil.colorize(
                                "&aSet " + target.getName() + "'s rank to &e" + rank));
                        plugin.getAuditManager().log(player, "RANK_SET", target.getName(),
                                "Rank: " + rank);
                    } else {
                        SoundUtil.playError(player);
                        player.sendMessage(TextUtil.colorize("&cVault permissions not available!"));
                    }
                });
            }
            case 16 -> {
                // Reset to default — confirm
                new ConfirmDialog(plugin, player,
                        "Reset Rank",
                        "&7Reset &e" + target.getName() + "&7's rank to &cdefault&7?",
                        () -> {
                            if (plugin.getVaultHook().hasPermissions()) {
                                plugin.getVaultHook().setGroup(target, target.getWorld().getName(), "default");
                                SoundUtil.playSuccess(player);
                                player.sendMessage(TextUtil.colorize(
                                        "&aReset " + target.getName() + "'s rank to default"));
                                plugin.getAuditManager().log(player, "RANK_RESET", target.getName(), "Reset to default");
                            }
                        },
                        null
                ).open();
            }
            default -> {
                // Quick rank buttons (slots 19-26)
                if (slot >= 19 && slot <= 26 && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                    String rankName = TextUtil.stripColor(item.getItemMeta().getDisplayName())
                            .replace("✔ ", "").trim();
                    if (!rankName.isEmpty() && !rankName.equals("No groups found")
                            && !rankName.equals("Vault not available")) {
                        if (plugin.getVaultHook().hasPermissions()) {
                            plugin.getVaultHook().setGroup(target, target.getWorld().getName(), rankName);
                            SoundUtil.playSuccess(player);
                            player.sendMessage(TextUtil.colorize(
                                    "&aSet " + target.getName() + "'s rank to &e" + rankName));
                            plugin.getAuditManager().log(player, "RANK_SET", target.getName(),
                                    "Rank: " + rankName);
                            refresh();
                        }
                    }
                }
            }
        }
    }

    private String getCurrentRank() {
        if (plugin.getVaultHook().hasPermissions()) {
            return plugin.getVaultHook().getGroup(target, target.getWorld().getName());
        }
        return "Unknown";
    }
}
