package net.ririfa.igf

import org.bukkit.entity.Player
import kotlin.reflect.KClass

/**
 * A generic dynamic GUI framework for creating inventories with an adaptive state-based design.
 *
 * @param S The type of the state enumeration that defines the various states the GUI can have.
 * @param player The player for whom this GUI is being created.
 * @see InventoryGUI
 * @since 1.1.0
 *
 * The `DynamicGUI` class extends `InventoryGUI` to provide enhanced functionality for creating GUIs
 * that dynamically change based on specific predefined states. Each state is defined by an enum
 * parameter, allowing the GUI to map buttons and functionality specific to each state.
 *
 * Features:
 * - Manages state transitions and updates the GUI accordingly.
 * - Allows the creation of mappings between states and button configurations.
 * - Supports dynamic updates of inventory content based on the current state.
 */
class DynamicGUI<S : Enum<S>>(
	private val enumClass: Class<S>,
	player: Player
): InventoryGUI(player) {
	// These functions are for Java users
	companion object {
		@JvmStatic
		fun <S : Enum<S>> of(enumKClass: KClass<S>, player: Player): DynamicGUI<S> =
			DynamicGUI(enumKClass, player)

		@JvmStatic
		fun of(player: Player) =
			DynamicGUI(SinglePage::class, player)

		inline fun <reified S : Enum<S>> of(player: Player): DynamicGUI<S> =
			DynamicGUI(S::class.java, player)
	}

	private var currentState: S? = null
	private var buttonMappings: Map<S, List<Button>> = emptyMap()

	constructor(enumKClass: KClass<S>, player: Player): this(enumKClass.java, player)

	@Suppress("UNCHECKED_CAST")
	constructor(player: Player): this(
		SinglePage::class as KClass<S>,
		player
	)

	override fun build(): InventoryGUI {
		if (currentState == null && enumClass.isAssignableFrom(SinglePage::class.java)) {
			@Suppress("UNCHECKED_CAST")
			currentState = SinglePage.PAGE as S
		}
		create()
		updateButtonsForState()
		return this
	}

	/**
	 * Sets the current state of the DynamicGUI to the specified initial state.
	 *
	 * @param initialState The initial state to set for the GUI.
	 * @return The current instance of DynamicGUI for method chaining.
	 */
	fun setState(initialState: S): DynamicGUI<S> {
		this.currentState = initialState
		return this
	}

	/**
	 * Sets the button mappings for the DynamicGUI. Each state is associated with a list of buttons
	 * that define the appearance and functionality of the GUI in that state.
	 *
	 * @param mappings A map associating each state of type [S] with a list of [Button]s. The keys represent
	 * the possible states of the GUI, and the values are the corresponding lists of buttons to display for each state.
	 * @return The current instance of [DynamicGUI] for method chaining.
	 */
	fun setButtonMappings(mappings: Map<S, List<Button>>): DynamicGUI<S> {
		this.buttonMappings = mappings
		return this
	}

	/**
	 * Updates the state of the DynamicGUI to a new state and refreshes the buttons to match the new state.
	 * If the new state is the same as the current state, no action is taken.
	 *
	 * @param newState The new state of type [S] to switch to. The buttons and GUI layout will update to reflect this state.
	 */
	fun switchState(newState: S) {
		if (newState == currentState) return
		currentState = newState
		updateButtonsForState()
	}

	/**
	 * Updates the inventory GUI to reflect the buttons associated with the current state.
	 * This method clears the existing inventory, applies a background if specified, and sets
	 * the buttons mapped to the current state into the inventory.
	 *
	 * Steps performed in this method:
	 * - Retrieves the list of buttons for the current state from `buttonMappings`.
	 * - Clears the inventory to prepare for the new buttons.
	 * - Applies the background to the inventory, if defined.
	 * - Adds the buttons to the inventory and sets each button in its respective slot.
	 * - Updates the player's inventory to reflect these changes immediately.
	 *
	 * It uses:
	 * - `buttonMappings`: A map linking states to button configurations.
	 * - `applyBackground()`: Applies a visual background to the inventory.
	 * - `setItems(List<Button>)`: Updates the GUI items list with the current buttons.
	 * - `inventory.setItem(slot, item)`: Places individual buttons into their designated slots.
	 * - `player.updateInventory()`: Ensures the player's view is updated with any changes made to the GUI.
	 *
	 * If the current state doesn't have mapped buttons, the inventory will remain empty
	 * aside from the background if one is applied.
	 *
	 * This function assumes that `currentState` has been set and that `buttonMappings`
	 * contains a valid mapping for the state.
	 */
	fun updateButtonsForState() {
		val buttons = buttonMappings[currentState] ?: emptyList()

		inventory.clear()
		applyBackground()

		setItems(buttons)
		buttons.forEach { button ->
			inventory.setItem(button.slot, button.toItemStack())
		}

		player.updateInventory()
	}
}