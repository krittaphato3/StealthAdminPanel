package com.adminpanel.command;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.MainMenu;
import com.adminpanel.listener.DeathListener;
import com.adminpanel.listener.SemiGodListener;
import com.adminpanel.manager.PermissionManager;
import com.adminpanel.util.SoundUtil;
import com.adminpanel.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stealth command with sub-command shortcuts.
 *
 * Usage:
 *   /ap                  → Opens GUI
 *   /ap unbreakable      → Toggle unbreakable on held item
 *   /ap unbreakable all  → Make all inventory items unbreakable
 *   /ap repair           → Repair held item to full durability
 *   /ap ench <name> <lvl> → Quick enchant held item
 *   /ap name <name>      → Rename held item
 *   /ap gm <mode>        → Change gamemode
 *   /ap heal [player]    → Heal player
 *   /ap feed [player]    → Feed player
 *   /ap fly              → Toggle fly
 *   /ap speed <level>    → Set walk speed
 *   /ap clear            → Clear inventory
 *   /ap god              → Toggle god mode (damage cancel + feed)
 *   /ap tp <player>      → Teleport to player
 *   /ap head <player>    → Get player head
 *   /ap anvil            → Open anvil
 */
public class StealthCommand extends Command {

    private final AdminPanel plugin;

    // God mode tracking
    private final Set<UUID> godMode = ConcurrentHashMap.newKeySet();

    public StealthCommand(AdminPanel plugin, String name) {
        super(name);
        this.plugin = plugin;
        this.setDescription("Stealth Admin Panel");
        this.setPermission(PermissionManager.USE);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (!player.hasPermission(PermissionManager.USE)) {
            player.sendMessage(plugin.getUnknownCommandMessage());
            return true;
        }

        // No args → open GUI
        if (args.length == 0) {
            Bukkit.getScheduler().runTask(plugin, () -> new MainMenu(plugin, player).open());
            return true;
        }

        // Route sub-commands
        String sub = args[0].toLowerCase();
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

        switch (sub) {
            case "unbreakable" -> handleUnbreakable(player, subArgs);
            case "repair" -> handleRepair(player);
            case "ench", "enchant" -> handleEnchant(player, subArgs);
            case "name" -> handleName(player, subArgs);
            case "gm", "gamemode" -> handleGamemode(player, subArgs);
            case "heal" -> handleHeal(player, subArgs);
            case "feed" -> handleFeed(player, subArgs);
            case "fly" -> handleFly(player);
            case "speed" -> handleSpeed(player, subArgs);
            case "clear" -> handleClear(player);
            case "god" -> handleGod(player);
            case "semigod" -> handleSemiGod(player);
            case "restore" -> handleRestore(player, subArgs);
            case "tp", "teleport" -> handleTeleport(player, subArgs);
            case "head" -> handleHead(player, subArgs);
            case "anvil" -> handleAnvil(player);
            default -> {
                player.sendMessage(TextUtil.colorize("&cUnknown sub-command: &f" + sub));
                player.sendMessage(TextUtil.colorize("&7Type &e/ap &7for GUI or &e/ap help &7for commands"));
            }
        }

        return true;
    }

    // === Sub-command handlers ===

