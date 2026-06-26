package com.adminpanel.gui.monitoring;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.MainMenu;
import com.adminpanel.gui.base.SubMenu;
import com.adminpanel.util.ColorUtil;
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
        super(plugin, player, "&0&lServer Performance", 5);
    }

    @Override
    protected void buildMenu() {
        // Row 1: TPS & Memory
        double[] tps = Bukkit.getServer().getTPS();
        String tps1 = formatTPS(tps[0]);
        String tps5 = formatTPS(tps[1]);
        String tps15 = formatTPS(tps[2]);

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

            int entities = world.getEntityCount();
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

        // Row 3: JVM info
        setItem(37, Material.COMMAND_BLOCK,
                "&9&lJVM Info",
                "&7Version: &f" + System.getProperty("java.version"),
                "&7OS: &f" + System.getProperty("os.name"),
                "&7Cores: &f" + runtime.availableProcessors());

        addBackButton();
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        // Navigation
        if (slot == 45) {
            new MainMenu(plugin, player).open();
        }
    }

    private String formatTPS(double tps) {
        if (tps >= 19.5) return "&a" + String.format("%.1f", tps);
        if (tps >= 17.0) return "&e" + String.format("%.1f", tps);
        return "&c" + String.format("%.1f", tps);
    }
}
