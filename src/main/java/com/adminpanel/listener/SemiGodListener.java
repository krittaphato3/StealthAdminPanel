package com.adminpanel.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Semi God Mode listener.
 *
 * When enabled, the player:
 * - Takes NO actual damage (all damage cancelled)
 * - Still sees realistic damage effects (red flash, knockback, particles, sounds)
 * - Knockback pushes AWAY from attacker (correct direction)
 * - Fall damage shows visual effects (camera shake, particles)
 * - Lava/fire effects have cooldown to prevent rapid-fire loops
 * - Hunger immunity
 */
public class SemiGodListener implements Listener {

    private static final Set<UUID> semiGodPlayers = ConcurrentHashMap.newKeySet();

    // Cooldown between visual effects (ms) to prevent rapid-fire loops (lava, fire)
    private static final long EFFECT_COOLDOWN_MS = 500;
    private static final Map<UUID, Long> effectCooldowns = new ConcurrentHashMap<>();

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
     * Check if the player is on cooldown for visual effects.
     */
    private boolean isOnCooldown(UUID uuid) {
        Long last = effectCooldowns.get(uuid);
        if (last == null) return false;
        return System.currentTimeMillis() - last < EFFECT_COOLDOWN_MS;
    }

    /**
     * Update the cooldown timestamp.
     */
    private void updateCooldown(UUID uuid) {
        effectCooldowns.put(uuid, System.currentTimeMillis());
    }

    /**
     * Trigger visual feedback: red flash, particles, sound.
     */
    private void playDamageEffects(Player player, double damage) {
        UUID uuid = player.getUniqueId();

        // Skip if on cooldown (prevents lava/fire rapid-fire loops)
        if (isOnCooldown(uuid)) return;
        updateCooldown(uuid);

        // Red flash via Paper API
        try {
            player.playHurtAnimation(player.getLocation().getYaw());
        } catch (Exception ignored) {
            // Fallback: particles only
            player.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR,
                    player.getLocation().add(0, 1, 0), 8, 0.3, 0.3, 0.3, 0.1);
        }

        // Hit sound (lower volume for ambient damage like lava)
        player.getWorld().playSound(player.getLocation(),
                org.bukkit.Sound.ENTITY_PLAYER_HURT, 0.4f, 1.0f);
    }

    /**
     * Handle entity attacks — cancel damage, apply correct knockback.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!semiGodPlayers.contains(player.getUniqueId())) return;

        event.setCancelled(true);

        double damage = event.getFinalDamage();
        if (damage > 0) {
            // Visual effects
            playDamageEffects(player, damage);

            // Correct knockback: push AWAY from attacker
            if (event.getDamager() != null) {
                // Direction: from attacker TO player (away from attacker)
                org.bukkit.util.Vector direction = player.getLocation().toVector()
                        .subtract(event.getDamager().getLocation().toVector())
                        .normalize();

                // Scale knockback by damage amount
                double knockbackStrength = Math.min(damage * 0.15, 0.8);

                org.bukkit.util.Vector knockback = direction.multiply(knockbackStrength).setY(0.3);
                player.setVelocity(player.getVelocity().add(knockback));
            }
        }
    }

    /**
     * Handle all non-entity damage: fall, lava, fire, void, drowning, suffocation, etc.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGenericDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) return; // handled above
        if (!(event.getEntity() instanceof Player player)) return;
        if (!semiGodPlayers.contains(player.getUniqueId())) return;

        event.setCancelled(true);

        double damage = event.getFinalDamage();
        if (damage > 0) {
            EntityDamageEvent.DamageCause cause = event.getCause();

            // Visual effects
            playDamageEffects(player, damage);

            // Specific effects based on damage cause
            switch (cause) {
                case FALL -> {
                    // Fall damage: camera shake + ground particles
                    player.getWorld().spawnParticle(Particle.BLOCK,
                            player.getLocation(), 15, 0.5, 0.1, 0.5, 0.1,
                            player.getLocation().getBlock().getType().createBlockData());
                    // Small upward bounce for "landing" feel
                    player.setVelocity(player.getVelocity().setY(0.1));
                }
                case LAVA, FIRE, FIRE_TICK -> {
                    // Lava/fire: smoke particles + sizzle sound
                    player.getWorld().spawnParticle(Particle.SMOKE,
                            player.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0.02);
                    player.getWorld().playSound(player.getLocation(),
                            org.bukkit.Sound.BLOCK_FIRE_EXTINGUISH, 0.3f, 1.5f);
                }
                case VOID -> {
                    // Void: dark particles + ominous sound
                    player.getWorld().spawnParticle(Particle.SMOKE,
                            player.getLocation(), 20, 0.5, 0.5, 0.5, 0.05);
                    player.getWorld().playSound(player.getLocation(),
                            org.bukkit.Sound.AMBIENT_CAVE, 0.5f, 0.5f);
                }
                case DROWNING -> {
                    // Drowning: bubble particles
                    player.getWorld().spawnParticle(Particle.BUBBLE,
                            player.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.1);
                }
                case SUFFOCATION -> {
                    // Suffocation: brief darkness
                    player.getWorld().spawnParticle(Particle.BLOCK,
                            player.getLocation().add(0, 1.8, 0), 5, 0.2, 0.2, 0.2, 0,
                            Material.STONE.createBlockData());
                }
                default -> {
                    // Generic: just particles
                    player.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR,
                            player.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0.1);
                }
            }
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
