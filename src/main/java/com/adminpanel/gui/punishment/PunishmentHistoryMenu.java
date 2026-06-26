package com.adminpanel.gui.punishment;

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
 * Punishment history — paginated list of all past punishments.
 * Searchable via AnvilGUI.
 */
public class PunishmentHistoryMenu extends PaginationGUI {

    private final List<Map<String, Object>> punishments;

    public PunishmentHistoryMenu(AdminPanel plugin, Player player) {
        super(plugin, player, "&0&lPunishment History");
        this.punishments = loadAllPunishments();
    }

    @Override
    protected List<ItemStack> getPageItems() {
        List<ItemStack> items = new ArrayList<>();

        for (Map<String, Object> p : punishments) {
            String type = (String) p.get("type");
            String target = (String) p.get("target_name");
            String reason = (String) p.get("reason");
            String issuer = (String) p.get("issuer_name");
            long duration = p.get("duration") != null ? ((Number) p.get("duration")).longValue() : -1;
            long createdAt = ((Number) p.get("created_at")).longValue();
            boolean active = p.get("active") != null && ((Number) p.get("active")).intValue() == 1;

            String typeColor = switch (type.toLowerCase()) {
                case "ban" -> "&c";
                case "mute" -> "&6";
                case "warn" -> "&e";
                default -> "&7";
            };
            String statusColor = active ? "&aActive" : "&cExpired";

            Material icon = switch (type.toLowerCase()) {
                case "ban" -> Material.RED_WOOL;
                case "mute" -> Material.ORANGE_WOOL;
                case "warn" -> Material.YELLOW_WOOL;
                default -> Material.PAPER;
            };

            items.add(new ItemBuilder(icon)
                    .name(typeColor + "&l" + type.toUpperCase() + " &7→ &e" + target)
                    .lore(
                            "&7Reason: &f" + (reason != null ? reason : "None"),
                            "&7Issued by: &f" + (issuer != null ? issuer : "Console"),
                            "&7Duration: " + DurationParser.formatColored(duration),
                            "&7Status: " + statusColor,
                            "&7Date: &f" + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
                                    .format(new java.util.Date(createdAt))
                    )
                    .build());
        }

        // If no punishments, show empty message
        if (items.isEmpty()) {
            items.add(new ItemBuilder(Material.BARRIER)
                    .name("&c&lNo punishments found")
                    .lore("&7The punishment log is empty.")
                    .build());
        }

        return items;
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        // No action on individual punishment items (read-only view)
    }

    @Override
    public String getMenuTitle() {
        return "&0&lPunishment History";
    }

    private List<Map<String, Object>> loadAllPunishments() {
        // Load all punishments from database
        return plugin.getDataManager().getAllPunishments(0, 500);
    }
}
