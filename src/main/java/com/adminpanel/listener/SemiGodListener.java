package com.adminpanel.listener;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Semi God Mode listener — realistic damage simulation.
 *
 * - No actual damage taken
 * - Knockback pushes AWAY from attacker, scales with damage
 * - Sound pitch/volume scales with damage amount
 * - Different effects per damage type
 * - Cooldown system prevents rapid-fire loops (lava, fire, suffocation)
 * - Entity attacks feel weighty based on weapon used
 */
public class SemiGodListener implements Listener {

    private static final Set<UUID> semiGodPlayers = ConcurrentHashMap.newKeySet();

    // Separate cooldowns for different damage categories
    private static final long ENTITY_HIT_COOLDOWN_MS = 300;   // Fast — attacks feel responsive
    private static final long AMBIENT_COOLDOWN_MS = 800;       // Slow — lava, fire, suffocation
    private static final Map<UUID, Long> entityCooldowns = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> ambientCooldowns = new ConcurrentHashMap<>();

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

    private boolean isEntityOnCooldown(UUID uuid) {
        Long last = entityCooldowns.get(uuid);
        return last != null && System.currentTimeMillis() - last < ENTITY_HIT_COOLDOWN_MS;
    }

    private boolean isAmbientOnCooldown(UUID uuid) {
        Long last = ambientCooldowns.get(uuid);
        return last != null && System.currentTimeMillis() - last < AMBIENT_COOLDOWN_MS;
    }

