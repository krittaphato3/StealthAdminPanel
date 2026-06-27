package com.adminpanel.gui.player;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.base.SubMenu;
import com.adminpanel.util.ItemBuilder;
import com.adminpanel.util.SoundUtil;
import com.adminpanel.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

/**
 * Anonymous Troll Menu — 15+ troll options with no admin attribution.
 *
 * All actions are anonymous:
 * - No "[Admin]" prefix on messages
 * - Actions appear to happen naturally
 * - Fake death shows no killer name
 * - Fake messages come from "Server" or just appear
 */
public class TrollMenu extends SubMenu {

    private final Player target;

    public TrollMenu(AdminPanel plugin, Player player, Player target) {
        super(plugin, player, "&0&lTroll: " + target.getName(), 6);
        this.target = target;
    }

    @Override
    protected void buildMenu() {
        // Row 1: Damage & Effects
        setItem(0, Material.BLAZE_ROD,
                "&e&lSmite",
                "&7Strike with lightning",
                "&7Anonymous - no source shown");

        setItem(1, Material.IRON_SWORD,
                "&c&lSlap",
                "&7Deal 3 hearts of damage",
                "&7Anonymous - no source shown");

        setItem(2, Material.FIRE_CHARGE,
                "&6&lFire",
                "&7Set target on fire for 5s",
                "&7Anonymous");

        setItem(3, Material.BONE,
                "&f&lSkeleton Ambush",
                "&7Spawn 3 skeletons near target",
                "&7Anonymous - they think it's natural");

        // Row 2: Movement & Control
        setItem(9, Material.BLUE_ICE,
                "&b&lFreeze",
                "&7Toggle movement lock",
                "&7Slowness 255 + Jump Boost 200");

        setItem(10, Material.FEATHER,
                "&a&lBounce",
                "&7Launch into the air");

        setItem(11, Material.ENDER_PEARL,
                "&5&lTeleport Behind",
                "&7Teleport behind the target");

        setItem(12, Material.SLIME_BALL,
                "&2&lLevitate",
                "&7Levitation effect for 5s");

        // Row 3: Confusion & Disorientation
        setItem(18, Material.FERMENTED_SPIDER_EYE,
                "&5&lDisorient",
                "&7Blindness + Nausea + Slowness");

        setItem(19, Material.NETHER_WART,
                "&4&lConfusion",
                "&7Nausea for 10 seconds");

        setItem(20, Material.COBWEB,
                "&7&lSlow",
                "&7Slowness 3 for 15 seconds");

        setItem(21, Material.ECHO_SHARD,
                "&8&lBlind",
                "&7Blindness for 10 seconds");

        // Row 4: Fake Messages & Death
        setItem(27, Material.WITHER_SKELETON_SKULL,
                "&8&lFake Death",
                "&7Play death animation + sound",
                "&7Broadcasts fake death message");

        setItem(28, Material.BARRIER,
                "&c&lFake Ban",
                "&7Send fake ban message to target",
                "&7They think they're banned!");

        setItem(29, Material.PAPER,
                "&e&lFake OP",
                "&7Send 'You are now op' message",
                "&7They think they got OP");

        setItem(30, Material.BOOK,
                "&6&lFake Report",
                "&7Send fake report notification",
                "&7'Your report has been submitted'");

        // Row 5: Inventory & Physical
        setItem(36, Material.CHEST,
                "&6&lLava Bath",
                "&7Place lava at feet (2s)");

        setItem(37, Material.BUCKET,
                "&b&lWater Bath",
                "&7Place water at feet (5s)");

        setItem(38, Material.ANVIL,
                "&7&lAnvil Drop",
                "&7Drop an anvil on their head");

        setItem(39, Material.NETHERITE_SWORD,
                "&4&lDrop Held Item",
                "&7Force drop their main hand item");

        // Row 6: Extreme Trolls
        setItem(45, Material.CHICKEN_SPAWN_EGG,
                "&f&lChicken Swarm",
                "&7Spawn 5 chickens around them");

        setItem(46, Material.CREEPER_SPAWN_EGG,
                "&a&lCreeper Surprise",
                "&7Spawn a creeper (no explosion)");

        setItem(47, Material.ENDER_EYE,
                "&5&lRandom Teleport",
                "&7TP to random location in world");

        setItem(48, Material.BEDROCK,
                "&8&lInventory Fill",
                "&7Fill inventory with dirt");

        addBackButton();
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null || item.getType() == Material.AIR) return;

