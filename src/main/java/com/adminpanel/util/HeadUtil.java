package com.adminpanel.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

/**
 * Utility for loading player heads.
 * Uses SkullMeta.setOwner() for broad compatibility with Spigot/Paper.
 */
public final class HeadUtil {

    private HeadUtil() {}

    /**
     * Create a player head ItemStack by name.
     */
    public static ItemStack getHead(String playerName) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwner(playerName);
            head.setItemMeta(meta);
        }
        return head;
    }

    /**
     * Create a player head with custom display name and lore.
     */
    public static ItemStack getHead(String playerName, String displayName, String... lore) {
        ItemStack head = getHead(playerName);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            if (displayName != null) {
                meta.setDisplayName(TextUtil.colorize(displayName));
            }
            if (lore != null && lore.length > 0) {
                java.util.List<String> coloredLore = new java.util.ArrayList<>();
                for (String line : lore) {
                    coloredLore.add(TextUtil.colorize(line));
                }
                meta.setLore(coloredLore);
            }
            head.setItemMeta(meta);
        }
        return head;
    }

    /**
     * Create a placeholder head with the Steve skin.
     */
    public static ItemStack getSteveHead() {
        return getHead("Steve");
    }
}
