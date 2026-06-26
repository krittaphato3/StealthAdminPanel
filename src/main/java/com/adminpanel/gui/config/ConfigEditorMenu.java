package com.adminpanel.gui.config;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.MainMenu;
import com.adminpanel.gui.base.SubMenu;
import com.adminpanel.util.ColorUtil;
import com.adminpanel.util.ItemBuilder;
import com.adminpanel.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * In-game config editor — toggle settings and hot-reload config.
 */
public class ConfigEditorMenu extends SubMenu {

    public ConfigEditorMenu(AdminPanel plugin, Player player) {
        super(plugin, player, "&0&lConfig Editor", 5);
    }

    @Override
    protected void buildMenu() {
        FileConfiguration config = plugin.pluginConfig();

        // Sound suppression
        boolean sounds = config.getBoolean("suppress-sounds", true);
        setItem(10, Material.NOTE_BLOCK,
                ColorUtil.toggleName("Suppress Sounds", sounds),
                "&7Cancel inventory open sounds",
                "&7Status: " + ColorUtil.stateIndicator(sounds));

        // Auto-ban after warns
        int autoBan = config.getInt("punishment.auto-ban-after-warns", 3);
        setItem(12, Material.IRON_SWORD,
                "&e&lAuto-ban Threshold",
                "&7Current: &f" + autoBan + " warns",
                "&70 = disabled");

        // Slow mode default
        int slowMode = config.getInt("chat.slow-mode-cooldown", 5);
        setItem(14, Material.CLOCK,
                "&b&lDefault Slow Mode",
                "&7Current: &f" + slowMode + "s",
                "&70 = disabled");

        // Announcement prefix
        String prefix = config.getString("announcement.prefix", "&6&l[Admin] &r");
        setItem(16, Material.BELL,
                "&6&lAnnouncement Prefix",
                "&7Current: " + prefix,
                "&7Click to change");

        // Staff chat format
        String staffFormat = config.getString("chat.staff-chat-format",
                "&8[&bStaff&8] &e%player%&7: &f%message%");
        setItem(19, Material.PAPER,
                "&9&lStaff Chat Format",
                "&7Current: " + staffFormat);

        // Max enchant level
        int maxEnchant = config.getInt("item-editor.max-enchant-level", 1000000);
        setItem(21, Material.ENCHANTED_GOLDEN_APPLE,
                "&d&lMax Enchant Level",
                "&7Current: &f" + maxEnchant);

        // Reload config
        setItem(31, Material.LIME_DYE,
                "&a&l✔ Reload Config",
                "&7Reload config.yml from disk");

        // Save config
        setItem(33, Material.COMPARATOR,
                "&e&lSave Config",
                "&7Save current config to disk");

        addBackButton();
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null || item.getType() == Material.AIR) return;

        FileConfiguration config = plugin.pluginConfig();

        switch (slot) {
            case 10 -> {
                // Toggle suppress-sounds
                boolean current = config.getBoolean("suppress-sounds", true);
                config.set("suppress-sounds", !current);
                plugin.saveConfig();
                player.sendMessage(TextUtil.colorize("&aSuppress sounds set to: " + (!current)));
                plugin.getAuditManager().log(player, "CONFIG_CHANGE", "suppress-sounds",
                        String.valueOf(!current));
                refresh();
            }
            case 31 -> {
                // Reload config
                plugin.reloadConfig();
                player.sendMessage(TextUtil.colorize("&aConfig reloaded from disk!"));
                refresh();
            }
            case 33 -> {
                // Save config
                plugin.saveConfig();
                player.sendMessage(TextUtil.colorize("&aConfig saved to disk!"));
            }
        }
    }
}
