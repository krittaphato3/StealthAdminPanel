package com.adminpanel.listener;

import com.adminpanel.AdminPanel;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Handles execute-on-use command bindings for items.
 *
 * Items can have PersistentDataContainer tag "adminpanel:command" which specifies a command
 * to execute when the item is used. Supports:
 * - Right-click air/block: Execute command
 * - Arrow hit entity: Execute command on hit target
 * - Melee hit: Execute command on hit target
 *
 * Placeholders: %player%, %target%
 */
public class ItemUseListener implements Listener {

    private final AdminPanel plugin;
    private final NamespacedKey commandKey;

    public ItemUseListener(AdminPanel plugin) {
        this.plugin = plugin;
        this.commandKey = new NamespacedKey(plugin, "command");
    }

    /**
     * Handle right-click use of bound items.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        String command = getBoundCommand(item);
        if (command == null) return;

        event.setCancelled(true);
        String executed = command.replace("%player%", player.getName());
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), executed);
    }

    /**
     * Handle arrow/projectile hit with bound items.
     */
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player shooter)) return;

        Projectile projectile = event.getEntity();
        ItemStack bow = shooter.getInventory().getItemInMainHand();

        String command = getBoundCommand(bow);
        if (command == null) {
            command = getBoundCommand(shooter.getInventory().getItemInOffHand());
        }
        if (command == null) return;

        if (event.getHitEntity() == null) {
            String executed = command
                    .replace("%player%", shooter.getName())
                    .replace("%target%", shooter.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), executed);
        }
    }

    /**
     * Handle melee/entity hit with bound items.
     */
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;

        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        String command = getBoundCommand(weapon);
        if (command == null) return;

        String targetName = event.getEntity().getName();
        String executed = command
                .replace("%player%", attacker.getName())
                .replace("%target%", targetName);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), executed);
    }

    /**
     * Extract the bound command from an item's PersistentDataContainer.
     *
     * @return The command string, or null if no command is bound
     */
    private String getBoundCommand(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        if (container.has(commandKey, PersistentDataType.STRING)) {
            return container.get(commandKey, PersistentDataType.STRING);
        }
        return null;
    }
}
