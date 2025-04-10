package net.ririfa.igf

import org.bukkit.entity.Player
import org.jetbrains.annotations.ApiStatus
import kotlin.reflect.KClass

/**
 * A generic class used to create a paginated GUI in Minecraft, allowing dynamic switching between
 * pages and states mapped to an enum type.
 * It supports custom item mappings, navigation buttons,
 * and empty state messages to provide a flexible inventory GUI system.
 *
 * @param S the enum type for state management must extend Enum
 * @param enumClass the class of the enum used for managing GUI states
 * @param player the player associated with the GUI
 */
class PaginatedDynamicGUI<S : Enum<S>>(
    private val enumClass: Class<S>,
    player: Player
) : InventoryGUI(player) {
    private val stateFixedButtonProviders: MutableMap<S, (S) -> List<Button>> = mutableMapOf()

    /**
     * Represents the current state of the paginated GUI.
     * This variable tracks the currently active state, allowing the GUI
     * to dynamically update its content and layout based on the specified state.
     *
     */
    var currentState: S? = null
        private set
    /**
     * Represents the current page being displayed in the paginated GUI.
     *
     * This variable is used to track and manage the navigation between different pages within a paginated dynamic graphical user interface.
     * Modifying this value directly updates the displayed page and related methods*/
    var currentPage: Int = 0
        private set
    /**
     * Represents the list of slot indexes used for displaying items in the paginated GUI.
     * Each integer in the list corresponds to a slot position in the GUI inventory.
     * These positions determine where items will be placed for display on the current page.
     */
    var slotPositions: List<Int> = emptyList()
        private set
    /**
     * Represents the maximum number of items to be displayed per page in the paginated GUI.
     * This value determines how many items can be shown on a single page before pagination is applied.
     */
    var itemsPerPage: Int = 9
        private set
    /**
     * Represents a button displayed when there are no items to show in the GUI.
     * This button is optional and may not be initialized for all instances of the GUI.
     * The button can serve as a placeholder or provide a message indicating that no items
     * are currently available.
     */
    var emptyMessageButton: Button? = null
        private set
    /**
     * Represents a pair of buttons used for navigation between pages in the paginated GUI.
     * The first element corresponds to the "Previous Page" button, and the second corresponds
     * to the "Next Page" button.
     * This property is nullable to indicate that navigation buttons
     * may not always be present.
     */
    var pageChangeButtons: Pair<Button, Button>? = null
        private set

    /**
     * Represents the list of buttons to be displayed on the current page of the paginated GUI.
     * This list is populated based on the current state and the mappings defined in `pageItemsMap`.
     * It contains the actual buttons that will be rendered in the GUI for user interaction.
     */
    var pageItems: List<Button> = emptyList()
        private set

    /**
     * Represents the total number of pages available in the paginated GUI.
     * This value is calculated based on the total number of items and the items per page.
     * It determines how many pages are needed to display all items in the GUI.
     */
    var totalPages = 1
        private set

    /**
     * A lambda function that generates a list of buttons based on the current state.
     * This function is used to dynamically create the button mappings for the GUI
     * based on the current state.
     * It allows for custom button generation logic to be applied
     * when the state changes.
     */
    var statePageItemProvider: ((S) -> List<Button>)? = null
        private set

    var paginationEnabledStates: Set<S> = enumClass.enumConstants.toSet()

    /**
     * Constructs a `PaginatedDynamicGUI` instance by delegating the given `enumKClass` and `player`
     * to another constructor.
     *
     * @param enumKClass The KClass of the generic type `T` used to manage the states of the GUI.
     * @param player The player for whom the GUI is constructed.
     */
    constructor(enumKClass: KClass<S>, player: Player): this(enumKClass.java, player)

    /**
     * A companion object for the `PaginatedDynamicGUI` class.
     * Provides a utility function to create an instance of the `PaginatedDynamicGUI` for a given Enum class and player.
     */
    companion object {
        /**
         * Creates an instance of [PaginatedDynamicGUI] for the given enumeration class and player.
         *
         * @param enumClass The [KClass] of the enumeration to be used for states in the GUI.
         * @param player The [Player] for whom the GUI is being created.
         * @return A new instance of [PaginatedDynamicGUI] configured with the provided enum class and player.
         */
        @JvmStatic
        fun <S : Enum<S>> of(enumClass: Class<S>, player: Player): PaginatedDynamicGUI<S> =
            PaginatedDynamicGUI(enumClass, player)

        /**
         * Kotlin-friendly version of the [PaginatedDynamicGUI] constructor using a reified enum type.
         *
         * This function allows creating a [PaginatedDynamicGUI] without explicitly passing the enum class.
         *
         * ### Example:
         * ```kotlin
         * val gui = PaginatedDynamicGUI.of<MyGUIState>(player)
         * ```
         *
         * @param S The enum type representing the GUI state.
         * @param player The player for whom the GUI is shown.
         * @return A [PaginatedDynamicGUI] instance bound to the enum type [S].
         */
        inline fun <reified S : Enum<S>> of(player: Player): PaginatedDynamicGUI<S> =
            PaginatedDynamicGUI(S::class.java, player)
    }

    fun setStateFixedButtonProviders(providers: Map<S, (S) -> List<Button>>): PaginatedDynamicGUI<S> {
        stateFixedButtonProviders.putAll(providers)
        return this
    }

    fun setPaginationEnabledStates(states: Set<S>): PaginatedDynamicGUI<S> {
        this.paginationEnabledStates = states
        return this
    }


    /**
     * Sets the button provider function for generating buttons based on the current state.
     * This function is called to create a list of buttons whenever the state changes,
     * allowing for dynamic button generation.
     *
     * @param provider A lambda function that takes a state of type S and returns a list of buttons.
     * @return The current instance of [PaginatedDynamicGUI] for method chaining.
     */
    fun setPageItemProvider(provider: (S) -> List<Button>): PaginatedDynamicGUI<S> {
        this.statePageItemProvider = provider
        return this
    }

    /**
     * Sets the slot positions for the inventory GUI.
     *
     * @param slots A list of integers representing the slot positions to be used.
     * @return The current instance of [PaginatedDynamicGUI] for method chaining.
     */
    fun setSlotPositions(slots: List<Int>): PaginatedDynamicGUI<S> {
        this.slotPositions = slots
        return this
    }

    /**
     * Sets the number of items that should be displayed per page in the GUI.
     *
     * @param count The number of items to be displayed per page.
     * @return The [PaginatedDynamicGUI] instance for method chaining.
     */
    fun setItemsPerPage(count: Int): PaginatedDynamicGUI<S> {
        this.itemsPerPage = count
        return this
    }

    /**
     * Sets the items to be paginated.
     * @param items The list of items to display across multiple pages.
     * @return This [PaginatedDynamicGUI] instance.
     */
    @ApiStatus.Internal // Maybe you should use `setPageItemProvider`
    fun setPageItems(items: List<Button>): PaginatedDynamicGUI<S> {
        this.pageItems = items
        setTotalPages(items.size)
        return this
    }

    /**
     * Sets the total number of pages.
     * This method should be called after setting up items.
     */
    fun setTotalPages(totalItems: Int): PaginatedDynamicGUI<S> {
        this.totalPages = (totalItems + itemsPerPage - 1) / itemsPerPage
        return this
    }

    /**
     * Sets the button to be displayed when the paginated GUI has no items to show.
     *
     * @param button The Button to represent an empty message in the GUI.
     *               This button will be displayed when there are no items in the current state or page.
     * @return The current instance of [PaginatedDynamicGUI] to enable method chaining.
     */
    fun setEmptyMessageButton(button: Button): PaginatedDynamicGUI<S> {
        this.emptyMessageButton = button
        return this
    }

    /**
     * Sets the buttons used to navigate between pages in the paginated GUI.
     * The provided buttons will be assigned click actions to move to the previous or next page.
     *
     * @param prev The button used to navigate to the previous page.
     * @param next The button used to navigate to the next page.
     * @return The current instance of [PaginatedDynamicGUI] for method chaining.
     */
    fun setPageChangeButtons(prev: Button, next: Button): PaginatedDynamicGUI<S> {
        pageChangeButtons = prev to next
        prev.setClick(this) { _, _ -> prevPage() }
        next.setClick(this) { _, _ -> nextPage() }
        return this
    }

    /**
     * Updates the current state of the paginated dynamic GUI and resets the current page to the first one.
     *
     * @param state The new state to set for the GUI.
     * @return The current instance of [PaginatedDynamicGUI] with the updated state.
     */
    fun setState(state: S): PaginatedDynamicGUI<S> {
        this.currentState = state
        this.currentPage = 0
        return this
    }

    /**
     * Constructs and prepares the InventoryGUI object for use by setting the appropriate state
     * and displaying it.
     * If the `currentState` is null and the `enumClass` corresponds to
     * `SinglePage`, it initializes `currentState` with `SinglePage.PAGE`.
     *
     * @return The constructed InventoryGUI instance.
     */
    override fun build(): InventoryGUI {
        create()
        display()
        return this
    }

    override fun getAllButtons(): List<Button> {
        return buildList {
            addAll(items)
            currentState?.let {
                stateFixedButtonProviders[it]?.let { provider ->
                    addAll(provider(it))
                }
            }
            if (currentState in paginationEnabledStates) {
                addAll(pageItems)
                pageChangeButtons?.let { (prev, next) -> add(prev); add(next) }
            }
        }
    }

    /**
     * Switches the current state of the GUI to the specified state.
     * If the given state is different from the current state, it updates the state,
     * resets the current page to the first page (index 0), and updates the display.
     *
     * @param state The new state to switch to. Typically corresponds to a mapped set of items in the GUI.
     */
    fun switchState(state: S) {
        if (state != currentState) {
            currentState = state
            currentPage = 0
        }

        statePageItemProvider?.let {
            val items = it(state)
            setPageItems(items)
        }

        display()
    }

    /**
     * Switches the current page of the paginated GUI to the specified page index and updates its display.
     *
     * @param page The index of the page to switch to. It should be a non-negative integer and within the range of available pages.
     */
    fun switchPage(page: Int) {
        currentPage = page
        display()
    }

    /**
     * Navigates to the next page in the paginated GUI.
     * This function increments the current page index by one
     * and updates the GUI to display the next set of items.
     */
    fun nextPage() = switchPage(currentPage + 1)
    /**
     * Navigates to the previous page in the paginated GUI.
     *
     * Updates the current page number to one less than the current value
     * and triggers a refresh of the GUI display to reflect the change.
     *
     */
    fun prevPage() = switchPage(currentPage - 1)

    /**
     * Updates and renders the inventory GUI based on the current state, page, and associated mappings.
     *
     * This method clears the inventory, applies a background if defined, and populates it with items
     */
    private fun display() {
        inventory.clear()
        applyBackground()

        items.forEach { item -> inventory.setItem(item.slot, item.toItemStack()) }

        val state = currentState ?: return

        stateFixedButtonProviders[state]?.invoke(state)?.forEach { button ->
            inventory.setItem(button.slot, button.toItemStack())
        }

        val isPaginationEnabled = currentState in paginationEnabledStates

        if (isPaginationEnabled) {
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
                pageChangeButtons?.first?.takeIf { currentPage > 0 },
                pageChangeButtons?.second?.takeIf { currentPage < totalPages - 1 }
            ).forEach { button ->
                inventory.setItem(button.slot, button.toItemStack())
            }
        }

    }
}
