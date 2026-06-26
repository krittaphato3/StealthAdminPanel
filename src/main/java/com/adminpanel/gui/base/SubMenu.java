package com.adminpanel.gui.base;

import com.adminpanel.AdminPanel;
import com.adminpanel.util.ItemBuilder;
import com.adminpanel.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 * Base class for non-paginated sub-menus.
 * Single-page menus that don't need pagination.
 *
 * Subclasses populate items in buildMenu() and handle clicks in onItemClick().
 */
public abstract class SubMenu implements InventoryHolder {

    protected final AdminPanel plugin;
    protected final Player player;
    protected final Inventory inventory;

    private boolean initialized = false;

    public SubMenu(AdminPanel plugin, Player player, String title, int rows) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, rows * 9, TextUtil.colorize(title));
        // NOTE: buildMenu() is NOT called here — subclass fields aren't set yet.
        // It is called in open() instead, after the subclass constructor completes.
    }

    /**
     * Build the menu by placing items into the inventory.
     * Called during construction.
     */
    protected abstract void buildMenu();

    /**
     * Handle a click on an item in the menu.
     * Checks for back button first, then delegates to onItemClick.
     *
     * @param player  The player who clicked
     * @param item    The clicked item
     * @param slot    The raw slot number
     */
    public void handleMenuClick(Player player, ItemStack item, int slot) {
        // Intercept back button clicks
        if (slot == getBackSlot()) {
            onBackClick();
            return;
        }
        onItemClick(player, item, slot);
    }

    /**
     * Handle a click on an item in the menu. Subclasses implement this.
     * Note: Back button is handled by handleMenuClick() — you don't need to handle it here.
     *
     * @param player  The player who clicked
     * @param item    The clicked item
     * @param slot    The raw slot number
     */
    public abstract void onItemClick(Player player, ItemStack item, int slot);

    /**
     * Handle the back button click. Override for custom back navigation.
     */
    protected void onBackClick() {
        player.closeInventory();
    }

    /**
     * Set an item in the inventory at a specific slot.
     */
    protected void setItem(int slot, ItemStack item) {
        inventory.setItem(slot, item);
    }

    /**
     * Set an item with a name and lore.
     */
    protected void setItem(int slot, Material material, String name, String... lore) {
        setItem(slot, new ItemBuilder(material).name(name).lore(lore).build());
    }

    /**
     * Fill empty slots with placeholder panes.
     */
    protected void fillPlaceholders() {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                inventory.setItem(i, ItemBuilder.placeholder());
            }
        }
    }

    /**
     * Add a standard back button at the bottom-left.
     */
    protected void addBackButton() {
        int lastRow = inventory.getSize() - 9;
        setItem(lastRow, new ItemBuilder(Material.ARROW)
                .name("&c&l← Back")
                .lore("&7Return to previous menu")
                .build());
    }

    /**
     * Refresh the menu by clearing and rebuilding.
     */
    public void refresh() {
        inventory.clear();
        buildMenu();
    }

    /**
     * Open this inventory for the player.
     * Builds the menu first to ensure subclass fields are initialized.
     */
    public void open() {
        if (!initialized) {
            buildMenu();
            initialized = true;
        }
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Get the slot of the back button (bottom-left of last row).
     */
    protected int getBackSlot() {
        return inventory.getSize() - 9;
    }

    /**
     * Check if a slot is the back button.
     */
    protected boolean isBackSlot(int slot) {
        return slot == getBackSlot();
    }

}
