package com.adminpanel.gui.economy;

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
 * Paginated balance view — shows all online players with their balances.
 * Click to give/take money.
 */
public class BalanceMenu extends PaginationGUI {

    public BalanceMenu(AdminPanel plugin, Player player) {
        super(plugin, player, "&0&lPlayer Balances");
    }

    @Override
    protected List<ItemStack> getPageItems() {
        List<ItemStack> items = new ArrayList<>();
        if (plugin.getEconomyManager() == null) return items;

        for (Player online : Bukkit.getOnlinePlayers()) {
            double balance = plugin.getEconomyManager().getBalance(online);
            String formatted = plugin.getEconomyManager().formatBalance(balance);

            items.add(HeadUtil.getHead(online.getName(),
                    "&6&l" + online.getName(),
                    "&7Balance: &a" + formatted,
                    "",
                    "&a&lClick to give money",
                    "&c&lShift-click to take money"));
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
                    // Shift-click: Take money
                    player.closeInventory();
                    new AnvilGUIBridge(plugin).openNumberInput(player, "Amount to Take", "100", amount -> {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            if (plugin.getEconomyManager().withdraw(online, amount)) {
                                plugin.getEconomyManager().deposit(player, amount);
                                player.sendMessage(TextUtil.colorize(
                                        "&cTook &e$" + amount + " &cfrom &e" + online.getName()));
                                plugin.getAuditManager().log(player, "ECONOMY_TAKE", online.getName(),
                                        "Took $" + amount);
                            } else {
                                player.sendMessage(TextUtil.colorize("&cNot enough money!"));
                            }
                        });
                    });
                } else {
                    // Normal click: Give money
                    player.closeInventory();
                    new AnvilGUIBridge(plugin).openNumberInput(player, "Amount to Give", "100", amount -> {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            if (plugin.getEconomyManager().deposit(online, amount)) {
                                player.sendMessage(TextUtil.colorize(
                                        "&aGave &e$" + amount + " &ato &e" + online.getName()));
                                plugin.getAuditManager().log(player, "ECONOMY_GIVE", online.getName(),
                                        "Gave $" + amount);
                            }
                        });
                    });
                }
                return;
            }
        }
    }

    @Override
    public void onBackClick() {
        new EconomyMenu(plugin, player).open();
    }

    @Override
    public String getMenuTitle() {
        return "&0&lPlayer Balances";
    }
}
