package com.adminpanel.listener;

import com.adminpanel.AdminPanel;
import com.adminpanel.hooks.AnvilGUIBridge;
import com.adminpanel.manager.SessionManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Tracks player sessions: join times, leave times, IPs, and alt detection.
 * Also cleans up pending AnvilGUI input sessions on disconnect.
 */
public class PlayerSessionListener implements Listener {

    private final AdminPanel plugin;
    private final SessionManager sessionManager;

    public PlayerSessionListener(AdminPanel plugin, SessionManager sessionManager) {
        this.plugin = plugin;
        this.sessionManager = sessionManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        sessionManager.onJoin(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        sessionManager.onLeave(player);
        // Clean up any pending AnvilGUI input sessions
        AnvilGUIBridge.cleanup(player.getUniqueId());
    }
}
