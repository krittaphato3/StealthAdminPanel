package com.adminpanel.listener;

import com.adminpanel.AdminPanel;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Semi God Mode listener.
 *
 * When enabled, the player:
 * - Takes NO actual damage (all damage cancelled)
 * - Still sees damage effects (red flash, knockback, particles, sounds)
 * - Is immune to fall damage, fire, drowning, void, etc.
 * - Is immune to hunger (food level stays full)
 * - Gets knockback pushed but health stays full
 */
public class SemiGodListener implements Listener {

    private static final Set<UUID> semiGodPlayers = ConcurrentHashMap.newKeySet();

    public static boolean isSemiGod(UUID playerUUID) {
        return semiGodPlayers.contains(playerUUID);
    }

    public static boolean toggleSemiGod(Player player) {
        UUID uuid = player.getUniqueId();
        if (semiGodPlayers.contains(uuid)) {
            semiGodPlayers.remove(uuid);
            return false;
        } else {
            semiGodPlayers.add(uuid);
            return true;
        }
    }

    public static Set<UUID> getSemiGodPlayers() {
        return semiGodPlayers;
    }

    /**
     * Handle entity damage — cancel damage but apply visual feedback.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!semiGodPlayers.contains(player.getUniqueId())) return;

        event.setCancelled(true);

        double damage = event.getFinalDamage();
        if (damage > 0) {
            // Red damage particles
            player.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR,
                    player.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.1);

            // Knockback from attacker
            if (event.getDamager() != null) {
                org.bukkit.util.Vector knockback = event.getDamager().getLocation()
                        .toVector().subtract(player.getLocation().toVector())
                        .normalize().multiply(0.5).setY(0.3);
                player.setVelocity(player.getVelocity().add(knockback));
            }

            // Hit sound
            player.getWorld().playSound(player.getLocation(),
                    org.bukkit.Sound.ENTITY_PLAYER_HURT, 0.5f, 1.0f);

            // Schedule red flash on next tick (avoids event recursion)
            Bukkit.getScheduler().runTask(AdminPanel.getInstance(), () -> {
                if (player.isOnline()) {
                    player.damage(0.001);
                }
            });
        }
    }

    /**
     * Handle non-entity damage (fall, fire, void, etc.) — cancel but show effects.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGenericDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) return; // handled above
        if (!(event.getEntity() instanceof Player player)) return;
        if (!semiGodPlayers.contains(player.getUniqueId())) return;

        event.setCancelled(true);

        double damage = event.getFinalDamage();
        if (damage > 0) {
            // Damage particles
            player.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR,
                    player.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0.1);

            // Hit sound
            player.getWorld().playSound(player.getLocation(),
                    org.bukkit.Sound.ENTITY_PLAYER_HURT, 0.3f, 1.0f);

            // Schedule red flash on next tick
            Bukkit.getScheduler().runTask(AdminPanel.getInstance(), () -> {
                if (player.isOnline()) {
                    player.damage(0.001);
                }
            });
        }
    }

    /**
     * Cancel hunger damage.
     */
    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!semiGodPlayers.contains(player.getUniqueId())) return;

        if (event.getFoodLevel() < 20) {
            event.setCancelled(true);
            player.setFoodLevel(20);
            player.setSaturation(20f);
        }
    }
}
