package com.adminpanel.gui.economy;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.base.SubMenu;
import com.adminpanel.util.ColorUtil;
import com.adminpanel.util.ItemBuilder;
import com.adminpanel.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Economy leaderboard — top balances on the server.
 */
public class LeaderboardMenu extends SubMenu {

    public LeaderboardMenu(AdminPanel plugin, Player player) {
        super(plugin, player, "&0&lEconomy Leaderboard", 5);
    }

    @Override
    protected void buildMenu() {
        if (plugin.getEconomyManager() == null || !plugin.getEconomyManager().isAvailable()) {
            setItem(22, Material.BARRIER,
                    "&c&lVault Not Available",
                    "&7Economy features require Vault");
            addBackButton();
            return;
        }

        Map<String, Double> leaderboard = plugin.getEconomyManager().getLeaderboard(45);

        int slot = 0;
        int rank = 1;
        for (Map.Entry<String, Double> entry : leaderboard.entrySet()) {
            if (slot >= 45) break;

            String medal = switch (rank) {
                case 1 -> "🥇 ";
                case 2 -> "🥈 ";
                case 3 -> "🥉 ";
                default -> "#" + rank + " ";
            };

            Material icon = rank <= 3 ? Material.GOLD_INGOT : Material.IRON_INGOT;

            setItem(slot, icon,
                    "&e" + medal + entry.getKey(),
                    "&7Balance: &a" + plugin.getEconomyManager().formatBalance(entry.getValue()));

            rank++;
            slot++;
        }

        if (leaderboard.isEmpty()) {
            setItem(22, Material.BARRIER,
                    "&c&lNo Data",
                    "&7No player balances available");
        }

        addBackButton();
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        // Read-only leaderboard
    }

    @Override
    protected void onBackClick() {
        new EconomyMenu(plugin, player).open();
    }
}
