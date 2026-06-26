package com.adminpanel.gui.economy;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.MainMenu;
import com.adminpanel.gui.base.SubMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Economy hub — routes to balance view, leaderboard, and give/take.
 */
public class EconomyMenu extends SubMenu {

    public EconomyMenu(AdminPanel plugin, Player player) {
        super(plugin, player, "&0&lEconomy", 3);
    }

    @Override
    protected void buildMenu() {
        setItem(10, Material.EMERALD,
                "&a&lView Balances",
                "&7See all player balances",
                "&7Paginated & searchable");

        setItem(13, Material.NETHER_STAR,
                "&e&lLeaderboard",
                "&7Top balances on the server");

        setItem(16, Material.GOLD_INGOT,
                "&6&lGive / Take",
                "&7Modify player balances",
                "&7Click a player to manage");

        if (plugin.getEconomyManager() == null || !plugin.getEconomyManager().isAvailable()) {
            setItem(22, Material.BARRIER,
                    "&c&lVault Not Available",
                    "&7Economy features require Vault");
        }

        addBackButton();
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null || item.getType() == Material.AIR) return;

        if (plugin.getEconomyManager() == null || !plugin.getEconomyManager().isAvailable()) {
            player.sendMessage(com.adminpanel.util.TextUtil.colorize("&cVault economy not available!"));
            return;
        }

        switch (slot) {
            case 10 -> new BalanceMenu(plugin, player).open();
            case 13 -> new LeaderboardMenu(plugin, player).open();
            case 45 -> new MainMenu(plugin, player).open();
        }
    }
}
