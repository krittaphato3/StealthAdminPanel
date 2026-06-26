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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Base class for paginated GUI menus.
 *
 * Layout:
 * - Rows 0-4 (slots 0-44): Item grid, 45 items per page
 * - Row 5 (slots 45-53): Control bar
 *   - Slot 45: Previous page
 *   - Slot 49: Search (AnvilGUI)
 *   - Slot 53: Next page
 *   - Slots 46-48, 50-52: Back button / filler
 *
 * Subclasses must implement:
 * - getPageItems(): Return all items that should be displayed
 * - onItemClick(Player, ItemStack, int): Handle click on an item
 * - getTitle(): Return the inventory title
 */
public abstract class PaginationGUI implements InventoryHolder {

    protected final AdminPanel plugin;
    protected final Player player;
    protected final Inventory inventory;
    protected final List<ItemStack> allItems = new ArrayList<>();
    protected List<ItemStack> filteredItems = new ArrayList<>();
    protected int currentPage = 0;
    protected String searchFilter = "";
    protected int totalPages = 1;

    private static final int ITEMS_PER_PAGE = 45; // 5 rows × 9 columns
    private static final int PAGE_SIZE = 54;       // 6 rows × 9 columns

    private boolean initialized = false;

    public PaginationGUI(AdminPanel plugin, Player player, String title) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, PAGE_SIZE, TextUtil.colorize(title));
        // NOTE: loadItems() and render() are NOT called here — subclass fields aren't set yet.
        // They are called in open() instead, after the subclass constructor completes.
    }

    /**
     * Load all items into the buffer. Called on construction and refresh.
     * Subclasses populate allItems here.
     */
    protected void loadItems() {
        allItems.clear();
        allItems.addAll(getPageItems());
        applyFilter();
    }

    /**
     * Subclasses return all items that should appear in the paginated list.
     */
    protected abstract List<ItemStack> getPageItems();

    /**
     * Handle a click on an item in the grid.
     *
     * @param player  The player who clicked
     * @param item    The clicked item
     * @param slot    The raw slot number
     */
    public abstract void onItemClick(Player player, ItemStack item, int slot);

    /**
     * Get the inventory title.
     */
    public abstract String getMenuTitle();

    /**
     * Get the items per page (default 45, can be overridden).
     */
    protected int getItemsPerPage() {
        return ITEMS_PER_PAGE;
    }

    /**
     * Whether this menu has a search feature.
     */
    protected boolean hasSearch() {
        return true;
    }

    /**
     * Whether this menu has a back button.
     */
    protected boolean hasBackButton() {
        return true;
    }

    /**
     * Handle the back button click. Override to provide custom back navigation.
     */
    public void onBackClick() {
        player.closeInventory();
    }

    /**
     * Handle the search button click. Override to provide custom search behavior.
     */
    protected void onSearchClick() {
        // Default: open AnvilGUI for text search
        player.closeInventory();
        // AnvilGUI integration is handled by GUIClickListener
    }

    /**
     * Apply the current search filter to items.
     */
    protected void applyFilter() {
        if (searchFilter.isEmpty()) {
            filteredItems = new ArrayList<>(allItems);
        } else {
            filteredItems = new ArrayList<>();
            String lower = searchFilter.toLowerCase();
            for (ItemStack item : allItems) {
                if (item == null || item.getType() == Material.AIR) continue;
                if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                    String name = TextUtil.stripColor(item.getItemMeta().getDisplayName());
                    if (name.toLowerCase().contains(lower)) {
                        filteredItems.add(item);
                    }
                }
            }
        }
        totalPages = Math.max(1, (int) Math.ceil((double) filteredItems.size() / getItemsPerPage()));
        if (currentPage >= totalPages) {
            currentPage = Math.max(0, totalPages - 1);
        }
    }

    /**
     * Set the search filter and re-render.
     */
    public void setSearchFilter(String filter) {
        this.searchFilter = filter != null ? filter : "";
        this.currentPage = 0;
        applyFilter();
        render();
    }

    /**
     * Get the current search filter.
     */
    public String getSearchFilter() {
        return searchFilter;
    }

    /**
     * Get the filtered items list.
     */
    public List<ItemStack> getFilteredItems() {
        return filteredItems;
    }

    /**
     * Re-load items and re-render.
     */
    public void refresh() {
        loadItems();
        render();
    }

    /**
     * Render the current page of items into the inventory.
     */
    protected void render() {
        // Clear inventory
        inventory.clear();

        int startIndex = currentPage * getItemsPerPage();
        int endIndex = Math.min(startIndex + getItemsPerPage(), filteredItems.size());

        // Fill item grid (rows 0-4)
        for (int i = startIndex; i < endIndex; i++) {
            int slot = i - startIndex;
            if (slot < getItemsPerPage()) {
                inventory.setItem(slot, filteredItems.get(i));
            }
        }

        // Fill empty grid slots with placeholder
        for (int i = endIndex - startIndex; i < getItemsPerPage(); i++) {
            inventory.setItem(i, ItemBuilder.placeholder());
        }

        // Render control bar (row 5)
        renderControlBar();
    }

    /**
     * Render the bottom control bar with navigation buttons.
     */
    protected void renderControlBar() {
        // Slot 45: Previous page (if not on first page)
        if (currentPage > 0) {
            inventory.setItem(45, new ItemBuilder(Material.ARROW)
                    .name("&e&l← Previous Page")
                    .lore("&7Page " + currentPage + " of " + totalPages)
                    .build());
        } else {
            inventory.setItem(45, ItemBuilder.placeholder());
        }

        // Slot 46-48: Filler or back button
        if (hasBackButton()) {
            inventory.setItem(46, new ItemBuilder(Material.ARROW)
                    .name("&c&l← Back")
                    .lore("&7Return to previous menu")
                    .build());
        }
        inventory.setItem(47, ItemBuilder.placeholder());
        inventory.setItem(48, ItemBuilder.placeholder());

        // Slot 49: Search (if search enabled)
        if (hasSearch()) {
            String searchName = searchFilter.isEmpty() ? "&b&l🔍 Search" : "&b&l🔍 Search: &f" + searchFilter;
            inventory.setItem(49, new ItemBuilder(Material.NAME_TAG)
                    .name(searchName)
                    .lore("&7Click to search/filter items",
                          "&7Current filter: &f" + (searchFilter.isEmpty() ? "None" : searchFilter))
                    .build());
        }

        // Slot 50-52: Filler
        inventory.setItem(50, ItemBuilder.placeholder());
        inventory.setItem(51, ItemBuilder.placeholder());

        // Page info in slot 52
        inventory.setItem(52, new ItemBuilder(Material.PAPER)
                .name("&7Page &e" + (currentPage + 1) + "&7/&e" + totalPages)
                .lore("&7Total items: &f" + filteredItems.size())
                .build());

        // Slot 53: Next page (if not on last page)
        if (currentPage < totalPages - 1) {
            inventory.setItem(53, new ItemBuilder(Material.ARROW)
                    .name("&e&lNext Page →")
                    .lore("&7Page " + (currentPage + 2) + " of " + totalPages)
                    .build());
        } else {
            inventory.setItem(53, ItemBuilder.placeholder());
        }
    }

    /**
     * Navigate to the next page.
     */
    public void nextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            render();
        }
    }

    /**
     * Navigate to the previous page.
     */
    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            render();
        }
    }

    /**
     * Open this inventory for the player.
     * Builds the menu first to ensure subclass fields are initialized.
     */
    public void open() {
        if (!initialized) {
            loadItems();
            render();
            initialized = true;
        }
        player.openInventory(inventory);
    }

    /**
     * Get the current page number (0-based).
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * Get the total number of pages.
     */
    public int getTotalPages() {
        return totalPages;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Check if a slot is in the control bar (row 5).
     */
    public boolean isControlBarSlot(int slot) {
        return slot >= 45 && slot <= 53;
    }

    /**
     * Get the back button slot.
     */
    protected int getBackSlot() {
        return 46;
    }

    /**
     * Get the search button slot.
     */
    protected int getSearchSlot() {
        return 49;
    }
}
