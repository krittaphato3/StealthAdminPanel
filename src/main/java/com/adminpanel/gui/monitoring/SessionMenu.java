package com.adminpanel.gui.monitoring;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.base.PaginationGUI;
import com.adminpanel.hooks.AnvilGUIBridge;
import com.adminpanel.util.DurationParser;
import com.adminpanel.util.HeadUtil;
import com.adminpanel.util.ItemBuilder;
import com.adminpanel.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Player session history — shows join/leave times, playtime, IP.
 */
public class SessionMenu extends PaginationGUI {

    private final OfflinePlayer target;

    public SessionMenu(AdminPanel plugin, Player player, OfflinePlayer target) {
        super(plugin, player, "&0&lSession History");
        this.target = target;
    }

    public SessionMenu(AdminPanel plugin, Player player) {
        this(plugin, player, (OfflinePlayer) player);
    }

    @Override
    protected List<ItemStack> getPageItems() {
        List<ItemStack> items = new ArrayList<>();

        // Info header
        long playtime = plugin.getSessionManager().getPlaytime(target.getUniqueId());
        String lastIP = plugin.getSessionManager().getLastIP(target.getUniqueId());

        items.add(new ItemBuilder(Material.PAPER)
                .name("&e&l" + (target.getName() != null ? target.getName() : "Unknown"))
                .lore(
                        "&7UUID: &f" + target.getUniqueId(),
                        "&7Last IP: &f" + lastIP,
                        "&7Total Playtime: &a" + DurationParser.format(playtime),
                        "&7Online: " + (target.isOnline() ? "&aYes" : "&cNo")
                )
                .build());

        // Session entries
        List<Map<String, Object>> sessions = plugin.getSessionManager().getSessions(target.getUniqueId());
        for (Map<String, Object> session : sessions) {
            long joinTime = ((Number) session.get("join_time")).longValue();
            long leaveTime = session.get("leave_time") != null ? ((Number) session.get("leave_time")).longValue() : 0;
            String ip = (String) session.get("ip");

            String status = leaveTime == 0 ? "&aOnline" : "&cLeft";
            long duration = leaveTime == 0 ? System.currentTimeMillis() - joinTime : leaveTime - joinTime;

            items.add(new ItemBuilder(Material.BOOK)
                    .name("&7Session &f" + new java.text.SimpleDateFormat("MM/dd HH:mm")
                            .format(new java.util.Date(joinTime)))
                    .lore(
                            "&7Duration: &e" + DurationParser.format(duration),
                            "&7IP: &f" + (ip != null ? ip : "Unknown"),
                            "&7Status: " + status
                    )
                    .build());
        }

        if (sessions.isEmpty() && items.size() == 1) {
            items.add(new ItemBuilder(Material.BARRIER)
                    .name("&7&lNo session data")
                    .build());
        }

        return items;
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        // Read-only
    }

    @Override
    public String getMenuTitle() {
        return "&0&lSession History";
    }
}
