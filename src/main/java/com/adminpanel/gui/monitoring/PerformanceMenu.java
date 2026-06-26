package com.adminpanel.gui.monitoring;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.MainMenu;
import com.adminpanel.gui.base.SubMenu;
import com.adminpanel.util.ColorUtil;
import com.adminpanel.util.ItemBuilder;
import com.adminpanel.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.management.ManagementFactory;

/**
 * Server performance dashboard — TPS, memory, entity counts, chunk counts.
 */
public class PerformanceMenu extends SubMenu {

    public PerformanceMenu(AdminPanel plugin, Player player) {
        super(plugin, player, "&0&lServer Performance", 6);
    }

    @Override
    protected void buildMenu() {
        // Row 1: TPS & Memory (use reflection for Paper TPS, fallback to N/A)
        String tps1 = "&7N/A";
        String tps5 = "&7N/A";
        String tps15 = "&7N/A";
        try {
            // Try Paper's getTPS() via reflection
            var tpsMethod = Bukkit.getServer().getClass().getMethod("getTPS");
            double[] tps = (double[]) tpsMethod.invoke(Bukkit.getServer());
            tps1 = formatTPS(tps[0]);
            tps5 = formatTPS(tps[1]);
            tps15 = formatTPS(tps[2]);
        } catch (Exception ignored) {}

        setItem(10, Material.PAPER,
                "&e&lTPS (Ticks Per Second)",
                "&71m: " + tps1,
                "&75m: " + tps5,
                "&715m: " + tps15);

        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        double memPercent = (double) usedMemory / maxMemory * 100;

        setItem(12, Material.REDSTONE,
                "&c&lMemory Usage",
                "&7Used: &e" + usedMemory + " MB",
                "&7Free: &a" + freeMemory + " MB",
                "&7Max: &f" + maxMemory + " MB",
                "&7Usage: " + (memPercent > 80 ? "&c" : memPercent > 50 ? "&e" : "&a")
                        + String.format("%.1f%%", memPercent));

        // Uptime
        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
        long uptimeSec = uptimeMs / 1000;
        long hours = uptimeSec / 3600;
        long minutes = (uptimeSec % 3600) / 60;
        setItem(14, Material.CLOCK,
                "&6&lServer Uptime",
                "&7" + hours + "h " + minutes + "m");

        // Row 2: World stats
        int slot = 19;
        for (World world : Bukkit.getWorlds()) {
            if (slot > 35) break;

            int entities = world.getEntities().size();
            int chunks = world.getLoadedChunks().length;
            int players = world.getPlayers().size();

            String entityColor = entities > 1000 ? "&c" : entities > 500 ? "&e" : "&a";

            setItem(slot, Material.GRASS_BLOCK,
                    "&2&l" + world.getName(),
                    "&7Entities: " + entityColor + entities,
                    "&7Chunks: &f" + chunks,
                    "&7Players: &f" + players,
                    "&7Difficulty: &f" + world.getDifficulty());

            slot++;
        }

        // Row 3: JVM info + Monitoring sub-menus
        setItem(37, Material.COMMAND_BLOCK,
                "&9&lJVM Info",
                "&7Version: &f" + System.getProperty("java.version"),
                "&7OS: &f" + System.getProperty("os.name"),
                "&7Cores: &f" + runtime.availableProcessors());

        setItem(38, Material.BOOK,
                "&b&lSession History",
                "&7View player join/leave sessions",
                "&7Playtime, IP tracking");

        setItem(39, Material.RED_WOOL,
                "&c&lAlt Detection",
                "&7Find accounts sharing IPs",
                "&7Online player IP analysis");

        setItem(40, Material.BOOKSHELF,
                "&6&lAudit Log",
                "&7View all admin actions",
                "&7Searchable action trail");

        // Auto-refresh indicator
        setItem(49, new ItemBuilder(Material.CLOCK)
                .name("&a&l⟳ Auto-Refresh: ON")
                .lore("&7Page auto-refreshes every 3 seconds",
                      "&7Close menu to stop")
                .build());

        addBackButton();

        // Schedule auto-refresh
        scheduleRefresh();
    }

    private org.bukkit.scheduler.BukkitTask refreshTask;

    private void scheduleRefresh() {
        if (refreshTask != null) refreshTask.cancel();
        refreshTask = org.bukkit.Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (player.isOnline() && player.getOpenInventory().getTopInventory().getHolder() == this) {
                refresh();
            } else {
                refreshTask.cancel();
            }
        }, 60L, 60L); // Every 3 seconds (60 ticks)
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (refreshTask != null) refreshTask.cancel();
        switch (slot) {
            case 38 -> new SessionMenu(plugin, player).open();
            case 39 -> new AltDetectMenu(plugin, player).open();
            case 40 -> new AuditLogMenu(plugin, player).open();
        }
    }

    @Override
    public void onBackClick() {
        if (refreshTask != null) refreshTask.cancel();
        new MainMenu(plugin, player).open();
    }

    private String formatTPS(double tps) {
        if (tps >= 19.5) return "&a" + String.format("%.1f", tps);
        if (tps >= 17.0) return "&e" + String.format("%.1f", tps);
        return "&c" + String.format("%.1f", tps);
    }
}
