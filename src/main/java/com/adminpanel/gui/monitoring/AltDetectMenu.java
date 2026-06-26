package com.adminpanel.gui.monitoring;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.base.PaginationGUI;
import com.adminpanel.util.HeadUtil;
import com.adminpanel.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Alt detection — find accounts sharing the same IP address.
 */
public class AltDetectMenu extends PaginationGUI {

    public AltDetectMenu(AdminPanel plugin, Player player) {
        super(plugin, player, "&0&lAlt Detection");
    }

    @Override
    protected List<ItemStack> getPageItems() {
        List<ItemStack> items = new ArrayList<>();

        // Build IP → players map from online players
        Map<String, List<String>> ipToPlayers = new HashMap<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            String ip = online.getAddress() != null ?
                    online.getAddress().getAddress().getHostAddress() : "Unknown";
            ipToPlayers.computeIfAbsent(ip, k -> new ArrayList<>()).add(online.getName());
        }

        // Show IPs with multiple accounts
        for (Map.Entry<String, List<String>> entry : ipToPlayers.entrySet()) {
            if (entry.getValue().size() > 1) {
                StringBuilder names = new StringBuilder();
                for (String name : entry.getValue()) {
                    names.append("&7• &e").append(name).append("\n");
                }

                items.add(new ItemBuilder(Material.RED_WOOL)
                        .name("&c&l⚠ Alt Accounts Detected")
                        .lore(
                                "&7IP: &f" + entry.getKey(),
                                "&7Players:",
                                names.toString().split("\n")
                        )
                        .build());
            }
        }

        // Show all players with their IPs
        for (Player online : Bukkit.getOnlinePlayers()) {
            String ip = online.getAddress() != null ?
                    online.getAddress().getAddress().getHostAddress() : "Unknown";

            items.add(HeadUtil.getHead(online.getName(),
                    "&e&l" + online.getName(),
                    "&7IP: &f" + ip,
                    "&7Alts: &f" + ipToPlayers.getOrDefault(ip, List.of()).size()
            ));
        }

        if (items.isEmpty()) {
            items.add(new ItemBuilder(Material.BARRIER)
                    .name("&7&lNo data available")
                    .build());
        }

        return items;
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        // Read-only view
    }

    @Override
    public String getMenuTitle() {
        return "&0&lAlt Detection";
    }
}
