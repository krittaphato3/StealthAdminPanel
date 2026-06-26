package com.adminpanel.manager;

import com.adminpanel.AdminPanel;
import com.adminpanel.hooks.VaultHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages economy operations via Vault.
 * Gracefully no-ops if Vault economy is not available.
 */
public class EconomyManager {

    private final AdminPanel plugin;
    private final VaultHook vaultHook;

    public EconomyManager(AdminPanel plugin, VaultHook vaultHook) {
        this.plugin = plugin;
        this.vaultHook = vaultHook;
    }

    /**
     * Check if economy is available.
     */
    public boolean isAvailable() {
        return vaultHook != null && vaultHook.hasEconomy();
    }

    /**
     * Get a player's balance.
     */
    public double getBalance(Player player) {
        if (!isAvailable()) return 0;
        return vaultHook.getEconomy().getBalance(player);
    }

    /**
     * Get a player's balance by name.
     */
    public double getBalance(String playerName) {
        if (!isAvailable()) return 0;
        return vaultHook.getEconomy().getBalance(playerName);
    }

    /**
     * Deposit money to a player.
     */
    public boolean deposit(Player player, double amount) {
        if (!isAvailable()) return false;
        return vaultHook.getEconomy().depositPlayer(player, amount).transactionSuccess();
    }

    /**
     * Withdraw money from a player.
     */
    public boolean withdraw(Player player, double amount) {
        if (!isAvailable()) return false;
        return vaultHook.getEconomy().withdrawPlayer(player, amount).transactionSuccess();
    }

    /**
     * Set a player's balance.
     */
    public boolean setBalance(Player player, double amount) {
        if (!isAvailable()) return false;
        double current = getBalance(player);
        if (amount > current) {
            return vaultHook.getEconomy().depositPlayer(player, amount - current).transactionSuccess();
        } else if (amount < current) {
            return vaultHook.getEconomy().withdrawPlayer(player, current - amount).transactionSuccess();
        }
        return true;
    }

    /**
     * Get the top balances (leaderboard).
     * Returns a LinkedHashMap of playerName -> balance, sorted by balance descending.
     */
    public Map<String, Double> getLeaderboard(int limit) {
        Map<String, Double> leaderboard = new LinkedHashMap<>();
        if (!isAvailable()) return leaderboard;

        // Get all online players and sort by balance
        var players = new java.util.ArrayList<>(Bukkit.getOnlinePlayers());
        players.sort((a, b) -> Double.compare(getBalance(b), getBalance(a)));

        for (int i = 0; i < Math.min(limit, players.size()); i++) {
            Player p = players.get(i);
            leaderboard.put(p.getName(), getBalance(p));
        }

        return leaderboard;
    }

    /**
     * Get the formatted balance string.
     */
    public String formatBalance(double balance) {
        if (!isAvailable()) return "$0.00";
        return vaultHook.getEconomy().format(balance);
    }
}
