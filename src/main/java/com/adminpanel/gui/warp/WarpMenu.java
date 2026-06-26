package com.adminpanel.gui.warp;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.MainMenu;
import com.adminpanel.gui.base.PaginationGUI;
import com.adminpanel.hooks.AnvilGUIBridge;
import com.adminpanel.util.ItemBuilder;
import com.adminpanel.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Warp management — create, delete, teleport to warps.
 */
public class WarpMenu extends PaginationGUI {

    public WarpMenu(AdminPanel plugin, Player player) {
        super(plugin, player, "&0&lWarps");
    }

    @Override
    protected List<ItemStack> getPageItems() {
        List<ItemStack> items = new ArrayList<>();

        // Create warp button
        items.add(new ItemBuilder(Material.ENDER_PEARL)
                .name("&a&l+ Create Warp")
                .lore("&7Create a warp at your current location")
                .build());

        // List warps
        List<Map<String, Object>> warps = plugin.getWarpManager().getAllWarps();
        for (Map<String, Object> warp : warps) {
            String name = (String) warp.get("name");
            String world = (String) warp.get("world");
            double x = ((Number) warp.get("x")).doubleValue();
            double y = ((Number) warp.get("y")).doubleValue();
            double z = ((Number) warp.get("z")).doubleValue();
            String creator = (String) warp.get("creator");

            items.add(new ItemBuilder(Material.ENDER_EYE)
                    .name("&5&l" + name)
                    .lore(
                            "&7World: &f" + world,
                            "&7X: &f" + String.format("%.1f", x),
                            "&7Y: &f" + String.format("%.1f", y),
                            "&7Z: &f" + String.format("%.1f", z),
                            "&7Created by: &f" + creator,
                            "",
                            "&a&lClick to teleport",
                            "&c&lShift-click to delete")
                    .build());
        }

        return items;
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null || item.getType() == Material.AIR) return;

        if (item.getType() == Material.ENDER_PEARL) {
            // Create warp
            player.closeInventory();
            new AnvilGUIBridge(plugin).openWarpNameInput(player, name -> {
                org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                    if (plugin.getWarpManager().createWarp(name, player)) {
                        player.sendMessage(TextUtil.colorize("&aWarp &e" + name + " &acreated!"));
                        plugin.getAuditManager().log(player, "WARP_CREATE", name,
                                "Created at " + player.getLocation().getBlockX() + ", "
                                        + player.getLocation().getBlockY() + ", "
                                        + player.getLocation().getBlockZ());
                    } else {
                        player.sendMessage(TextUtil.colorize("&cFailed to create warp."));
                    }
                    refresh();
                });
            });
            return;
        }

        if (item.getType() == Material.ENDER_EYE && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String warpName = TextUtil.stripColor(item.getItemMeta().getDisplayName());

            if (player.isSneaking()) {
                // Delete warp
                plugin.getWarpManager().deleteWarp(warpName);
                player.sendMessage(TextUtil.colorize("&cWarp &e" + warpName + " &cdeleted"));
                plugin.getAuditManager().log(player, "WARP_DELETE", warpName, "Deleted");
                refresh();
            } else {
                // Teleport to warp
                if (plugin.getWarpManager().teleportTo(player, warpName)) {
                    player.sendMessage(TextUtil.colorize("&aTeleported to warp &e" + warpName));
                } else {
                    player.sendMessage(TextUtil.colorize("&cWarp not found or world unloaded."));
                }
            }
        }
    }

    @Override
    public String getMenuTitle() {
        return "&0&lWarps";
    }
}
