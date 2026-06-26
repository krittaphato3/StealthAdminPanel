package com.adminpanel.gui.player;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.base.SubMenu;
import com.adminpanel.gui.economy.BalanceMenu;
import com.adminpanel.gui.staff.PlayerNotesMenu;
import com.adminpanel.hooks.AnvilGUIBridge;
import com.adminpanel.manager.PermissionManager;
import com.adminpanel.util.HeadUtil;
import com.adminpanel.util.ItemBuilder;
import com.adminpanel.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Actions menu for a selected player.
 * Shows options: Inventory, Give Items, Troll, Set Rank, Notes, Economy.
 */
public class PlayerActionMenu extends SubMenu {

    private final Player target;

    public PlayerActionMenu(AdminPanel plugin, Player player, Player target) {
        super(plugin, player, "&0&l" + target.getName() + "'s Actions", 4);
        this.target = target;
    }

    @Override
    protected void buildMenu() {
        // Player head display
        setItem(4, HeadUtil.getHead(target.getName(),
                "&e&l" + target.getName(),
                "&7UUID: &f" + target.getUniqueId(),
                "&7Ping: &f" + target.getPing() + "ms",
                "&7World: &f" + target.getWorld().getName()));

        // Row 1: Core actions
        if (PermissionManager.has(player, PermissionManager.INVSEE)) {
            setItem(10, Material.CHEST,
                    "&a&lOpen Inventory",
                    "&7View & manage " + target.getName() + "'s inventory");
        }

        if (PermissionManager.has(player, PermissionManager.INVSEE)) {
            setItem(11, Material.DIAMOND,
                    "&b&lGive Items",
                    "&7Give items to " + target.getName());
        }

        if (PermissionManager.has(player, PermissionManager.TROLL)) {
            setItem(13, Material.BLAZE_ROD,
                    "&c&lTroll Options",
                    "&7Smite, Slap, Freeze, Fake Death");
        }

        if (PermissionManager.has(player, PermissionManager.RANKS)) {
            setItem(15, Material.NAME_TAG,
                    "&6&lSet Rank",
                    "&7Change " + target.getName() + "'s rank via Vault");
        }

        // Row 2: Info & extras
        if (PermissionManager.has(player, PermissionManager.ECONOMY)) {
            setItem(28, Material.EMERALD,
                    "&2&lEconomy",
                    "&7View/Give/Take balance");
        }

        if (PermissionManager.has(player, PermissionManager.NOTE)) {
            setItem(30, Material.BOOK,
                    "&9&lPlayer Notes",
                    "&7View/Add notes on this player");
        }

        // Row 3: Quick actions
        setItem(31, Material.ENDER_PEARL,
                "&d&lTeleport to " + target.getName(),
                "&7Instantly teleport to this player");

        setItem(32, Material.BARRIER,
                "&4&lKick Player",
                "&7Kick " + target.getName() + " from the server");

        addBackButton();
        fillPlaceholders();
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null || item.getType() == Material.AIR) return;

        switch (slot) {
            case 10 -> {
                // Open Inventory
                player.closeInventory();
                Bukkit.getScheduler().runTask(plugin, () ->
                        new InventoryViewMenu(plugin, player, target).open());
            }
            case 11 -> {
                // Give Items — open AnvilGUI for amount
                player.closeInventory();
                new AnvilGUIBridge(plugin).openNumberInput(player, "Item Amount", "64", amount -> {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        ItemStack hand = player.getInventory().getItemInMainHand();
                        if (hand != null && hand.getType() != Material.AIR) {
                            ItemStack give = hand.clone();
                            give.setAmount(amount);
                            target.getInventory().addItem(give);
                            player.sendMessage(TextUtil.colorize(
                                    "&aGave " + amount + "x " + give.getType().name() + " to " + target.getName()));
                        } else {
                            player.sendMessage(TextUtil.colorize("&cHold an item in your hand to give!"));
                        }
                    });
                });
            }
            case 13 -> {
                // Troll Options
                new TrollMenu(plugin, player, target).open();
            }
            case 15 -> {
                // Set Rank
                player.closeInventory();
                new AnvilGUIBridge(plugin).openRankInput(player, rank -> {
                    if (plugin.getVaultHook().hasPermissions()) {
                        plugin.getVaultHook().setGroup(target, target.getWorld().getName(), rank);
                        player.sendMessage(TextUtil.colorize(
                                "&aSet " + target.getName() + "'s rank to &e" + rank));
                    } else {
                        player.sendMessage(TextUtil.colorize("&cVault permissions not available!"));
                    }
                });
            }
            case 28 -> {
                // Economy
                new com.adminpanel.gui.economy.EconomyMenu(plugin, player).open();
            }
            case 30 -> {
                // Player Notes
                new PlayerNotesMenu(plugin, player, target).open();
            }
            case 31 -> {
                // Teleport to player
                player.teleport(target.getLocation());
                player.sendMessage(TextUtil.colorize("&aTeleported to " + target.getName()));
            }
            case 32 -> {
                // Kick player
                target.kickPlayer(TextUtil.colorize("&cKicked by admin."));
                player.sendMessage(TextUtil.colorize("&aKicked " + target.getName()));
                plugin.getAuditManager().log(player, "KICK", target.getName(), "Kicked via admin panel");
            }
            case 45 -> {
                // Back button
                new PlayerListMenu(plugin, player).open();
            }
        }
    }
}