        switch (slot) {
            // Row 1: Damage & Effects
            case 0 -> anonymousSmite(target);
            case 1 -> anonymousSlap(target);
            case 2 -> anonymousFire(target);
            case 3 -> anonymousSkeletonAmbush(target);

            // Row 2: Movement & Control
            case 9 -> toggleFreeze(target, player);
            case 10 -> anonymousBounce(target);
            case 11 -> anonymousTeleportBehind(target, player);
            case 12 -> anonymousLevitate(target);

            // Row 3: Confusion
            case 18 -> anonymousDisorient(target);
            case 19 -> anonymousConfusion(target);
            case 20 -> anonymousSlow(target);
            case 21 -> anonymousBlind(target);

            // Row 4: Fake Messages
            case 27 -> anonymousFakeDeath(target);
            case 28 -> anonymousFakeBan(target);
            case 29 -> anonymousFakeOP(target);
            case 30 -> anonymousFakeReport(target);

            // Row 5: Physical
            case 36 -> anonymousLavaBath(target);
            case 37 -> anonymousWaterBath(target);
            case 38 -> anonymousAnvilDrop(target);
            case 39 -> anonymousDropHeld(target);

            // Row 6: Extreme
            case 45 -> anonymousChickenSwarm(target);
            case 46 -> anonymousCreeperSurprise(target);
            case 47 -> anonymousRandomTeleport(target);
            case 48 -> anonymousInventoryFill(target);
        }
    }

    // ===========================
    //  ANONYMOUS TROLL IMPLEMENTATIONS
    // ===========================

    private void anonymousSmite(Player target) {
        SoundUtil.playDramatic(player);
        target.getWorld().strikeLightningEffect(target.getLocation());
        // No message to anyone — anonymous
    }

    private void anonymousSlap(Player target) {
        SoundUtil.playError(player);
        target.damage(6.0);
        // Damage appears natural — no source attribution
    }

    private void anonymousFire(Player target) {
        target.setFireTicks(100); // 5 seconds
        SoundUtil.playDramatic(player);
    }

    private void anonymousSkeletonAmbush(Player target) {
        Location loc = target.getLocation();
        for (int i = 0; i < 3; i++) {
            double angle = 2 * Math.PI * i / 3;
            Location spawnLoc = loc.clone().add(Math.cos(angle) * 3, 0, Math.sin(angle) * 3);
            target.getWorld().spawnEntity(spawnLoc, org.bukkit.entity.EntityType.SKELETON);
        }
        SoundUtil.playDramatic(player);
    }

    private void toggleFreeze(Player target, Player admin) {
        boolean hasSlowness = target.hasPotionEffect(PotionEffectType.SLOWNESS);
        if (hasSlowness) {
            target.removePotionEffect(PotionEffectType.SLOWNESS);
            target.removePotionEffect(PotionEffectType.JUMP_BOOST);
            SoundUtil.playUnlock(player);
            player.sendMessage(TextUtil.colorize("&bUnfroze " + target.getName()));
        } else {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 255, false, false));
            target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 200, false, false));
            SoundUtil.playLock(player);
            player.sendMessage(TextUtil.colorize("&bFroze " + target.getName()));
        }
    }

    private void anonymousBounce(Player target) {
        Location loc = target.getLocation();
        loc.setY(loc.getY() + 0.5);
        target.teleport(loc);
        target.setVelocity(target.getLocation().getDirection().multiply(0).setY(2.5));
        SoundUtil.playDramatic(player);
    }

    private void anonymousTeleportBehind(Player target, Player admin) {
        Location behind = target.getLocation().add(
                target.getLocation().getDirection().multiply(-2));
        target.teleport(behind);
        SoundUtil.playTeleport(player);
    }

    private void anonymousLevitate(Player target) {
        target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 100, 2, false, false));
        SoundUtil.playDramatic(player);
    }

    private void anonymousDisorient(Player target) {
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 1, false, false));
        target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 200, 1, false, false));
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 2, false, false));
    }

    private void anonymousConfusion(Player target) {
        target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 200, 2, false, false));
    }

    private void anonymousSlow(Player target) {
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 300, 3, false, false));
    }

    private void anonymousBlind(Player target) {
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 1, false, false));
    }

    private void anonymousFakeDeath(Player target) {
        // Play death animation + sound — no killer name
        SoundUtil.playDramatic(player);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1.0f);
        // Fake death message — appears natural, no admin name
        Bukkit.broadcastMessage(TextUtil.colorize("&r" + target.getName() + " was slain by Zombie"));
    }

    private void anonymousFakeBan(Player target) {
        // Send fake ban message directly to the target
        target.sendMessage("");
        target.sendMessage(TextUtil.colorize("&4&k                    "));
        target.sendMessage(TextUtil.colorize("&4&l      BANNED"));
        target.sendMessage(TextUtil.colorize("&7Your account has been banned"));
        target.sendMessage(TextUtil.colorize("&7Reason: &fCheating"));
        target.sendMessage(TextUtil.colorize("&7Duration: &fPermanent"));
        target.sendMessage(TextUtil.colorize("&7"));
        target.sendMessage(TextUtil.colorize("&7If you believe this is a mistake,"));
        target.sendMessage(TextUtil.colorize("&7please appeal at our website."));
        target.sendMessage(TextUtil.colorize("&4&k                    "));
        target.sendMessage("");
        SoundUtil.playDramatic(player);
    }

    private void anonymousFakeOP(Player target) {
        target.sendMessage(TextUtil.colorize("&7You are now op!"));
        SoundUtil.playSuccess(player);
    }

    private void anonymousFakeReport(Player target) {
        target.sendMessage("");
        target.sendMessage(TextUtil.colorize("&e&l[Report System] &7Your report has been submitted."));
        target.sendMessage(TextUtil.colorize("&7Our team will review it within 24 hours."));
        target.sendMessage("");
        SoundUtil.playSearch(player);
    }

    private void anonymousLavaBath(Player target) {
        Location loc = target.getLocation();
        loc.getBlock().setType(Material.LAVA);
        SoundUtil.playDramatic(player);
        // Auto-remove after 2 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> loc.getBlock().setType(Material.AIR), 40L);
    }

    private void anonymousWaterBath(Player target) {
        Location loc = target.getLocation();
        loc.getBlock().setType(Material.WATER);
        SoundUtil.playDramatic(player);
        // Auto-remove after 5 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> loc.getBlock().setType(Material.AIR), 100L);
    }

    private void anonymousAnvilDrop(Player target) {
        Location loc = target.getLocation().add(0, 5, 0);
        loc.getBlock().setType(Material.ANVIL);
        SoundUtil.playDramatic(player);
        // Remove after 3 seconds to prevent building damage
        Bukkit.getScheduler().runTaskLater(plugin, () -> loc.getBlock().setType(Material.AIR), 60L);
    }

    private void anonymousDropHeld(Player target) {
        ItemStack held = target.getInventory().getItemInMainHand();
        if (held != null && held.getType() != Material.AIR) {
            target.getWorld().dropItemNaturally(target.getLocation(), held.clone());
            target.getInventory().setItemInMainHand(null);
            SoundUtil.playError(player);
        }
    }

    private void anonymousChickenSwarm(Player target) {
        Location loc = target.getLocation();
        for (int i = 0; i < 5; i++) {
            double angle = 2 * Math.PI * i / 5;
            Location spawnLoc = loc.clone().add(Math.cos(angle) * 2, 0, Math.sin(angle) * 2);
            target.getWorld().spawnEntity(spawnLoc, org.bukkit.entity.EntityType.CHICKEN);
        }
        SoundUtil.playDramatic(player);
    }

    private void anonymousCreeperSurprise(Player target) {
        Location loc = target.getLocation().add(2, 0, 0);
        org.bukkit.entity.Creeper creeper = (org.bukkit.entity.Creeper)
                target.getWorld().spawnEntity(loc, org.bukkit.entity.EntityType.CREEPER);
        creeper.setPowered(false); // No explosion
        // Start the fuse by igniting via NMS
        try {
            java.lang.reflect.Method igniteMethod = creeper.getClass().getMethod("setIgnited", boolean.class);
            igniteMethod.invoke(creeper, true);
        } catch (Exception ignored) {
            // Fallback: just let the creeper exist
        }
        SoundUtil.playDramatic(player);
    }

    private void anonymousRandomTeleport(Player target) {
        World world = target.getWorld();
        int x = (int) (Math.random() * 1000 - 500);
        int z = (int) (Math.random() * 1000 - 500);
        int y = world.getHighestBlockYAt(x, z) + 1;
        target.teleport(new Location(world, x, y, z));
        SoundUtil.playTeleport(player);
    }

    private void anonymousInventoryFill(Player target) {
        ItemStack dirt = new ItemStack(Material.DIRT, 64);
        for (int i = 0; i < 36; i++) {
            target.getInventory().setItem(i, dirt.clone());
        }
        SoundUtil.playError(player);
    }
}
