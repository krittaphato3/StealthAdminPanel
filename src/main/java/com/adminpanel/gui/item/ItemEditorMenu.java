package com.adminpanel.gui.item;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.MainMenu;
import com.adminpanel.gui.base.SubMenu;
import com.adminpanel.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Item editor hub — edit the held item's NBT, enchantments, attributes, etc.
 * Requires an item in hand.
 */
public class ItemEditorMenu extends SubMenu {

    private final ItemStack heldItem;

    public ItemEditorMenu(AdminPanel plugin, Player player) {
        super(plugin, player, "&0&lItem Editor", 4);
        this.heldItem = player.getInventory().getItemInMainHand();
    }

    @Override
    protected void buildMenu() {
        if (heldItem == null || heldItem.getType() == Material.AIR) {
            setItem(13, Material.BARRIER,
                    "&c&lNo Item in Hand",
                    "&7Hold an item in your main hand",
                    "&7to use the item editor");
            addBackButton();
            return;
        }

        // Show current item info
        setItem(4, heldItem.getType(),
                "&e&l" + heldItem.getType().name(),
                "&7Amount: &f" + heldItem.getAmount(),
                heldItem.hasItemMeta() && heldItem.getItemMeta().hasDisplayName() ?
                        "&7Name: &f" + TextUtil.stripColor(heldItem.getItemMeta().getDisplayName()) : "");

        // Row 1: Core editing
        setItem(10, Material.ENCHANTED_BOOK,
                "&b&lEnchantments",
                "&7Add/remove enchantments",
                "&7Unlimited levels supported");

        setItem(11, Material.DIAMOND_CHESTPLATE,
                "&9&lAttributes",
                "&7Edit attack damage, speed",
                "&7Armor, luck, and more");

        setItem(12, Material.NAME_TAG,
                "&6&lDisplay",
                "&7Edit name, lore, model data",
                "&7Item flags & visibility");

        // Row 2: Advanced
        setItem(19, Material.BOOKSHELF,
                "&5&lNBT Editor",
                "&7Advanced NBT tag editing",
                "&7Full raw NBT access");

        setItem(20, Material.COMMAND_BLOCK,
                "&c&lCommand Binding",
                "&7Bind a command to execute",
                "&7On use, hit, or projectile");

        setItem(21, Material.REDSTONE,
                "&e&lDamage Override",
                "&7Set custom attack damage",
                "&7Override vanilla calculation");

        // Quick actions
        setItem(28, Material.BARRIER,
                "&4&lUnbreakable: " + isUnbreakable(),
                "&7Click to toggle");

        addBackButton();
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (heldItem == null || heldItem.getType() == Material.AIR) return;

        switch (slot) {
            case 10 -> new EnchantMenu(plugin, player, heldItem).open();
            case 11 -> new AttributeMenu(plugin, player, heldItem).open();
            case 12 -> new DisplayMenu(plugin, player, heldItem).open();
            case 19 -> new NBTMenu(plugin, player, heldItem).open();
            case 20 -> new CommandBindMenu(plugin, player, heldItem).open();
            case 28 -> {
                // Toggle unbreakable
                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                if (itemInHand != null) {
                    org.bukkit.inventory.meta.ItemMeta meta = itemInHand.getItemMeta();
                    if (meta != null) {
                        meta.setUnbreakable(!meta.isUnbreakable());
                        if (meta.isUnbreakable()) {
                            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);
                        } else {
                            meta.removeItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);
                        }
                        itemInHand.setItemMeta(meta);
                        player.sendMessage(TextUtil.colorize(
                                "&aUnbreakable set to &e" + meta.isUnbreakable()));
                        plugin.getAuditManager().log(player, "ITEM_EDIT", itemInHand.getType().name(),
                                "Unbreakable: " + meta.isUnbreakable());
                    }
                }
                refresh();
            }
            case 45 -> new MainMenu(plugin, player).open();
        }
    }

    private String isUnbreakable() {
        if (heldItem.hasItemMeta()) {
            return heldItem.getItemMeta().isUnbreakable() ? "&aON" : "&cOFF";
        }
        return "&cOFF";
    }
}
