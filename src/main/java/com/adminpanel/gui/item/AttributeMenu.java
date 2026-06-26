package com.adminpanel.gui.item;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.base.PaginationGUI;
import com.adminpanel.hooks.AnvilGUIBridge;
import com.adminpanel.util.ItemBuilder;
import com.adminpanel.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Attribute editor — edit attack damage, speed, armor, etc.
 * Uses Bukkit's Attribute API for version-safe attribute manipulation.
 */
public class AttributeMenu extends PaginationGUI {

    private final ItemStack editingItem;

    public AttributeMenu(AdminPanel plugin, Player player, ItemStack item) {
        super(plugin, player, "&0&lAttribute Editor");
        this.editingItem = item;
    }

    @Override
    protected List<ItemStack> getPageItems() {
        List<ItemStack> items = new ArrayList<>();

        for (Attribute attr : Attribute.values()) {
            Material icon = switch (attr) {
                case GENERIC_ATTACK_DAMAGE -> Material.IRON_SWORD;
                case GENERIC_ATTACK_SPEED -> Material.FEATHER;
                case GENERIC_ARMOR -> Material.IRON_CHESTPLATE;
                case GENERIC_ARMOR_TOUGHNESS -> Material.DIAMOND;
                case GENERIC_LUCK -> Material.CLOVER;
                case GENERIC_MAX_HEALTH -> Material.RED_APPLE;
                case GENERIC_MOVEMENT_SPEED -> Material.SUGAR;
                case GENERIC_FOLLOW_RANGE -> Material.SPYGLASS;
                case GENERIC_KNOCKBACK_RESISTANCE -> Material.SHIELD;
                default -> Material.PAPER;
            };

            items.add(new ItemBuilder(icon)
                    .name("&e" + attr.name())
                    .lore(
                            "&7Description: &f" + (attr.getDescription() != null ? attr.getDescription() : "N/A"),
                            "",
                            "&a&lClick to set value (AnvilGUI)",
                            "&c&lShift-click to remove modifier")
                    .build());
        }

        return items;
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null || item.getType() == Material.AIR) return;

        for (Attribute attr : Attribute.values()) {
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                    && TextUtil.stripColor(item.getItemMeta().hasDisplayName() ?
                            item.getItemMeta().getDisplayName() : "").equals(attr.name())) {

                if (player.isSneaking()) {
                    // Remove modifier
                    ItemMeta meta = editingItem.getItemMeta();
                    if (meta != null) {
                        meta.removeAttributeModifier(attr);
                        editingItem.setItemMeta(meta);
                        player.sendMessage(TextUtil.colorize("&cRemoved " + attr.name() + " modifier"));
                    }
                    refresh();
                } else {
                    // Set value via AnvilGUI
                    player.closeInventory();
                    new AnvilGUIBridge(plugin).openNumberInput(player,
                            attr.name() + " Value", "1.0", value -> {
                        org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                            ItemMeta meta = editingItem.getItemMeta();
                            if (meta != null) {
                                // Remove existing modifier first
                                meta.removeAttributeModifier(attr);

                                // Add new modifier
                                AttributeModifier modifier = new AttributeModifier(
                                        UUID.randomUUID(),
                                        attr.name().toLowerCase(),
                                        value,
                                        AttributeModifier.Operation.ADD_NUMBER,
                                        EquipmentSlotGroup.ANY
                                );
                                meta.addAttributeModifier(attr, modifier);
                                editingItem.setItemMeta(meta);

                                player.sendMessage(TextUtil.colorize(
                                        "&aSet " + attr.name() + " to &e" + value));
                                plugin.getAuditManager().log(player, "ITEM_ATTRIBUTE",
                                        editingItem.getType().name(),
                                        attr.name() + " = " + value);
                            }
                            refresh();
                        });
                    });
                }
                return;
            }
        }
    }

    @Override
    public String getMenuTitle() {
        return "&0&lAttribute Editor";
    }
}
