package com.adminpanel.listener;

import com.adminpanel.AdminPanel;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Overrides item damage based on custom PersistentDataContainer attributes.
 *
 * Items can have "adminpanel:damage" tag to set custom attack damage.
 * This overrides the vanilla damage calculation for that item.
 */
public class DamageListener implements Listener {

    private final AdminPanel plugin;
    private final NamespacedKey damageKey;

    public DamageListener(AdminPanel plugin) {
        this.plugin = plugin;
        this.damageKey = new NamespacedKey(plugin, "damage");
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;

        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        if (weapon == null || !weapon.hasItemMeta()) return;

        PersistentDataContainer container = weapon.getItemMeta().getPersistentDataContainer();

        // Check for custom damage override
        if (container.has(damageKey, PersistentDataType.DOUBLE)) {
            double customDamage = container.get(damageKey, PersistentDataType.DOUBLE);
            event.setDamage(customDamage);
        }
    }
}
