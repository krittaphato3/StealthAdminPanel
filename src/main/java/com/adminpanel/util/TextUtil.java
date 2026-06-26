package com.adminpanel.util;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Text formatting utilities.
 * Handles &-color codes, hex colors, and placeholder replacement.
 */
public final class TextUtil {

    // Matches &#RRGGBB hex color codes
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    // Matches &-based color codes
    private static final Pattern COLOR_PATTERN = Pattern.compile("&[0-9a-fk-orA-FK-OR]");

    private TextUtil() {}

    /**
     * Translate a string with &-color codes and hex colors.
     * Supports: &0-&9, &a-&f, &k-&o, &#RRGGBB
     */
    public static String colorize(String text) {
        if (text == null) return "";
        // Process hex colors first
        Matcher hexMatcher = HEX_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (hexMatcher.find()) {
            hexMatcher.appendReplacement(sb, ChatColor.of("#" + hexMatcher.group(1)).toString());
        }
        hexMatcher.appendTail(sb);
        // Process standard color codes
        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }

    /**
     * Strip all color codes from a string.
     */
    public static String stripColor(String text) {
        if (text == null) return "";
        return ChatColor.stripColor(colorize(text));
    }

    /**
     * Replace a placeholder in a string.
     * Placeholders are in the format %key%.
     */
    public static String replace(String text, String key, String value) {
        if (text == null) return "";
        return text.replace("%" + key + "%", value != null ? value : "");
    }

    /**
     * Replace multiple placeholders.
     */
    public static String replace(String text, String... pairs) {
        if (text == null) return "";
        for (int i = 0; i + 1 < pairs.length; i += 2) {
            text = replace(text, pairs[i], pairs[i + 1]);
        }
        return text;
    }

    /**
     * Format a player name with color.
     */
    public static String formatPlayerName(String name) {
        return "&e" + name + "&7";
    }

    /**
     * Create a centered line for chat (Minecraft chat is 154 pixels wide).
     */
    public static String center(String message) {
        int padding = (154 - (message.length() * 6)) / 2;
        if (padding <= 0) return message;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < padding / 6; i++) {
            sb.append(" ");
        }
        sb.append(message);
        return sb.toString();
    }

    /**
     * Format a large number with commas (1234567 → 1,234,567).
     */
    public static String formatNumber(long number) {
        if (number < 0) return "-" + formatNumber(-number);
        String s = String.valueOf(number);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (i > 0 && (s.length() - i) % 3 == 0) {
                sb.append(",");
            }
            sb.append(s.charAt(i));
        }
        return sb.toString();
    }

    /**
     * Format a decimal number to 2 decimal places.
     */
    public static String formatDecimal(double number) {
        return String.format("%.2f", number);
    }
}
