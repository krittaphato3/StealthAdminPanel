package com.adminpanel.util;

import net.md_5.bungee.api.ChatColor;

import java.awt.Color;

/**
 * Advanced color utilities: RGB gradients, rainbow text, hex conversions.
 */
public final class ColorUtil {

    private ColorUtil() {}

    /**
     * Apply an RGB gradient across a string.
     * Example: gradient("Hello", Color.RED, Color.BLUE) → "H" in red ... "o" in blue.
     */
    public static String gradient(String text, Color start, Color end) {
        if (text == null || text.isEmpty()) return text;

        int len = text.length();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < len; i++) {
            float ratio = (float) i / Math.max(len - 1, 1);
            int r = (int) (start.getRed() + ratio * (end.getRed() - start.getRed()));
            int g = (int) (start.getGreen() + ratio * (end.getGreen() - start.getGreen()));
            int b = (int) (start.getBlue() + ratio * (end.getBlue() - start.getBlue()));

            ChatColor color = ChatColor.of(new java.awt.Color(r, g, b));
            sb.append(color).append(text.charAt(i));
        }

        return sb.toString();
    }

    /**
     * Apply rainbow gradient to a string.
     */
    public static String rainbow(String text) {
        if (text == null || text.isEmpty()) return text;

        float hueStep = 1.0f / text.length();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            float hue = i * hueStep;
            int rgb = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);
            ChatColor color = ChatColor.of(new java.awt.Color(rgb));
            sb.append(color).append(text.charAt(i));
        }

        return sb.toString();
    }

    /**
     * Convert a hex string (#RRGGBB) to ChatColor.
     */
    public static ChatColor fromHex(String hex) {
        if (hex == null || hex.isEmpty()) return ChatColor.WHITE;
        if (hex.startsWith("#")) hex = hex.substring(1);
        try {
            int rgb = Integer.parseInt(hex, 16);
            return ChatColor.of(new java.awt.Color(rgb));
        } catch (NumberFormatException e) {
            return ChatColor.WHITE;
        }
    }

    /**
     * Format a string with a hex color code.
     * Input: "Hello", "#FF5555" → colored "Hello"
     */
    public static String hexColor(String text, String hex) {
        return fromHex(hex) + text + ChatColor.RESET;
    }

    /**
     * Create a gradient between two hex colors.
     */
    public static String gradientHex(String text, String startHex, String endHex) {
        Color start = hexToAwt(startHex);
        Color end = hexToAwt(endHex);
        return gradient(text, start, end);
    }

    /**
     * Convert hex string to java.awt.Color.
     */
    private static Color hexToAwt(String hex) {
        if (hex.startsWith("#")) hex = hex.substring(1);
        int rgb = Integer.parseInt(hex, 16);
        return new Color(rgb);
    }

    /**
     * Get a color name for a boolean state (true = green, false = red).
     */
    public static String booleanColor(boolean state) {
        return state ? "&a" : "&c";
    }

    /**
     * Get a state indicator string for boolean values.
     */
    public static String stateIndicator(boolean state) {
        return state ? "&a&l✔ ENABLED" : "&c&l✘ DISABLED";
    }

    /**
     * Get a toggle item name based on state.
     */
    public static String toggleName(String baseName, boolean state) {
        return (state ? "&a" : "&c") + baseName;
    }
}
