package com.adminpanel.gui.server;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.MainMenu;
import com.adminpanel.gui.base.SubMenu;
import com.adminpanel.util.ColorUtil;
import com.adminpanel.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Server management hub — whitelist, ban list, active players.
 */
public class ServerMenu extends SubMenu {

    public ServerMenu(AdminPanel plugin, Player player) {
        super(plugin, player, "&0&lServer Management", 3);
    }

    @Override
    protected void buildMenu() {
        boolean wlEnabled = Bukkit.hasWhitelist();
        setItem(10, wlEnabled ? Material.LIME_WOOL : Material.RED_WOOL,
                ColorUtil.toggleName("Whitelist", wlEnabled),
                "&7Status: " + ColorUtil.stateIndicator(wlEnabled),
                "&7Click to toggle");

        setItem(12, Material.IRON_BARS,
                "&c&lBan List",
                "&7View & manage banned players",
                "&7Paginated & searchable");

        int online = Bukkit.getOnlinePlayers().size();
        setItem(14, Material.PLAYER_HEAD,
                "&e&lActive Players",
                "&7Online: &f" + online,
                "&7Click to manage (kick/tp)");

        setItem(16, Material.REDSTONE_BLOCK,
                "&4&lServer Actions",
                "&7Global announcements",
                "&7Broadcast messages");

        addBackButton();
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null || item.getType() == Material.AIR) return;

        switch (slot) {
            case 10 -> {
                // Toggle whitelist
                boolean newState = !Bukkit.hasWhitelist();
                Bukkit.setWhitelist(newState);
                player.sendMessage(TextUtil.colorize(newState ? "&aWhitelist ENABLED" : "&cWhitelist DISABLED"));
                plugin.getAuditManager().log(player, "WHITELIST", "Server",
                        newState ? "Enabled" : "Disabled");
                refresh();
            }
            case 12 -> new BanListMenu(plugin, player).open();
            case 14 -> new ActivePlayersMenu(plugin, player).open();
        }
    }

    @Override
    protected void onBackClick() {
        new MainMenu(plugin, player).open();
    }
}
