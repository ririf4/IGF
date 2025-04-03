package net.ririfa.igf

import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.plugin.java.JavaPlugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Main handler for the InventoryGUI framework (IGF).
 * It manages global listeners, event handling, and NamespacedKey initialization.
 */
@Suppress("unused")
object IGF : Listener {
    val logger: Logger = LoggerFactory.getLogger(IGF::class.java.simpleName)
    lateinit var ID: String
    private var globalListener: GUIListener = NoOpListener

    /**
     * Initializes the IGF with the given plugin and registers the events.
     *
     * @param plugin The JavaPlugin instance to initialize with.
     */
    fun init(plugin: JavaPlugin, nameSpace: String) {
        ID = nameSpace
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    /**
     * Creates a new NamespacedKey with the given name parts.
     * The key is created using the initialized plugin ID.
     *
     * @param name The name parts to create the key with.
     * @return The created NamespacedKey.
     * @throws IllegalStateException if IGF has not been initialized yet.
     * @see NamespacedKey
     */
    fun createKey(vararg name: String): NamespacedKey {
        if (!this::ID.isInitialized) {
            throw IllegalStateException("IGF has not been initialized yet.")
        }
        return NamespacedKey(ID, name.joinToString("."))
    }

    /**
     * Sets the global listener for all GUIs managed by IGF.
     *
     * @param listener The [GUIListener] to be used globally.
     */
    fun setGlobalListener(listener: GUIListener) {
        globalListener = listener
    }

    /**
     * Handles inventory click events.
     * Cancels clicks on the background material by default and delegates to the appropriate listener.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInventoryClick(event: InventoryClickEvent) {
        val holder = event.clickedInventory?.holder as? InventoryGUI ?: return
        val button = holder.items.find { button -> button.slot == event.slot } ?: return

        event.isCancelled = true

        button.onClick?.run {
            (event.whoClicked as? Player)?.let { this }
            if (button.skipGUIListenerCall) return
        }

        holder.getListener()?.onInventoryClick(event, holder) ?: globalListener.onInventoryClick(event, holder)
    }

    /**
     * Handles inventory close events.
     * Delegates to the appropriate listener if set.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInventoryClose(event: InventoryCloseEvent) {
        val holder = event.inventory.holder

        // Check if the closed inventory holder is of type InventoryGUI
        if (holder !is InventoryGUI) return

        // Delegate the event to the local or global listener
        holder.getListener()?.onInventoryClose(event, holder) ?: globalListener.onInventoryClose(event, holder)
        if (holder.shouldCallGlobalListener()) {
            globalListener.onInventoryClose(event, holder)
        }
    }

    /**
     * Handles inventory open events.
     * Delegates to the appropriate listener if set.
     */
    @EventHandler
    fun onInventoryOpen(event: InventoryOpenEvent) {
        val holder = event.inventory.holder

        // Check if the opened inventory holder is of type InventoryGUI
        if (holder !is InventoryGUI) return

        // Delegate the event to the local or global listener
        holder.getListener()?.onInventoryOpen(event, holder) ?: globalListener.onInventoryOpen(event, holder)
        if (holder.shouldCallGlobalListener()) {
            globalListener.onInventoryOpen(event, holder)
        }
    }

    /**
     * A no-operation listener to handle cases when the global listener is not set.
     * This prevents null checks and provides a safe default.
     */
    private object NoOpListener : GUIListener {
        override fun onInventoryClick(event: InventoryClickEvent, gui: InventoryGUI) {}
        override fun onInventoryClose(event: InventoryCloseEvent, gui: InventoryGUI) {}
        override fun onInventoryOpen(event: InventoryOpenEvent, gui: InventoryGUI) {}
    }
}
