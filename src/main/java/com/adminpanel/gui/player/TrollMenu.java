package com.adminpanel.gui.player;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.base.SubMenu;
import com.adminpanel.util.ItemBuilder;
import com.adminpanel.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Troll menu for a selected player.
 * Options: Smite (lightning), Slap (damage), Freeze, Fake Death, Disorient.
 */
public class TrollMenu extends SubMenu {

    private final Player target;
    private boolean frozen = false;

    public TrollMenu(AdminPanel plugin, Player player, Player target) {
        super(plugin, player, "&0&lTroll: " + target.getName(), 3);
        this.target = target;
    }

    @Override
    protected void buildMenu() {
        setItem(10, Material.BLAZE_ROD,
                "&e&l⚡ Smite",
                "&7Strike target with lightning");

        setItem(11, Material.IRON_SWORD,
                "&c&l🗡 Slap",
                "&7Deal 3 hearts of damage");

        setItem(12, Material.BLUE_ICE,
                "&b&l❄ Freeze",
                "&7Toggle movement freeze");

        setItem(13, Material.WITHER_SKELETON_SKULL,
                "&4&l💀 Fake Death",
                "&7Play death animation + sound");

        setItem(14, Material.FERMENTED_SPIDER_EYE,
                "&5&l😵 Disorient",
                "&7Blindness + Nausea + Slowness");

        setItem(15, Material.SLIME_BALL,
                "&a&l bounce",
                "&7Launch target into the air");

        setItem(16, Material.LAVA_BUCKET,
                "&6&l🔥 Lava Bath",
                "&7Place lava at target's feet");

        addBackButton();
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null || item.getType() == Material.AIR) return;

        switch (slot) {
            case 10 -> {
                // Smite — strike lightning
                target.getWorld().strikeLightningEffect(target.getLocation());
                player.sendMessage(TextUtil.colorize("&eSmited " + target.getName() + " with lightning!"));
                plugin.getAuditManager().log(player, "TROLL_SMITE", target.getName(), "Lightning strike");
            }
            case 11 -> {
                // Slap — deal damage
                target.damage(6.0); // 3 hearts
                player.sendMessage(TextUtil.colorize("&cSlapped " + target.getName() + " for 3 hearts!"));
                plugin.getAuditManager().log(player, "TROLL_SLAP", target.getName(), "6.0 damage");
            }
            case 12 -> {
                // Freeze toggle
                frozen = !frozen;
                if (frozen) {
                    target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 255, false, false));
                    target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 200, false, false));
                    player.sendMessage(TextUtil.colorize("&bFroze " + target.getName() + "!"));
                } else {
                    target.removePotionEffect(PotionEffectType.SLOWNESS);
                    target.removePotionEffect(PotionEffectType.JUMP_BOOST);
                    player.sendMessage(TextUtil.colorize("&bUnfroze " + target.getName() + "!"));
                }
                plugin.getAuditManager().log(player, "TROLL_FREEZE", target.getName(),
                        frozen ? "Frozen" : "Unfrozen");
            }
            case 13 -> {
                // Fake death — play death animation + sound
                target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1.0f, 1.0f);
                // Send death message
                Bukkit.broadcastMessage(TextUtil.colorize("&r" + target.getName() + " was slain by Admin"));
                player.sendMessage(TextUtil.colorize("&4Played fake death for " + target.getName()));
                plugin.getAuditManager().log(player, "TROLL_FAKE_DEATH", target.getName(), "Death animation");
            }
            case 14 -> {
                // Disorient — blindness + nausea + slowness
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 1, false, false));
                target.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 200, 1, false, false));
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 200, 2, false, false));
                player.sendMessage(TextUtil.colorize("&5Disoriented " + target.getName() + "!"));
                plugin.getAuditManager().log(player, "TROLL_DISORIENT", target.getName(), "Blind+Nausea+Slow");
            }
            case 15 -> {
                // Bounce — launch into air
                Location loc = target.getLocation();
                loc.setY(loc.getY() + 0.5);
                target.teleport(loc);
                target.setVelocity(target.getLocation().getDirection().multiply(0).setY(2.5));
                player.sendMessage(TextUtil.colorize("&aLaunched " + target.getName() + " into the air!"));
                plugin.getAuditManager().log(player, "TROLL_BOUNCE", target.getName(), "Launched upward");
            }
            case 16 -> {
                // Lava bath
                Location loc = target.getLocation();
                loc.getBlock().setType(Material.LAVA);
                player.sendMessage(TextUtil.colorize("&6Placed lava at " + target.getName() + "'s feet!"));
                // Remove lava after 2 seconds
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    loc.getBlock().setType(Material.AIR);
                }, 40L);
                plugin.getAuditManager().log(player, "TROLL_LAVA", target.getName(), "Lava placement");
            }
            case 45 -> {
                // Back
                new PlayerActionMenu(plugin, player, target).open();
            }
        }
    }
}
