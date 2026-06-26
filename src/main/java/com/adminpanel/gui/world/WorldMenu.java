package com.adminpanel.gui.world;

import com.adminpanel.AdminPanel;
import com.adminpanel.gui.MainMenu;
import com.adminpanel.gui.base.SubMenu;
import com.adminpanel.util.ColorUtil;
import com.adminpanel.util.ItemBuilder;
import com.adminpanel.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * World configuration menu — toggle game rules, weather, time.
 * Shows green/red wool for boolean states.
 */
public class WorldMenu extends SubMenu {

    private final World world;

    public WorldMenu(AdminPanel plugin, Player player) {
        super(plugin, player, "&0&lWorld Configuration", 6);
        this.world = player.getWorld();
    }

    @Override
    protected void buildMenu() {
        // Row 1: Time & Weather
        setItem(10, Material.CLOCK,
                "&e&lTime: &f" + getTimeName(),
                "&7Current: &f" + world.getTime() + " ticks",
                "&7Click to cycle: Day → Night → Dawn");

        boolean isStorming = world.isStorming();
        setItem(12, isStorming ? Material.BLUE_WOOL : Material.YELLOW_WOOL,
                isStorming ? "&9&lWeather: STORM" : "&e&lWeather: CLEAR",
                "&7Click to toggle weather");

        setItem(13, Material.SUNFLOWER,
                "&6&lSet to Day",
                "&7Set time to 0 (noon)");

        setItem(14, Material.CRYING_OBSIDIAN,
                "&5&lSet to Night",
                "&7Set time to 13000 (midnight)");

        // Row 2: GameRule toggles
        boolean pvp = world.getGameRuleValue(org.bukkit.GameRule.PVP).equalsIgnoreCase("true");
        setItem(19, pvp ? Material.LIME_WOOL : Material.RED_WOOL,
                ColorUtil.toggleName("PVP", pvp),
                "&7Click to toggle",
                "&7Status: " + ColorUtil.stateIndicator(pvp));

        boolean mobSpawn = world.getGameRuleValue(org.bukkit.GameRule.DO_MOB_SPAWNING).equalsIgnoreCase("true");
        setItem(20, mobSpawn ? Material.LIME_WOOL : Material.RED_WOOL,
                ColorUtil.toggleName("Mob Spawning", mobSpawn),
                "&7Click to toggle",
                "&7Status: " + ColorUtil.stateIndicator(mobSpawn));

        boolean daylight = world.getGameRuleValue(org.bukkit.GameRule.DO_DAYLIGHT_CYCLE).equalsIgnoreCase("true");
        setItem(21, daylight ? Material.LIME_WOOL : Material.RED_WOOL,
                ColorUtil.toggleName("Daylight Cycle", daylight),
                "&7Click to toggle",
                "&7Status: " + ColorUtil.stateIndicator(daylight));

        boolean weatherCycle = world.getGameRuleValue(org.bukkit.GameRule.DO_WEATHER_CYCLE).equalsIgnoreCase("true");
        setItem(22, weatherCycle ? Material.LIME_WOOL : Material.RED_WOOL,
                ColorUtil.toggleName("Weather Cycle", weatherCycle),
                "&7Click to toggle",
                "&7Status: " + ColorUtil.stateIndicator(weatherCycle));

        boolean fireTick = world.getGameRuleValue(org.bukkit.GameRule.DO_FIRE_TICK).equalsIgnoreCase("true");
        setItem(23, fireTick ? Material.LIME_WOOL : Material.RED_WOOL,
                ColorUtil.toggleName("Fire Tick", fireTick),
                "&7Click to toggle",
                "&7Status: " + ColorUtil.stateIndicator(fireTick));

        boolean dropItems = world.getGameRuleValue(org.bukkit.GameRule.DO_TILE_DROPS).equalsIgnoreCase("true");
        setItem(24, dropItems ? Material.LIME_WOOL : Material.RED_WOOL,
                ColorUtil.toggleName("Tile Drops", dropItems),
                "&7Click to toggle",
                "&7Status: " + ColorUtil.stateIndicator(dropItems));

        // Row 3: More game rules
        boolean keepInv = world.getGameRuleValue(org.bukkit.GameRule.KEEP_INVENTORY).equalsIgnoreCase("true");
        setItem(28, keepInv ? Material.LIME_WOOL : Material.RED_WOOL,
                ColorUtil.toggleName("Keep Inventory", keepInv),
                "&7Click to toggle",
                "&7Status: " + ColorUtil.stateIndicator(keepInv));

        boolean mobGrief = world.getGameRuleValue(org.bukkit.GameRule.MOB_GRIEFING).equalsIgnoreCase("true");
        setItem(29, mobGrief ? Material.LIME_WOOL : Material.RED_WOOL,
                ColorUtil.toggleName("Mob Griefing", mobGrief),
                "&7Click to toggle",
                "&7Status: " + ColorUtil.stateIndicator(mobGrief));

        boolean announceAdv = world.getGameRuleValue(org.bukkit.GameRule.ANNOUNCE_ADVANCEMENTS).equalsIgnoreCase("true");
        setItem(30, announceAdv ? Material.LIME_WOOL : Material.RED_WOOL,
                ColorUtil.toggleName("Announce Advancements", announceAdv),
                "&7Click to toggle",
                "&7Status: " + ColorUtil.stateIndicator(announceAdv));

        boolean commandBlock = world.getGameRuleValue(org.bukkit.GameRule.COMMAND_BLOCK_OUTPUT).equalsIgnoreCase("true");
        setItem(31, commandBlock ? Material.LIME_WOOL : Material.RED_WOOL,
                ColorUtil.toggleName("Command Block Output", commandBlock),
                "&7Click to toggle",
                "&7Status: " + ColorUtil.stateIndicator(commandBlock));

        boolean doImmediateRespawn = world.getGameRuleValue(org.bukkit.GameRule.DO_IMMEDIATE_RESPAWN).equalsIgnoreCase("true");
        setItem(32, doImmediateRespawn ? Material.LIME_WOOL : Material.RED_WOOL,
                ColorUtil.toggleName("Immediate Respawn", doImmediateRespawn),
                "&7Click to toggle",
                "&7Status: " + ColorUtil.stateIndicator(doImmediateRespawn));

        boolean showDeathMessages = world.getGameRuleValue(org.bukkit.GameRule.SHOW_DEATH_MESSAGES).equalsIgnoreCase("true");
        setItem(33, showDeathMessages ? Material.LIME_WOOL : Material.RED_WOOL,
                ColorUtil.toggleName("Show Death Messages", showDeathMessages),
                "&7Click to toggle",
                "&7Status: " + ColorUtil.stateIndicator(showDeathMessages));

        // Row 4: World info
        setItem(37, Material.GRASS_BLOCK,
                "&2&lWorld Info",
                "&7Name: &f" + world.getName(),
                "&7Difficulty: &f" + world.getDifficulty(),
                "&7Players: &f" + world.getPlayers().size(),
                "&7Entities: &f" + world.getEntityCount(),
                "&7Chunks: &f" + world.getLoadedChunks().length);

        addBackButton();
    }

