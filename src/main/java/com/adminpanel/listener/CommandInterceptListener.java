package com.adminpanel.listener;

import com.adminpanel.AdminPanel;
import com.adminpanel.manager.PermissionManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * Intercepts command attempts from unauthorized players.
 *
 * If a player without adminpanel.use tries /ap or /adminpanel,
 * the event is canceled and a vanilla "Unknown command" message is sent.
 * This prevents any "You do not have permission" plugin-leakage messages.
 */
public class CommandInterceptListener implements Listener {

    private final AdminPanel plugin;

    public CommandInterceptListener(AdminPanel plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage().toLowerCase();

        // Check if the command is /ap or /adminpanel (with or without arguments)
        boolean isApCommand = message.equals("/ap") || message.startsWith("/ap ")
                || message.equals("/adminpanel") || message.startsWith("/adminpanel ");

        if (!isApCommand) return;

        // If the player has permission, let the StealthCommand handle it
        if (player.hasPermission(PermissionManager.USE)) return;

        // Cancel the event to prevent any plugin from processing it
        event.setCancelled(true);

        // Send the vanilla "Unknown command" message
        player.sendMessage(plugin.getUnknownCommandMessage());
    }
}
