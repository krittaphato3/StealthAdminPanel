package com.adminpanel.gui.monitoring;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.MainMenu;
import com.adminpanel.gui.base.PaginationGUI;
import com.adminpanel.util.DurationParser;
import com.adminpanel.util.ItemBuilder;
import com.adminpanel.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Admin audit log — paginated, searchable trail of all admin actions.
 */
public class AuditLogMenu extends PaginationGUI {

    private final List<Map<String, Object>> logEntries;

    public AuditLogMenu(AdminPanel plugin, Player player) {
        super(plugin, player, "&0&lAudit Log");
        this.logEntries = loadLogs();
    }

    @Override
    protected List<ItemStack> getPageItems() {
        List<ItemStack> items = new ArrayList<>();

        for (Map<String, Object> entry : logEntries) {
            String admin = (String) entry.get("admin_name");
            String action = (String) entry.get("action");
            String target = (String) entry.get("target");
            String details = (String) entry.get("details");
            long timestamp = ((Number) entry.get("timestamp")).longValue();

            Material icon = switch (action.split("_")[0]) {
                case "BAN", "UNBAN" -> Material.RED_WOOL;
                case "MUTE", "UNMUTE" -> Material.ORANGE_WOOL;
                case "WARN" -> Material.YELLOW_WOOL;
                case "KICK" -> Material.IRON_BOOTS;
                case "TELEPORT" -> Material.ENDER_PEARL;
                case "ITEM" -> Material.DIAMOND;
                case "RANK" -> Material.NAME_TAG;
                case "ECONOMY" -> Material.EMERALD;
                case "WHITELIST" -> Material.PAPER;
                case "CHAT", "FILTER" -> Material.BOOK;
                case "TROLL" -> Material.BLAZE_ROD;
                case "NOTE" -> Material.BOOKSHELF;
                default -> Material.PAPER;
            };

            items.add(new ItemBuilder(icon)
                    .name("&e" + action + " &7→ &f" + (target != null ? target : ""))
                    .lore(
                            "&7Admin: &a" + admin,
                            "&7Details: &f" + (details != null ? details : "None"),
                            "&7Time: &f" + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                    .format(new java.util.Date(timestamp))
                    )
                    .build());
        }

        if (items.isEmpty()) {
            items.add(new ItemBuilder(Material.BARRIER)
                    .name("&7&lNo audit entries")
                    .lore("&7No admin actions have been logged yet.")
                    .build());
        }

        return items;
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        // Read-only log view
    }

    @Override
    public void onBackClick() {
        new MainMenu(plugin, player).open();
    }

    @Override
    public String getMenuTitle() {
        return "&0&lAudit Log";
    }

    private List<Map<String, Object>> loadLogs() {
        return plugin.getAuditManager().getLog(0, 200);
    }
}