    @Override
    public void onItemClick(Player player, ItemStack item, int slot) {
        if (item == null || item.getType() == Material.AIR) return;

        switch (slot) {
            case 10 -> {
                // Cycle time
                long currentTime = world.getTime();
                if (currentTime < 6000) {
                    world.setTime(13000); // Set to night
                } else if (currentTime < 18000) {
                    world.setTime(0); // Set to day
                } else {
                    world.setTime(6000); // Set to dawn
                }
                player.sendMessage(TextUtil.colorize("&aTime set to " + world.getTime()));
                plugin.getAuditManager().log(player, "WORLD_TIME", world.getName(),
                        "Time: " + world.getTime());
                refresh();
            }
            case 12 -> {
                // Toggle weather
                world.setStorm(!world.isStorming());
                player.sendMessage(TextUtil.colorize("&aWeather toggled to " +
                        (world.isStorming() ? "Storm" : "Clear")));
                plugin.getAuditManager().log(player, "WORLD_WEATHER", world.getName(),
                        world.isStorming() ? "Storm" : "Clear");
                refresh();
            }
            case 13 -> {
                world.setTime(0);
                player.sendMessage(TextUtil.colorize("&aSet time to Day"));
                refresh();
            }
            case 14 -> {
                world.setTime(13000);
                player.sendMessage(TextUtil.colorize("&aSet time to Night"));
                refresh();
            }
            // GameRule toggles
            case 19 -> toggleGameRule(player, org.bukkit.GameRule.PVP);
            case 20 -> toggleGameRule(player, org.bukkit.GameRule.DO_MOB_SPAWNING);
            case 21 -> toggleGameRule(player, org.bukkit.GameRule.DO_DAYLIGHT_CYCLE);
            case 22 -> toggleGameRule(player, org.bukkit.GameRule.DO_WEATHER_CYCLE);
            case 23 -> toggleGameRule(player, org.bukkit.GameRule.DO_FIRE_TICK);
            case 24 -> toggleGameRule(player, org.bukkit.GameRule.DO_TILE_DROPS);
            case 28 -> toggleGameRule(player, org.bukkit.GameRule.KEEP_INVENTORY);
            case 29 -> toggleGameRule(player, org.bukkit.GameRule.MOB_GRIEFING);
            case 30 -> toggleGameRule(player, org.bukkit.GameRule.ANNOUNCE_ADVANCEMENTS);
            case 31 -> toggleGameRule(player, org.bukkit.GameRule.COMMAND_BLOCK_OUTPUT);
            case 32 -> toggleGameRule(player, org.bukkit.GameRule.DO_IMMEDIATE_RESPAWN);
            case 33 -> toggleGameRule(player, org.bukkit.GameRule.SHOW_DEATH_MESSAGES);
            case 45 -> new MainMenu(plugin, player).open();
        }
    }

    private void toggleGameRule(Player player, org.bukkit.GameRule<Boolean> rule) {
        boolean current = world.getGameRuleValue(rule);
        world.setGameRule(rule, !current);
        player.sendMessage(TextUtil.colorize("&a" + rule.getName() + " set to " + (!current)));
        plugin.getAuditManager().log(player, "GAMERULE", world.getName(),
                rule.getName() + " = " + (!current));
        refresh();
    }

    private String getTimeName() {
        long time = world.getTime();
        if (time < 6000) return "Dawn";
        if (time < 12000) return "Day";
        if (time < 13000) return "Sunset";
        if (time < 18000) return "Night";
        return "Midnight";
    }
}
