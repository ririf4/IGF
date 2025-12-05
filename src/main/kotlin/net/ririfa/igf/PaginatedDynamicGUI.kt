package net.ririfa.igf

import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
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
    var currentState: S? = null
        private set
    var currentPage: Int = 0
        private set
    var slotPositions: List<Int> = emptyList()
        private set
    var itemsPerPage: Int = 9
        private set
    var emptyMessageButton: Button? = null
        private set
    var pageChangeButtons: Pair<Button, Button>? = null
        private set
    var pageItems: List<Button> = emptyList()
        private set
    var totalPages = 1
        private set
    var statePageItemProvider: ((S) -> List<Button>)? = null
        private set
    var paginationEnabledStates: Set<S> = enumClass.enumConstants.toSet()
        private set
    var onCloseFunc: ((PaginatedDynamicGUI<S>, InventoryCloseEvent.Reason) -> Unit)? = null
        private set
    var onOpenFunc: ((PaginatedDynamicGUI<S>) -> Unit)? = null
        private set

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

    /**
     * Sets the providers for fixed buttons associated with each state.
     *
     * These buttons will always be displayed regardless of pagination.
     * You can use this to show static UI elements (like category headers or filters).
     *
     * @param providers A map from each state to a lambda that returns a list of fixed buttons.
     * @return The current instance of [PaginatedDynamicGUI] for method chaining.
     */
    fun setStateFixedButtonProviders(providers: Map<S, (S) -> List<Button>>): PaginatedDynamicGUI<S> {
        stateFixedButtonProviders.putAll(providers)
        return this
    }

    /**
     * Specifies which states should use pagination.
     *
     * If a state is not included in this set, pagination will be disabled for that state,
     * and all items will be shown without page navigation.
     *
     * @param states The set of enum states that should use pagination.
     * @return The current instance of [PaginatedDynamicGUI] for method chaining.
     */
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

        currentState?.let { state ->
            statePageItemProvider?.let { provider ->
                val items = provider(state)
                setPageItems(items)
            }
        }

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
     * Sets a handler to be called when this GUI is closed.
     *
     * This callback will be triggered on GUI close events, allowing you to perform
     * any necessary cleanup or state updates.
     *
     * @param block The lambda to execute when the GUI is closed.
     *              The instance of [PaginatedDynamicGUI] will be passed as the receiver.
     * @return The current instance of [PaginatedDynamicGUI] for method chaining.
     */
    fun onClose(block: (PaginatedDynamicGUI<S>, InventoryCloseEvent.Reason) -> Unit): PaginatedDynamicGUI<S> {
        this.onCloseFunc = block
        return this
    }

    fun onOpen(block: (PaginatedDynamicGUI<S>) -> Unit): PaginatedDynamicGUI<S> {
        this.onOpenFunc = block
        return this
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

    fun nextPage() = switchPage(currentPage + 1)
    fun prevPage() = switchPage(currentPage - 1)
}
