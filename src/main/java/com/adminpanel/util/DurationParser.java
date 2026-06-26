package com.adminpanel.util;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses duration strings like "1h30m", "7d", "30s", "perm" into milliseconds.
 * Supports: s (seconds), m (minutes), h (hours), d (days), w (weeks), perm (permanent).
 */
public final class DurationParser {

    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)([smhdw])");

    private DurationParser() {}

    /**
     * Parse a duration string to milliseconds.
     * Returns -1 for permanent (perm/perm).
     * Returns 0 for invalid input.
     */
    public static long parse(String input) {
        if (input == null || input.isEmpty()) return 0;

        String lower = input.toLowerCase().trim();

        // Permanent
        if (lower.equals("perm") || lower.equals("permanent") || lower.equals("-1") || lower.equals("inf")) {
            return -1;
        }

        long totalMs = 0;
        Matcher matcher = DURATION_PATTERN.matcher(lower);

        while (matcher.find()) {
            long value = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);

            switch (unit) {
                case "s" -> totalMs += TimeUnit.SECONDS.toMillis(value);
                case "m" -> totalMs += TimeUnit.MINUTES.toMillis(value);
                case "h" -> totalMs += TimeUnit.HOURS.toMillis(value);
                case "d" -> totalMs += TimeUnit.DAYS.toMillis(value);
                case "w" -> totalMs += TimeUnit.DAYS.toMillis(value * 7);
            }
        }

        // If no pattern matched but we got here, check for plain number (treat as seconds)
        if (totalMs == 0) {
            try {
                long plainSeconds = Long.parseLong(lower);
                return TimeUnit.SECONDS.toMillis(plainSeconds);
            } catch (NumberFormatException ignored) {}
        }

        return totalMs;
    }

    /**
     * Format milliseconds to a human-readable string.
     * Example: 3661000 → "1h 1m 1s"
     */
    public static String format(long ms) {
        if (ms == -1) return "Permanent";
        if (ms <= 0) return "Expired";

        long days = TimeUnit.MILLISECONDS.toDays(ms);
        long hours = TimeUnit.MILLISECONDS.toHours(ms) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(ms) % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (seconds > 0) sb.append(seconds).append("s");

        return sb.toString().trim();
    }

    /**
     * Format milliseconds with color codes for GUI display.
     * Green for long durations, yellow for medium, red for short.
     */
    public static String formatColored(long ms) {
        if (ms == -1) return "&aPermanent";
        if (ms <= 0) return "&cExpired";

        String formatted = format(ms);

        if (ms > TimeUnit.DAYS.toMillis(30)) return "&a" + formatted;
        if (ms > TimeUnit.DAYS.toMillis(1)) return "&e" + formatted;
        return "&c" + formatted;
    }

    /**
     * Check if a duration has expired.
     */
    public static boolean isExpired(long expiresAt) {
        return expiresAt != -1 && System.currentTimeMillis() > expiresAt;
    }
}