    // ===========================
    //  ENTITY ATTACKS (fast cooldown)
    // ===========================

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!semiGodPlayers.contains(player.getUniqueId())) return;

        event.setCancelled(true);

        double damage = event.getFinalDamage();
        if (damage <= 0) return;

        UUID uuid = player.getUniqueId();
        if (isEntityOnCooldown(uuid)) return;
        entityCooldowns.put(uuid, System.currentTimeMillis());

        // --- Red flash ---
        try {
            player.playHurtAnimation(player.getLocation().getYaw());
        } catch (Exception ignored) {}

        // --- Sound scales with damage (higher damage = louder + higher pitch) ---
        float volume = (float) Math.min(0.3 + (damage / 20.0), 1.0);
        float pitch = (float) Math.min(0.8 + (damage / 40.0), 1.5);
        player.getWorld().playSound(player.getLocation(),
                Sound.ENTITY_PLAYER_HURT, volume, pitch);

        // --- Knockback: correct direction + scales with damage ---
        if (event.getDamager() != null) {
            org.bukkit.util.Vector direction = player.getLocation().toVector()
                    .subtract(event.getDamager().getLocation().toVector())
                    .normalize();

            double strength = Math.min(damage * 0.12, 0.7);
            org.bukkit.util.Vector knockback = direction.multiply(strength).setY(0.25);
            player.setVelocity(player.getVelocity().add(knockback));
        }

        // --- Particles scale with damage ---
        int particleCount = (int) Math.min(3 + damage, 20);
        player.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR,
                player.getLocation().add(0, 1, 0), particleCount,
                0.3, 0.3, 0.3, 0.1);

        // --- Weapon-specific effects ---
        if (event.getDamager() instanceof Player attacker) {
            ItemStack weapon = attacker.getInventory().getItemInMainHand();
            Material type = weapon.getType();

            if (type.name().contains("AXE")) {
                // Axe: extra particles + heavier sound
                player.getWorld().spawnParticle(Particle.CRIT,
                        player.getLocation().add(0, 1, 0), 8, 0.3, 0.3, 0.3, 0.2);
            } else if (type == Material.TRIDENT) {
                // Trident: electric particles
                player.getWorld().spawnParticle(Particle.ELECTRIC_SPARK,
                        player.getLocation().add(0, 1, 0), 12, 0.3, 0.3, 0.3, 0.1);
            } else if (type.name().contains("SWORD")) {
                // Sword: slash particles
                player.getWorld().spawnParticle(Particle.SWEEP_ATTACK,
                        player.getLocation().add(0, 1, 0), 3, 0.5, 0.3, 0.5, 0.1);
            } else if (type == Material.BOW || type == Material.CROSSBOW) {
                // Arrow: impact particles
                player.getWorld().spawnParticle(Particle.CRIT,
                        player.getLocation().add(0, 1, 0), 10, 0.2, 0.2, 0.2, 0.3);
            } else {
                // Fist: minimal particles
                player.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR,
                        player.getLocation().add(0, 1, 0), 5, 0.2, 0.2, 0.2, 0.1);
            }
        }
    }

    // ===========================
    //  AMBIENT DAMAGE (slow cooldown — lava, fire, fall, etc.)
    // ===========================

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGenericDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!semiGodPlayers.contains(player.getUniqueId())) return;

        event.setCancelled(true);

        double damage = event.getFinalDamage();
        if (damage <= 0) return;

        UUID uuid = player.getUniqueId();
        if (isAmbientOnCooldown(uuid)) return;
        ambientCooldowns.put(uuid, System.currentTimeMillis());

        EntityDamageEvent.DamageCause cause = event.getCause();

        // --- Red flash ---
        try {
            player.playHurtAnimation(player.getLocation().getYaw());
        } catch (Exception ignored) {}

        // --- Effect based on cause ---
        switch (cause) {
            case FALL -> {
                int particles = (int) Math.min(5 + damage * 2, 30);
                player.getWorld().spawnParticle(Particle.BLOCK,
                        player.getLocation(), particles, 0.5, 0.1, 0.5, 0.1,
                        player.getLocation().getBlock().getType().createBlockData());
                player.getWorld().playSound(player.getLocation(),
                        Sound.ENTITY_PLAYER_HURT, 0.5f, 0.8f);
                player.setVelocity(player.getVelocity().setY(0.1));
            }
            case LAVA, FIRE, FIRE_TICK -> {
                player.getWorld().spawnParticle(Particle.SMOKE,
                        player.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0.02);
                player.getWorld().playSound(player.getLocation(),
                        Sound.BLOCK_FIRE_EXTINGUISH, 0.2f, 1.5f);
                player.getWorld().playSound(player.getLocation(),
                        Sound.ENTITY_PLAYER_HURT, 0.3f, 1.2f);
            }
            case VOID -> {
                player.getWorld().spawnParticle(Particle.SMOKE,
                        player.getLocation(), 20, 0.5, 0.5, 0.5, 0.05);
                player.getWorld().playSound(player.getLocation(),
                        Sound.AMBIENT_CAVE, 0.5f, 0.5f);
            }
            case DROWNING -> {
                player.getWorld().spawnParticle(Particle.BUBBLE,
                        player.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.1);
                player.getWorld().playSound(player.getLocation(),
                        Sound.ENTITY_PLAYER_HURT, 0.4f, 0.9f);
            }
            case SUFFOCATION -> {
                player.getWorld().spawnParticle(Particle.BLOCK,
                        player.getLocation().add(0, 1.8, 0), 5, 0.2, 0.2, 0.2, 0,
                        Material.STONE.createBlockData());
                player.getWorld().playSound(player.getLocation(),
                        Sound.ENTITY_PLAYER_HURT, 0.3f, 0.8f);
            }
            case CONTACT -> {
                // Cactus, sweet berry, etc.
                player.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR,
                        player.getLocation().add(0, 1, 0), 5, 0.2, 0.2, 0.2, 0.1);
                player.getWorld().playSound(player.getLocation(),
                        Sound.ENTITY_PLAYER_HURT, 0.4f, 1.3f);
            }
            default -> {
                player.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR,
                        player.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0.1);
                player.getWorld().playSound(player.getLocation(),
                        Sound.ENTITY_PLAYER_HURT, 0.3f, 1.0f);
            }
        }
    }

    // ===========================
    //  HUNGER IMMUNITY
    // ===========================

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
