package com.adminpanel.gui.chat;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.base.PaginationGUI;
import com.adminpanel.util.ColorUtil;
import com.adminpanel.util.HeadUtil;
import com.adminpanel.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Paginated list of muted players.
 * Click to unmute.
 */
public class MuteListMenu extends PaginationGUI {

    public MuteListMenu(AdminPanel plugin, Player player) {
        super(plugin, player, "&0&lMuted Players");
    }

    @Override
    protected List<ItemStack> getPageItems() {
        List<ItemStack> items = new ArrayList<>();
        for (UUID mutedUUID : plugin.getChatManager().getMutedPlayers()) {
            Player muted = Bukkit.getPlayer(mutedUUID);
            String name = muted != null ? muted.getName() : mutedUUID.toString().substring(0, 8);

            items.add(HeadUtil.getHead(name,
                    "&c&l" + name,
                    "&7Status: &cMUTED",
                    muted != null ? "&7Online: &aYes" : "&7Online: &cNo",
                    "",
                    "&a&lClick to unmute"));
        }

        if (items.isEmpty()) {
            items.add(new ItemBuilder(Material.LIME_DYE)
                    .name("&a&lNo muted players")
                    .lore("&7No one is currently muted.")
                    .build());
        }

        return items;
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null) return;

        // Find the muted player
        for (UUID mutedUUID : plugin.getChatManager().getMutedPlayers()) {
            Player muted = Bukkit.getPlayer(mutedUUID);
            String name = muted != null ? muted.getName() : mutedUUID.toString().substring(0, 8);

            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                    && TextUtil.stripColor(item.getItemMeta().getDisplayName()).contains(name)) {
                plugin.getPunishmentManager().unmutePlayer(name);
                plugin.getAuditManager().log(player, "UNMUTE", name, "Unmuted via panel");
                player.sendMessage(TextUtil.colorize("&aUnmuted &e" + name));
                refresh();
                return;
            }
        }
    }

    @Override
    public String getMenuTitle() {
        return "&0&lMuted Players";
    }
}
