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
    internal lateinit var plugin: JavaPlugin

    /**
     * Initializes the IGF with the given plugin and registers the events.
     *
     * @param plugin The JavaPlugin instance to initialize with.
     */
    fun init(plugin: JavaPlugin, nameSpace: String) {
        ID = nameSpace
        this.plugin = plugin
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
        if (!::ID.isInitialized) {
            throw IllegalStateException("IGF has not been initialized yet.")
        }
        return NamespacedKey(ID, name.joinToString("."))
    }

    /**
     * Handles inventory click events.
     * Cancels clicks on the background material by default and delegates to the appropriate listener.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInventoryClick(event: InventoryClickEvent) {
        val holder = event.clickedInventory?.holder as? InventoryGUI ?: return

        event.isCancelled = true

        val button = holder.getAllButtons().find { button -> button.slot == event.slot } ?: return

        button.onClick?.run {
            (event.whoClicked as? Player)?.let { player ->
                onClick(player, holder)
            }
        }
    }

    /**
     * Handles inventory close events.
     * Delegates to the appropriate listener if set.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInventoryClose(event: InventoryCloseEvent) {
        val holder = event.inventory.holder

        if (holder !is InventoryGUI) return

        when (holder) {
            is PaginatedDynamicGUI<*> -> {
                @Suppress("UNCHECKED_CAST")
                (holder as PaginatedDynamicGUI<Enum<*>>).onCloseFunc?.invoke(holder, event.reason)
            }

            is SimpleGUI -> {
                holder.onCloseFunc?.invoke(holder, event.reason)
            }

            is PaginatedGUI -> {
                holder.onCloseFunc?.invoke(holder, event.reason)
            }

            is DynamicGUI<*> -> {
                @Suppress("UNCHECKED_CAST")
                (holder as DynamicGUI<Enum<*>>).onCloseFunc?.invoke(holder, event.reason)
            }

            else -> return
        }
    }

    /**
     * Handles inventory open events.
     * Delegates to the appropriate listener if set.
     */
    @EventHandler
    fun onInventoryOpen(event: InventoryOpenEvent) {
        val holder = event.inventory.holder

        if (holder !is InventoryGUI) return

        when (holder) {
            is PaginatedDynamicGUI<*> -> {
                @Suppress("UNCHECKED_CAST")
                (holder as PaginatedDynamicGUI<Enum<*>>).onOpenFunc?.invoke(holder)
            }

            is SimpleGUI -> {
                holder.onOpenFunc?.invoke(holder)
            }

            is PaginatedGUI -> {
                holder.onOpenFunc?.invoke(holder)
            }

            is DynamicGUI<*> -> {
                @Suppress("UNCHECKED_CAST")
                (holder as DynamicGUI<Enum<*>>).onOpenFunc?.invoke(holder)
            }

            else -> return
        }
    }

    internal fun runTask(block: () -> Unit) {
        plugin.server.scheduler.runTask(plugin, block)
    }
}
