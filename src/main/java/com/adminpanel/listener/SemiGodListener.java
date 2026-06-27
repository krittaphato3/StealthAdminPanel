package com.adminpanel.listener;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Semi God Mode listener.
 *
 * When enabled, the player:
 * - Takes NO actual damage (all damage cancelled)
 * - Still sees damage effects (red flash via NMS packet, knockback, particles, sounds)
 * - Is immune to fall damage, fire, drowning, void, etc.
 * - Is immune to hunger (food level stays full)
 * - Gets knockback pushed but health stays full
 *
 * Uses NMS packets for the red flash to avoid infinite damage event loops.
 */
public class SemiGodListener implements Listener {

    private static final Set<UUID> semiGodPlayers = ConcurrentHashMap.newKeySet();

    // Cached NMS methods for hurt animation
    private static Method playHurtSoundMethod;
    private static Method getHandleMethod;
    private static boolean nmsAvailable = false;

    static {
        try {
            // Paper 1.21+ has playHurtAnimation on Player
            getHandleMethod = Bukkit.getPlayer("").getClass().getMethod("getHandle");
        } catch (Exception ignored) {}

        // Try Paper's playHurtAnimation(float yaw) method
        try {
            Method method = Player.class.getMethod("playHurtAnimation", float.class);
            playHurtSoundMethod = method;
            nmsAvailable = true;
        } catch (NoSuchMethodException e) {
            // Spigot doesn't have playHurtAnimation, fall back to particles only
            nmsAvailable = false;
        }
    }

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
     * Trigger the red flash animation on a player.
     * Uses Paper's playHurtAnimation if available, otherwise particles only.
     */
    private static void playHurtAnimation(Player player) {
        if (nmsAvailable && playHurtSoundMethod != null) {
            try {
                playHurtSoundMethod.invoke(player, player.getLocation().getYaw());
            } catch (Exception ignored) {
                // Fallback to particles
                playHurtParticles(player);
            }
        } else {
            playHurtParticles(player);
        }
    }

    /**
     * Fallback: red damage particles when NMS is not available.
     */
    private static void playHurtParticles(Player player) {
        player.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR,
                player.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.1);
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
            // Red flash via NMS packet (safe, no event recursion)
            playHurtAnimation(player);

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
            // Red flash via NMS packet
            playHurtAnimation(player);

            // Hit sound
            player.getWorld().playSound(player.getLocation(),
                    org.bukkit.Sound.ENTITY_PLAYER_HURT, 0.3f, 1.0f);
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
