package com.adminpanel.hooks;

import com.adminpanel.AdminPanel;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Vault API integration hook.
 * Provides access to economy and permission systems.
 * Gracefully handles absence of Vault.
 */
public class VaultHook {

    private final AdminPanel plugin;
    private Economy economy = null;
    private Permission permission = null;
    private boolean enabled = false;

    public VaultHook(AdminPanel plugin) {
        this.plugin = plugin;
    }

    /**
     * Set up Vault hooks. Returns true if at least one hook was successful.
     */
    public boolean setup() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault not found. Economy & Rank features limited.");
            return false;
        }

        // Hook into Economy
        RegisteredServiceProvider<Economy> econRsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (econRsp != null) {
            economy = econRsp.getProvider();
            plugin.getLogger().info("Vault Economy hooked: " + economy.getName());
        }

        // Hook into Permissions
        RegisteredServiceProvider<Permission> permRsp = Bukkit.getServicesManager().getRegistration(Permission.class);
        if (permRsp != null) {
            permission = permRsp.getProvider();
            plugin.getLogger().info("Vault Permissions hooked: " + permission.getName());
        }

        enabled = (economy != null || permission != null);
        return enabled;
    }

    public boolean hasEconomy() {
        return economy != null;
    }

    public boolean hasPermissions() {
        return permission != null;
    }

    public Economy getEconomy() {
        return economy;
    }

    public Permission getPermission() {
        return permission;
    }

    /**
     * Set a player's primary group via Vault.
     */
    public boolean setGroup(Player player, String worldName, String group) {
        if (permission == null) return false;
        return permission.playerAddGroup(worldName, player, group);
    }

    /**
     * Get a player's primary group via Vault.
     */
    public String getGroup(Player player, String worldName) {
        if (permission == null) return "default";
        return permission.getPrimaryGroup(worldName, player);
    }

    /**
     * Add a permission to a player.
     */
    public boolean addPermission(Player player, String perm) {
        if (permission == null) return false;
        return permission.playerAdd(player, perm);
    }

    /**
     * Remove a permission from a player.
     */
    public boolean removePermission(Player player, String perm) {
        if (permission == null) return false;
        return permission.playerRemove(player, perm);
    }
}
