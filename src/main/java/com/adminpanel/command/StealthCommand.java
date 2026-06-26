package com.adminpanel.command;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.MainMenu;
import com.adminpanel.manager.PermissionManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stealth command registered dynamically via CommandMap.
 * NOT declared in plugin.yml — invisible to players without permission.
 *
 * - tabComplete() returns results ONLY if player has adminpanel.use
 * - execute() sends vanilla "Unknown command" if player lacks permission
 * - If authorized, opens the MainMenu GUI
 */
public class StealthCommand extends Command {

    private final AdminPanel plugin;

    public StealthCommand(AdminPanel plugin, String name) {
        super(name);
        this.plugin = plugin;
        this.setDescription("Stealth Admin Panel");
        this.setPermission(PermissionManager.USE);
        // Don't set usage — we handle everything in execute
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        // Only players can use this command
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        // Permission check — silent failure with vanilla message
        if (!player.hasPermission(PermissionManager.USE)) {
            player.sendMessage(plugin.getUnknownCommandMessage());
            return true;
        }

        // Open the main admin panel GUI
        Bukkit.getScheduler().runTask(plugin, () -> {
            MainMenu menu = new MainMenu(plugin, player);
            menu.open();
        });

        return true;
    }

    /**
     * Tab completion is ONLY available to players with adminpanel.use permission.
     * For unauthorized players, returns an empty list — completely invisible.
     */
    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        // No tab completions for console
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }

        // Return empty if player lacks permission — invisible in tab-complete
        if (!player.hasPermission(PermissionManager.USE)) {
            return Collections.emptyList();
        }

        // For authorized players, we could provide sub-command completions
        // For now, /ap opens the GUI directly so no sub-commands needed
        return Collections.emptyList();
    }

    @Override
    public boolean testPermission(CommandSender target) {
        // Override to prevent "You do not have permission" messages from leaking
        return target.hasPermission(PermissionManager.USE);
    }

    @Override
    public boolean testPermissionSilent(CommandSender target) {
        return target.hasPermission(PermissionManager.USE);
    }
}
