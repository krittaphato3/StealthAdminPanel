package com.adminpanel.listener;

import com.adminpanel.AdminPanel;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Overrides item damage based on custom NBT attributes.
 *
 * Items can have NBT data "AdminPanel:Damage" to set custom attack damage.
 * This overrides the vanilla damage calculation for that item.
 */
public class DamageListener implements Listener {

    private final AdminPanel plugin;

    public DamageListener(AdminPanel plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;

        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        if (weapon == null) return;

        try {
            NBTItem nbtItem = new NBTItem(weapon);

            // Check for custom damage override
            if (nbtItem.hasKey("AdminPanel:Damage")) {
                double customDamage = nbtItem.getDouble("AdminPanel:Damage");
                event.setDamage(customDamage);
            }

            // Check for custom knockback
            if (nbtItem.hasKey("AdminPanel:Knockback")) {
                double knockback = nbtItem.getDouble("AdminPanel:Knockback");
                // Apply extra knockback
                event.setDamage(event.getDamage() + knockback);
            }
        } catch (Exception ignored) {
            // NBT API not available for this item type
        }
    }
}