    private void handleUnbreakable(Player player, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("all")) {
            // Make all items unbreakable
            int count = 0;
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.setUnbreakable(true);
                        item.setItemMeta(meta);
                        count++;
                    }
                }
            }
            SoundUtil.playSuccess(player);
            player.sendMessage(TextUtil.colorize("&aSet &e" + count + " &aitems to unbreakable"));
            return;
        }

        // Toggle on held item
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) {
            SoundUtil.playError(player);
            player.sendMessage(TextUtil.colorize("&cYou must hold an item!"));
            return;
        }

        ItemMeta meta = hand.getItemMeta();
        if (meta != null) {
            meta.setUnbreakable(!meta.isUnbreakable());
            hand.setItemMeta(meta);
            SoundUtil.playSuccess(player);
            player.sendMessage(TextUtil.colorize(
                    "&aUnbreakable: " + (meta.isUnbreakable() ? "&a✔ ON" : "&c✘ OFF")));
        }
    }

    private void handleRepair(Player player) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) {
            SoundUtil.playError(player);
            player.sendMessage(TextUtil.colorize("&cYou must hold an item!"));
            return;
        }

        hand.setDurability((short) 0);
        SoundUtil.playSuccess(player);
        player.sendMessage(TextUtil.colorize("&aRepaired &e" + hand.getType().name()));
    }

    private void handleEnchant(Player player, String[] args) {
        if (args.length < 2) {
            SoundUtil.playError(player);
            player.sendMessage(TextUtil.colorize("&cUsage: &e/ap ench <enchantment> <level>"));
            return;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) {
            SoundUtil.playError(player);
            player.sendMessage(TextUtil.colorize("&cYou must hold an item!"));
            return;
        }

        // Find enchantment by name (fuzzy match)
        String enchName = args[0].toLowerCase();
        Enchantment enchantment = null;
        for (Enchantment e : Enchantment.values()) {
            String key = e.getKey().toString().toLowerCase();
            String simpleName = key.contains(":") ? key.split(":")[1] : key;
            if (simpleName.contains(enchName) || e.getName().toLowerCase().contains(enchName)) {
                enchantment = e;
                break;
            }
        }

        if (enchantment == null) {
            SoundUtil.playError(player);
            player.sendMessage(TextUtil.colorize("&cEnchantment not found: &f" + args[0]));
            return;
        }

        int level;
        try {
            level = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            SoundUtil.playError(player);
            player.sendMessage(TextUtil.colorize("&cInvalid level: &f" + args[1]));
            return;
        }

        hand.addUnsafeEnchantment(enchantment, level);
        SoundUtil.playSuccess(player);
        player.sendMessage(TextUtil.colorize(
                "&aAdded &e" + enchantment.getName() + " " + level + " &ato held item"));
    }

    private void handleName(Player player, String[] args) {
        if (args.length == 0) {
            SoundUtil.playError(player);
            player.sendMessage(TextUtil.colorize("&cUsage: &e/ap name <name>"));
            return;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) {
            SoundUtil.playError(player);
            player.sendMessage(TextUtil.colorize("&cYou must hold an item!"));
            return;
        }

        String name = String.join(" ", args);
        ItemMeta meta = hand.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(TextUtil.colorize(name));
            hand.setItemMeta(meta);
            SoundUtil.playSuccess(player);
            player.sendMessage(TextUtil.colorize("&aRenamed to: &f" + TextUtil.colorize(name)));
        }
    }

    private void handleGamemode(Player player, String[] args) {
        if (args.length == 0) {
            SoundUtil.playError(player);
            player.sendMessage(TextUtil.colorize("&cUsage: &e/ap gm <survival|creative|adventure|spectator>"));
            return;
        }

        String mode = args[0].toLowerCase();
        Bukkit.dispatchCommand(player, "gamemode " + mode);
        SoundUtil.playSuccess(player);
    }

    private void handleHeal(Player player, String[] args) {
        Player target = args.length > 0 ? Bukkit.getPlayer(args[0]) : player;
        if (target == null) {
            SoundUtil.playError(player);
            player.sendMessage(TextUtil.colorize("&cPlayer not found: &f" + args[0]));
            return;
        }

        target.setHealth(target.getMaxHealth());
        target.setFoodLevel(20);
        target.setSaturation(20f);
        SoundUtil.playSuccess(player);
        player.sendMessage(TextUtil.colorize("&aHealed &e" + target.getName()));
        if (!target.equals(player)) {
            target.sendMessage(TextUtil.colorize("&aYou have been healed by an admin."));
        }
    }

    private void handleFeed(Player player, String[] args) {
        Player target = args.length > 0 ? Bukkit.getPlayer(args[0]) : player;
        if (target == null) {
            SoundUtil.playError(player);
            player.sendMessage(TextUtil.colorize("&cPlayer not found: &f" + args[0]));
            return;
        }

        target.setFoodLevel(20);
        target.setSaturation(20f);
        SoundUtil.playSuccess(player);
        player.sendMessage(TextUtil.colorize("&aFed &e" + target.getName()));
        if (!target.equals(player)) {
            target.sendMessage(TextUtil.colorize("&aYou have been fed by an admin."));
        }
    }

    private void handleFly(Player player) {
        boolean fly = !player.getAllowFlight();
        player.setAllowFlight(fly);
        player.setFlying(fly);
        SoundUtil.playSuccess(player);
        player.sendMessage(TextUtil.colorize("&aFlight: " + (fly ? "&a✔ ON" : "&c✘ OFF")));
    }

    private void handleSpeed(Player player, String[] args) {
        float speed = 1.0f;
        if (args.length > 0) {
            try {
                speed = Float.parseFloat(args[0]);
                speed = Math.max(0.1f, Math.min(10.0f, speed));
            } catch (NumberFormatException e) {
                SoundUtil.playError(player);
                player.sendMessage(TextUtil.colorize("&cInvalid speed: &f" + args[0]));
                return;
            }
        }

        player.setWalkSpeed(speed / 10.0f); // Bukkit uses 0.0-1.0 range
        SoundUtil.playSuccess(player);
        player.sendMessage(TextUtil.colorize("&aWalk speed set to &e" + speed));
    }

    private void handleClear(Player player) {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setItemInOffHand(null);
        SoundUtil.playSuccess(player);
        player.sendMessage(TextUtil.colorize("&aInventory cleared!"));
    }

    private void handleGod(Player player) {
        UUID uuid = player.getUniqueId();
        if (godMode.contains(uuid)) {
            godMode.remove(uuid);
            SoundUtil.playToggleOff(player);
            player.sendMessage(TextUtil.colorize("&cGod mode: &cOFF"));
        } else {
            godMode.add(uuid);
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            SoundUtil.playToggleOn(player);
            player.sendMessage(TextUtil.colorize("&aGod mode: &aON"));
        }
    }

    private void handleSemiGod(Player player) {
        boolean wasEnabled = SemiGodListener.isSemiGod(player.getUniqueId());
        boolean nowEnabled = SemiGodListener.toggleSemiGod(player);

        if (nowEnabled) {
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            SoundUtil.playToggleOn(player);
            player.sendMessage(TextUtil.colorize("&aSemi God Mode: &aON"));
            player.sendMessage(TextUtil.colorize("&7You take no damage but still see effects (knockback, red tint, sounds)"));
        } else {
            SoundUtil.playToggleOff(player);
            player.sendMessage(TextUtil.colorize("&cSemi God Mode: &cOFF"));
        }
    }

    private void handleRestore(Player player, String[] args) {
        if (args.length == 0) {
            // Show list of players with saved drops
            var allDrops = DeathListener.getAllSavedDrops();
            if (allDrops.isEmpty()) {
                SoundUtil.playError(player);
                player.sendMessage(TextUtil.colorize("&cNo saved inventories to restore."));
                return;
            }

            player.sendMessage(TextUtil.colorize("&6Players with saved inventories:"));
            for (var entry : allDrops.entrySet()) {
                org.bukkit.OfflinePlayer offline = Bukkit.getOfflinePlayer(entry.getKey());
                String name = offline.getName() != null ? offline.getName() : entry.getKey().toString().substring(0, 8);
                int itemCount = entry.getValue().size();
                String world = DeathListener.getDeathWorld(entry.getKey());
                long deathTimeMs = DeathListener.getDeathTime(entry.getKey());
                String timeAgo = formatTimeAgo(System.currentTimeMillis() - deathTimeMs);

                player.sendMessage(TextUtil.colorize(
                        "&e" + name + " &7- &f" + itemCount + " items &7- &f" + world + " &7- &f" + timeAgo + " ago"));
            }
            player.sendMessage(TextUtil.colorize("&7Usage: &e/ap restore <player>"));
            return;
        }

        // Restore a specific player's items
        String targetName = args[0];
        org.bukkit.OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        UUID targetUUID = target.getUniqueId();

        if (!DeathListener.hasDrops(targetUUID)) {
            SoundUtil.playError(player);
            player.sendMessage(TextUtil.colorize("&cNo saved inventory found for &e" + targetName));
            return;
        }

        DeathListener.RestoreResult result = DeathListener.retrieveDrops(targetUUID);
        if (result == null) {
            SoundUtil.playError(player);
            player.sendMessage(TextUtil.colorize("&cNo items to restore for &e" + targetName));
            return;
        }

        List<ItemStack> drops = result.getItems();
        List<String> warnings = result.getWarnings();

        // Show warnings first
        if (result.hasWarnings()) {
            player.sendMessage(TextUtil.colorize("&6Restore warnings for &e" + targetName + "&6:"));
            for (String warning : warnings) {
                player.sendMessage(TextUtil.colorize("  " + warning));
            }
            player.sendMessage("");
        }

        if (drops.isEmpty()) {
            SoundUtil.playError(player);
            player.sendMessage(TextUtil.colorize("&cNo restorable items for &e" + targetName + " &c(all destroyed)."));
            return;
        }

        // Give items
        Player targetPlayer = Bukkit.getPlayer(targetUUID);
        if (targetPlayer != null && targetPlayer.isOnline()) {
            for (ItemStack item : drops) {
                targetPlayer.getInventory().addItem(item);
            }
            SoundUtil.playSuccess(player);
            player.sendMessage(TextUtil.colorize("&aRestored &f" + drops.size() + " items &ato &e" + target.getName()));
            targetPlayer.sendMessage(TextUtil.colorize("&aAn admin has restored your items!"));
        } else {
            for (ItemStack item : drops) {
                player.getInventory().addItem(item);
            }
            SoundUtil.playSuccess(player);
            player.sendMessage(TextUtil.colorize("&e" + target.getName() + " &7is offline. Items given to your inventory."));
        }

        plugin.getAuditManager().log(player, "ITEM_RESTORE", targetName,
                "Restored " + drops.size() + " items" +
                (result.hasWarnings() ? " (with warnings)" : ""));
    }

    private String formatTimeAgo(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + "d " + (hours % 24) + "h";
        if (hours > 0) return hours + "h " + (minutes % 60) + "m";
        if (minutes > 0) return minutes + "m " + (seconds % 60) + "s";
        return seconds + "s";
    }

    private void handleTeleport(Player player, String[] args) {
        if (args.length == 0) {
            SoundUtil.playError(player);
            player.sendMessage(TextUtil.colorize("&cUsage: &e/ap tp <player>"));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            SoundUtil.playError(player);
            player.sendMessage(TextUtil.colorize("&cPlayer not found: &f" + args[0]));
            return;
        }

        player.teleport(target.getLocation());
        SoundUtil.playTeleport(player);
        player.sendMessage(TextUtil.colorize("&aTeleported to &e" + target.getName()));
    }

    private void handleHead(Player player, String[] args) {
        if (args.length == 0) {
            SoundUtil.playError(player);
            player.sendMessage(TextUtil.colorize("&cUsage: &e/ap head <player>"));
            return;
        }

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = head.getItemMeta();
        if (meta instanceof org.bukkit.inventory.meta.SkullMeta skull) {
            skull.setOwner(args[0]);
            meta.setDisplayName(TextUtil.colorize("&e" + args[0] + "'s Head"));
            head.setItemMeta(meta);
        }

        player.getInventory().addItem(head);
        SoundUtil.playSuccess(player);
        player.sendMessage(TextUtil.colorize("&aGave you &e" + args[0] + "&a's head"));
    }

    private void handleAnvil(Player player) {
        player.closeInventory();
        Bukkit.getScheduler().runTask(plugin, () ->
                player.openInventory(Bukkit.createInventory(null, 27,
                        TextUtil.colorize("&0&lAnvil"))));
        SoundUtil.playOpen(player);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (!(sender instanceof Player player)) return Collections.emptyList();
        if (!player.hasPermission(PermissionManager.USE)) return Collections.emptyList();

        if (args.length == 1) {
            List<String> completions = new ArrayList<>(Arrays.asList(
                    "unbreakable", "repair", "ench", "name", "gm",
                    "heal", "feed", "fly", "speed", "clear",
                    "god", "semigod", "restore", "tp", "head", "anvil"
            ));
            return filterCompletions(completions, args[0]);
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            return switch (sub) {
                case "unbreakable" -> filterCompletions(List.of("all"), args[1]);
                case "ench", "enchant" -> {
                    List<String> enchNames = new ArrayList<>();
                    for (Enchantment e : Enchantment.values()) {
                        String key = e.getKey().toString();
                        enchNames.add(key.contains(":") ? key.split(":")[1] : key);
                    }
                    yield filterCompletions(enchNames, args[1]);
                }
                case "gm", "gamemode" -> filterCompletions(
                        List.of("survival", "creative", "adventure", "spectator"), args[1]);
                case "heal", "feed", "tp", "teleport", "head", "restore" -> {
                    List<String> names = new ArrayList<>();
                    for (Player p : Bukkit.getOnlinePlayers()) names.add(p.getName());
                    yield filterCompletions(names, args[1]);
                }
                case "speed" -> filterCompletions(List.of("1", "2", "5", "10"), args[1]);
                default -> Collections.emptyList();
            };
        }

        return Collections.emptyList();
    }

    private List<String> filterCompletions(List<String> options, String input) {
        List<String> result = new ArrayList<>();
        for (String opt : options) {
            if (opt.toLowerCase().startsWith(input.toLowerCase())) {
                result.add(opt);
            }
        }
        return result;
    }
}
