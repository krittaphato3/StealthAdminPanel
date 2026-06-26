package com.adminpanel.listener;

import com.adminpanel.AdminPanel;
import com.adminpanel.manager.SessionManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Tracks player sessions: join times, leave times, IPs, and alt detection.
 */
public class PlayerSessionListener implements Listener {

    private final AdminPanel plugin;
    private final SessionManager sessionManager;

    public PlayerSessionListener(AdminPanel plugin, SessionManager sessionManager) {
        this.plugin = plugin;
        this.sessionManager = sessionManager;
    }

    @EventHandler
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        // We can log the IP here, but the actual session recording happens on join
        // because we need a Player object
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
    }
}
