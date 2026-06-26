package com.adminpanel.gui.punishment;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.MainMenu;
import com.adminpanel.gui.base.SubMenu;
import com.adminpanel.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Punishment hub menu — routes to ban, mute, warn, and history.
 */
public class PunishmentMenu extends SubMenu {

    public PunishmentMenu(AdminPanel plugin, Player player) {
        super(plugin, player, "&0&lPunishment Hub", 3);
    }

    @Override
    protected void buildMenu() {
        setItem(10, Material.RED_WOOL,
                "&c&lBan Player",
                "&7Issue a temporary or permanent ban",
                "&7Dispatched via console for stealth");

        setItem(12, Material.ORANGE_WOOL,
                "&6&lMute Player",
                "&7Mute a player in chat",
                "&7Tracked in plugin database");

        setItem(14, Material.YELLOW_WOOL,
                "&e&lWarn Player",
                "&7Issue a warning strike",
                "&7Auto-bans after configurable threshold");

        setItem(16, Material.BOOK,
                "&9&lPunishment History",
                "&7View all past punishments",
                "&7Paginated & searchable");

        addBackButton();
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null || item.getType() == Material.AIR) return;

        switch (slot) {
            case 10 -> new BanMenu(plugin, player).open();
            case 12 -> new MuteMenu(plugin, player).open();
            case 14 -> new WarnMenu(plugin, player).open();
            case 16 -> new PunishmentHistoryMenu(plugin, player).open();
            case 45 -> new MainMenu(plugin, player).open();
        }
    }
}
