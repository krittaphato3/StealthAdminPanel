package com.adminpanel.util;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Sound effects for GUI interactions.
 * Provides consistent audio feedback across all menus.
 */
public final class SoundUtil {

    private SoundUtil() {}

    /**
     * Play when a menu opens.
     */
    public static void playOpen(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
    }

    /**
     * Play when a button is clicked in a menu.
     */
    public static void playClick(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.3f, 1.0f);
    }

    /**
     * Play when navigating between pages.
     */
    public static void playNavigate(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.4f, 1.5f);
    }

    /**
     * Play when a success action completes (ban, give item, etc.).
     */
    public static void playSuccess(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);
    }

    /**
     * Play when an error occurs or action fails.
     */
    public static void playError(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
    }

    /**
     * Play when a destructive action occurs (ban, kick, delete).
     */
    public static void playDestructive(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 0.3f, 1.2f);
    }

    /**
     * Play when toggling a setting on.
     */
    public static void playToggleOn(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 0.5f, 1.5f);
    }

    /**
     * Play when toggling a setting off.
     */
    public static void playToggleOff(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 0.5f, 0.8f);
    }

    /**
     * Play a dramatic action sound (smite, lightning, etc.).
     */
    public static void playDramatic(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.3f, 1.0f);
    }

    /**
     * Play when searching / input prompt appears.
     */
    public static void playSearch(Player player) {
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 0.4f, 1.2f);
    }

    /**
     * Play when a player is teleported.
     */
    public static void playTeleport(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.0f);
    }

    /**
     * Play when something is unlocked or enabled.
     */
    public static void playUnlock(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.4f, 1.2f);
    }

    /**
     * Play when something is locked or disabled.
     */
    public static void playLock(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 0.4f, 0.8f);
    }
}
