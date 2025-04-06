package net.ririfa.igf

import org.bukkit.entity.Player

/**
 * A class that provides paginated inventory GUI functionality with improved item management.
 * It allows dynamic page navigation, slot management, and customization using PersistentDataContainer.
 *
 * @param player The player who will view the GUI.
 * @see InventoryGUI
 * @since 1.0.0
 * @author RiriFa
 */
@Suppress("unused")
class PaginatedGUI(
    player: Player
) : InventoryGUI(player) {
    private var currentPage = 0
    private var itemsPerPage = 9
    private var totalPages = 1
    private var slotPositions: List<Int> = emptyList()
    private var emptyMessageButton: Button? = null
    private var pageItems: List<Button> = emptyList()

    private var prevPageButton: Button? = null
    private var nextPageButton: Button? = null

    override fun build(): PaginatedGUI {
        create()
        displayItemsForPage()
        return this
    }

    /**
     * Sets the slot positions for items.
     * @param slots The list of slot positions for the items.
     * @return This [PaginatedGUI] instance.
     */
    fun setSlotPositions(slots: List<Int>): PaginatedGUI {
        this.slotPositions = slots
        return this
    }

    /**
     * Sets the items to be paginated.
     * @param items The list of items to display across multiple pages.
     * @return This [PaginatedGUI] instance.
     */
    fun setPageItems(items: List<Button>): PaginatedGUI {
        this.pageItems = items
        setTotalPages(items.size)
        return this
    }

    /**
     * Sets a default button to display when there are no items.
     * @param button The button to display when there are no items.
     */
    fun setEmptyMessageButton(button: Button): PaginatedGUI {
        this.emptyMessageButton = button
        return this
    }

    /**
     * Sets the total number of pages.
     * This method should be called after setting up items.
     */
    fun setTotalPages(totalItems: Int): PaginatedGUI {
        this.totalPages = (totalItems + itemsPerPage - 1) / itemsPerPage
        return this
    }

    /**
     * Updates the inventory to display items for the current page.
     *
     * This method clears the existing inventory content, applies the background,
     * and populates the inventory with items specific to the current page. It
     * also sets up navigation buttons if applicable.
     *
     * The method performs the following actions:
     * - Clears the inventory and applies a background.
     * - If there are no items to display, the empty message button is set.
     * - Items for the current page are added to the inventory based on
     *   the defined slot positions.
     * - Navigation buttons (previous/next) are added if the conditions
     *   for navigation are met.
     *
     * Navigation buttons are only displayed when the respective conditions
     * for their visibility (e.g., not on the first or last page) are met.
     */
    fun displayItemsForPage() {
        inventory.clear()
        applyBackground()

        items.forEach { item -> inventory.setItem(item.slot, item.toItemStack()) }

        if (pageItems.isEmpty()) {
            emptyMessageButton?.let { inventory.setItem(it.slot, it.toItemStack()) }
            return
        }

        val startIndex = currentPage * itemsPerPage
        val endIndex = (startIndex + itemsPerPage).coerceAtMost(pageItems.size)

        pageItems.subList(startIndex, endIndex).forEachIndexed { index, button ->
            slotPositions.getOrNull(index)?.let { slot ->
                inventory.setItem(slot, button.toItemStack())
            }
        }

        listOfNotNull(
            prevPageButton?.takeIf { currentPage > 0 },
            nextPageButton?.takeIf { currentPage < totalPages - 1 }
        ).forEach { button ->
            inventory.setItem(button.slot, button.toItemStack())
        }
    }

    /**
     * Sets the buttons for page navigation.
     * @param prevButton The button to navigate to the previous page.
     * @param nextButton The button to navigate to the next page.
     * @return This [PaginatedGUI] instance.
     * @throws IllegalStateException If custom IDs aren't set for page navigation.
     */
    fun setPageButtons(prevButton: Button, nextButton: Button): PaginatedGUI {
        prevPageButton = prevButton.setClick(this) { _, _ -> prevPage() }
        nextPageButton = nextButton.setClick(this) { _, _ -> nextPage() }
        return this
    }

    /**
     * Sets the number of items per page.
     * @param itemsPerPage The number of items to display per page.
     *
     * @return This [PaginatedGUI] instance.
     */
    fun setItemsPerPage(itemsPerPage: Int): PaginatedGUI {
        this.itemsPerPage = itemsPerPage
        setTotalPages(pageItems.size)
        return this
    }

    /**
     * Sets the current page for the inventory.
     * @param page The page number to set.
     * @return This [PaginatedGUI] instance.
     */
    fun setPage(page: Int): PaginatedGUI {
        val newPage = page.coerceIn(0, totalPages - 1)
        if (newPage != currentPage) {
            currentPage = newPage
            displayItemsForPage()
        }
        return this
    }

    fun prevPage() = setPage(currentPage - 1)
    fun nextPage() = setPage(currentPage + 1)
}
