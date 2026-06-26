package com.adminpanel.gui;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.base.SubMenu;
import com.adminpanel.gui.economy.EconomyMenu;
import com.adminpanel.gui.item.ItemEditorMenu;
import com.adminpanel.gui.player.PlayerListMenu;
import com.adminpanel.gui.punishment.PunishmentMenu;
import com.adminpanel.gui.server.ServerMenu;
import com.adminpanel.gui.staff.PlayerNotesMenu;
import com.adminpanel.gui.staff.StaffListMenu;
import com.adminpanel.gui.world.WorldMenu;
import com.adminpanel.manager.PermissionManager;
import com.adminpanel.util.ColorUtil;
import com.adminpanel.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Main hub menu — central routing to all sub-menus.
 * Buttons are only rendered if the player has the required permission.
 */
public class MainMenu extends SubMenu {

    public MainMenu(AdminPanel plugin, Player player) {
        super(plugin, player, "&0&lAdmin Panel", 6);
    }

    @Override
    protected void buildMenu() {
        // Row 1: Player Control, Punishment, Economy
        int slot = 10;
        if (PermissionManager.canControlPlayers(player)) {
            setItem(slot, Material.PLAYER_HEAD,
                    "&a&lPlayer Control",
                    "&7Manage online players",
                    "&7• View Inventory",
                    "&7• Give Items",
                    "&7• Troll Options",
                    "&7• Set Rank");
        }

        slot = 13;
        if (PermissionManager.canPunish(player)) {
            setItem(slot, Material.IRON_SWORD,
                    "&c&lPunishments",
                    "&7Ban, Mute, Warn players",
                    "&7• Temporary & Permanent bans",
                    "&7• Mutes with duration",
                    "&7• Warning strike system",
                    "&7• Punishment history");
        }

        slot = 16;
        if (PermissionManager.canUseEconomy(player)) {
            setItem(slot, Material.EMERALD,
                    "&6&lEconomy",
                    "&7Manage player balances",
                    "&7• View balances",
                    "&7• Give / Take money",
                    "&7• Leaderboard");
        }

        // Row 2: Chat, World, Server
        slot = 19;
        if (PermissionManager.canManageChat(player)) {
            setItem(slot, Material.PAPER,
                    "&b&lChat Management",
                    "&7Control server chat",
                    "&7• Global mute",
                    "&7• Slow mode",
                    "&7• Staff chat",
                    "&7• Chat filter");
        }

        slot = 22;
        if (PermissionManager.canManageWorld(player)) {
            setItem(slot, Material.GRASS_BLOCK,
                    "&2&lWorld Configuration",
                    "&7Modify world settings",
                    "&7• Toggle Day/Night",
                    "&7• Weather control",
                    "&7• GameRule toggles");
        }

        slot = 25;
        if (PermissionManager.canManageServer(player)) {
            setItem(slot, Material.COMMAND_BLOCK,
                    "&5&lServer Management",
                    "&7Server-wide controls",
                    "&7• Whitelist management",
                    "&7• Ban list (searchable)",
                    "&7• Active players (kick/tp)");
        }

        // Row 3: Item Editor, Announcements, Staff, Monitoring
        slot = 28;
        if (PermissionManager.canEditItems(player)) {
            setItem(slot, Material.ENCHANTED_GOLDEN_APPLE,
                    "&d&lItem Editor",
                    "&7Edit held item's NBT",
                    "&7• Enchantments (unlimited)",
                    "&7• Attributes & Stats",
                    "&7• Name & Lore",
                    "&7• Bind commands");
        }

        slot = 29;
        if (PermissionManager.canAnnounce(player)) {
            setItem(slot, Material.BELL,
                    "&e&lAnnouncements",
                    "&7Send server announcements",
                    "&7• Custom messages",
                    "&7• Announcement templates");
        }

        slot = 31;
        if (PermissionManager.canUseStaffFeatures(player)) {
            setItem(slot, Material.EMERALD_BLOCK,
                    "&a&lStaff Panel",
                    "&7Staff coordination",
                    "&7• Staff online list",
                    "&7• Staff chat toggle",
                    "&7• Player notes");
        }

        slot = 33;
        if (PermissionManager.canMonitor(player)) {
            setItem(slot, Material.SPYGLASS,
                    "&9&lMonitoring",
                    "&7Server & player monitoring",
                    "&7• TPS & Memory",
                    "&7• Session history",
                    "&7• Alt detection",
                    "&7• Admin audit log");
        }

        // Row 4: Warps, Presets, Config
        slot = 37;
        if (PermissionManager.canManageWarps(player)) {
            setItem(slot, Material.ENDER_PEARL,
                    "&5&lWarps",
                    "&7Manage teleport warps",
                    "&7• Create / Delete warps",
                    "&7• Teleport to warps");
        }

        slot = 39;
        if (PermissionManager.canAnnounce(player)) {
            setItem(slot, Material.BOOK,
                    "&6&lPresets & Templates",
                    "&7Manage announcement & ban templates");
        }

        slot = 41;
        if (PermissionManager.canEditConfig(player)) {
            setItem(slot, Material.REDSTONE,
                    "&c&lConfig Editor",
                    "&7Edit plugin config in-game",
                    "&7• Toggle settings",
                    "&7• Hot-reload config");
        }

        fillPlaceholders();
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null || item.getType() == Material.AIR) return;

        switch (slot) {
            case 10 -> {
                if (PermissionManager.canControlPlayers(player)) {
                    new PlayerListMenu(plugin, player).open();
                }
            }
            case 13 -> {
                if (PermissionManager.canPunish(player)) {
                    new PunishmentMenu(plugin, player).open();
                }
            }
            case 16 -> {
                if (PermissionManager.canUseEconomy(player)) {
                    new EconomyMenu(plugin, player).open();
                }
            }
            case 19 -> {
                if (PermissionManager.canManageChat(player)) {
                    new com.adminpanel.gui.chat.ChatMenu(plugin, player).open();
                }
            }
            case 22 -> {
                if (PermissionManager.canManageWorld(player)) {
                    new WorldMenu(plugin, player).open();
                }
            }
            case 25 -> {
                if (PermissionManager.canManageServer(player)) {
                    new ServerMenu(plugin, player).open();
                }
            }
            case 28 -> {
                if (PermissionManager.canEditItems(player)) {
                    new ItemEditorMenu(plugin, player).open();
                }
            }
            case 29 -> {
                if (PermissionManager.canAnnounce(player)) {
                    new com.adminpanel.gui.announcement.AnnouncementMenu(plugin, player).open();
                }
            }
            case 31 -> {
                if (PermissionManager.canUseStaffFeatures(player)) {
                    new StaffListMenu(plugin, player).open();
                }
            }
            case 33 -> {
                if (PermissionManager.canMonitor(player)) {
                    new com.adminpanel.gui.monitoring.PerformanceMenu(plugin, player).open();
                }
            }
            case 37 -> {
                if (PermissionManager.canManageWarps(player)) {
                    new com.adminpanel.gui.warp.WarpMenu(plugin, player).open();
                }
            }
            case 39 -> {
                if (PermissionManager.canAnnounce(player)) {
                    new com.adminpanel.gui.preset.PresetMenu(plugin, player).open();
                }
            }
            case 41 -> {
                if (PermissionManager.canEditConfig(player)) {
                    new com.adminpanel.gui.config.ConfigEditorMenu(plugin, player).open();
                }
            }
        }
    }
}
