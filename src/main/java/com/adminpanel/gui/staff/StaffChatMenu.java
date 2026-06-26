package com.adminpanel.gui.staff;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.base.SubMenu;
import com.adminpanel.util.ColorUtil;
import com.adminpanel.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Staff chat toggle menu — enable/disable staff-only chat mode.
 */
public class StaffChatMenu extends SubMenu {

    public StaffChatMenu(AdminPanel plugin, Player player) {
        super(plugin, player, "&0&lStaff Chat", 3);
    }

    @Override
    protected void buildMenu() {
        boolean isOn = plugin.getChatManager().isStaffChatToggled(player.getUniqueId());

        setItem(13, isOn ? Material.LIME_WOOL : Material.RED_WOOL,
                isOn ? "&a&l✔ Staff Chat: ON" : "&c&l✘ Staff Chat: OFF",
                "&7Toggle staff-only chat mode",
                "&7When ON, your messages go to",
                "&7staff chat only (not logged in console)",
                "",
                "&7Status: " + ColorUtil.stateIndicator(isOn));

        addBackButton();
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (slot == 13) {
            boolean toggled = plugin.getChatManager().toggleStaffChat(player.getUniqueId());
            player.sendMessage(TextUtil.colorize(toggled ?
                    "&aStaff chat ENABLED" : "&cStaff chat DISABLED"));
            refresh();
        } else if (slot == getBackSlot()) {
            new StaffListMenu(plugin, player).open();
        }
    }
}
