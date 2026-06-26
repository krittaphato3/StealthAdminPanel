package com.adminpanel.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Fluent ItemStack builder.
 * Chain methods to build items without verbose boilerplate.
 *
 * Usage:
 *   ItemStack item = new ItemBuilder(Material.DIAMOND_SWORD)
 *       .name("&6&lSuper Sword")
 *       .lore("&7Damage: &c100", "&7Speed: &aFast")
 *       .glow(true)
 *       .unbreakable(true)
 *       .build();
 */
public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(ItemStack item) {
        this.item = item.clone();
        this.meta = this.item.getItemMeta();
    }

    /**
     * Set the display name with &-color code support.
     */
    public ItemBuilder name(String name) {
        if (meta != null) {
            meta.setDisplayName(TextUtil.colorize(name));
        }
        return this;
    }

    /**
     * Set the lore with &-color code support.
     */
    public ItemBuilder lore(String... lines) {
        if (meta != null) {
            List<String> colored = new ArrayList<>();
            for (String line : lines) {
                colored.add(TextUtil.colorize(line));
            }
            meta.setLore(colored);
        }
        return this;
    }

    /**
     * Set the lore from a list.
     */
    public ItemBuilder lore(List<String> lines) {
        if (meta != null) {
            List<String> colored = new ArrayList<>();
            for (String line : lines) {
                colored.add(TextUtil.colorize(line));
            }
            meta.setLore(colored);
        }
        return this;
    }

    /**
     * Add lines to existing lore.
     */
    public ItemBuilder addLore(String... lines) {
        if (meta != null) {
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            for (String line : lines) {
                lore.add(TextUtil.colorize(line));
            }
            meta.setLore(lore);
        }
        return this;
    }

    /**
     * Toggle enchantment glow effect (without actual enchantment).
     */
    public ItemBuilder glow(boolean glow) {
        if (meta != null) {
            if (glow) {
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            } else {
                meta.removeEnchant(Enchantment.DURABILITY);
            }
        }
        return this;
    }

    /**
     * Add a real enchantment with a specific level.
     * Use Integer.MAX_VALUE for "unlimited" levels.
     */
    public ItemBuilder enchant(Enchantment enchantment, int level) {
        if (meta != null) {
            meta.addEnchant(enchantment, level, true);
        }
        return this;
    }

    /**
     * Make the item unbreakable.
     */
    public ItemBuilder unbreakable(boolean unbreakable) {
        if (meta != null) {
            meta.setUnbreakable(unbreakable);
            if (unbreakable) {
                meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            }
        }
        return this;
    }

    /**
     * Set the amount.
     */
    public ItemBuilder amount(int amount) {
        item.setAmount(amount);
        return this;
    }

    /**
     * Set custom model data.
     */
    public ItemBuilder modelData(int modelData) {
        if (meta != null) {
            meta.setCustomModelData(modelData);
        }
        return this;
    }

    /**
     * Hide specific item flags.
     */
    public ItemBuilder hideFlags(ItemFlag... flags) {
        if (meta != null) {
            meta.addItemFlags(flags);
        }
        return this;
    }

    /**
     * Hide all item flags.
     */
    public ItemBuilder hideAllFlags() {
        if (meta != null) {
            meta.addItemFlags(ItemFlag.values());
        }
        return this;
    }

    /**
     * Set skull owner to a player name (offline).
     */
    public ItemBuilder skullOwner(String playerName) {
        if (meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwner(playerName);
        }
        return this;
    }

    /**
     * Set skull owner via GameProfile with base64 texture.
     */
    public ItemBuilder skullTexture(String base64Texture) {
        if (meta instanceof SkullMeta skullMeta) {
            GameProfile profile = new GameProfile(UUID.randomUUID(), "AdminPanel");
            PropertyMap properties = profile.getProperties();
            properties.put("textures",
                    new com.mojang.authlib.properties.Property("textures", base64Texture));
            try {
                Field profileField = skullMeta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(skullMeta, profile);
            } catch (Exception ignored) {}
        }
        return this;
    }

    /**
     * Set leather armor color.
     */
    public ItemBuilder leatherColor(Color color) {
        if (meta instanceof LeatherArmorMeta leatherMeta) {
            leatherMeta.setColor(color);
        }
        return this;
    }

    /**
     * Set the durability (damage value).
     */
    public ItemBuilder durability(short durability) {
        item.setDurability(durability);
        return this;
    }

    /**
     * Add an item flag.
     */
    public ItemBuilder flag(ItemFlag flag) {
        if (meta != null) {
            meta.addItemFlags(flag);
        }
        return this;
    }

    /**
     * Build and return the ItemStack.
     */
    public ItemStack build() {
        if (meta != null) {
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Convenience: create a stained glass pane of a specific color.
     */
    public static ItemBuilder coloredPane(Material material, Color color) {
        ItemBuilder builder = new ItemBuilder(material);
        if (builder.meta instanceof LeatherArmorMeta leatherMeta) {
            leatherMeta.setColor(color);
        }
        return builder;
    }

    /**
     * Create a placeholder (black stained glass pane) for GUI filler.
     */
    public static ItemStack placeholder() {
        return new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .name(" ")
                .hideAllFlags()
                .build();
    }

    /**
     * Create a separator line of placeholder panes.
     */
    public static ItemStack[] separatorLine() {
        ItemStack[] panes = new ItemStack[9];
        Arrays.fill(panes, placeholder());
        return panes;
    }
}
