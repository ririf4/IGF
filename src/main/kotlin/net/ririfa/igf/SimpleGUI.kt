package net.ririfa.igf

import org.bukkit.entity.Player

/**
 * A simple GUI class that provides basic inventory GUI functionality.
 * It allows setting up buttons, listeners, and background materials for GUI customization.
 * This class can be extended to create more complex GUIs.
 * @param player The player who will view the GUI.
 * @see InventoryGUI
 * @since 1.0.0
 * @author RiriFa
 */
@Suppress("unused")
class SimpleGUI(
    player: Player
) : InventoryGUI(player) {
    var onCloseFunc: (SimpleGUI) -> Unit = {}

    override fun build(): InventoryGUI {
        create()
        applyBackground()
        displayItems()
        return this
    }

    // SimpleGUI doesn't have original item holder
    override fun getAllButtons(): List<Button> = items

    fun onClose(block: (SimpleGUI) -> Unit): SimpleGUI {
        this.onCloseFunc = block
        return this
    }
}