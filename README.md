# IGF - Inventory GUI Framework

Latest version: ![Dynamic XML Badge](https://img.shields.io/badge/dynamic/xml?url=https%3A%2F%2Frepo.ririfa.net%2Frepository%2Fmaven-public%2Fnet%2Fririfa%2Figf%2Fmaven-metadata.xml&query=%2Fmetadata%2Fversioning%2Flatest&style=plastic&logo=sonatype&label=Nexus)


IGF (Inventory GUI Framework) is a lightweight library for creating intuitive and powerful Inventory GUIs in Minecraft plugins. It simplifies the process of building and managing custom GUI interfaces using the native Minecraft inventory system, making it ideal for developers who want to enhance their plugins with an interactive user experience.

## Features
- Easy-to-use API for creating Inventory GUIs.
- Support for different event handlers (click, open, close).
- Built-in utility functions for managing complex GUIs.

## Getting Started

### Installation
To include IGF in your project, add the following dependency to your `pom.xml` if you're using Maven:

```xml
<repositories>
    <repository>
        <id>ririfa-repo</id>
        <url>https://repo.ririfa.net/maven2</url>
    </repository>
</repositories>

<dependency>
    <groupId>net.ririfa</groupId>
    <artifactId>igf</artifactId>
    <version>{version}</version> <!-- Replace {version} with the latest version -->
</dependency>
```

For Gradle, include this in your `build.gradle`:
```groovy
repositories {
    maven { url "https://repo.ririfa.net/maven2" }
}

dependencies {
    implementation 'net.ririfa:igf:{version}' // Replace {version} with the latest version
}
```

For kotlin DSL:
```kotlin
repositories {
    maven("https://repo.ririfa.net/maven2")
}

dependencies {
    implementation("net.ririfa:igf:{version}") // Replace {version} with the latest version
}
```

Hint: You can always find the latest version on [RiriFa Repo](https://repo.ririfa.net/service/rest/repository/browse/maven-public/net/ririfa/igf/).

### License
This project is licensed under the MIT License – see the [LICENSE](LICENSE) file for details.

## Usage

---

## 🔧 Initialization

To use IGF, start by initializing it in your plugin's `onEnable()` method.  
This sets up the internal event system and enables utility features like key generation.

---

### 📦 Basic setup

```kotlin
override fun onEnable() {
    IGF.init(this, "yourpluginid") // Used for event registration and key namespacing
}
```

Once initialized, features like `InventoryGUI`, `Button`, and `IGF.createKey(...)` become available.

---

### 🗝️ Creating `NamespacedKey`s

Use `IGF.createKey(...)` to generate namespaced keys that are scoped to your plugin:

```kotlin
val key = IGF.createKey("my", "custom", "data")
// → yourpluginid:my.custom.data
```

This is useful when adding persistent data to buttons via `setData()` or `addData()`.

---

### 🌍 Optional: Global Listener

You can also set a global listener to handle all GUI events across your plugin:

```kotlin
IGF.setGlobalListener(object : GUIListener {
    override fun onInventoryClick(event: InventoryClickEvent, gui: InventoryGUI) {
        println("Global click by ${event.whoClicked.name}")
    }
})
```

This is optional—most use cases are covered by individual button handlers (`Button.setClick { ... }`).

---

### `SimpleGUI`

`SimpleGUI` is a ready-to-use implementation of `InventoryGUI`, designed for quick and simple inventory screens.  
It is perfect for static UIs such as confirmation dialogs, menus with a fixed number of options,
or any interface that doesn't require pagination or runtime generation.

You can easily set a background, add buttons with click handlers, and open the GUI to a player.  
Each button supports persistent data (via NamespacedKey and PersistentDataType) and individual click actions.

**Key features:**
- Set a static background item for all slots.
- Display buttons with custom name, data, and per-button click handlers.
- Easily extendable for small to medium-sized GUIs.

---

**Example:**

```kotlin
val gui = SimpleGUI(player)
    .setTitle(Component.text("Main Menu"))
    .setSize(27)
    .setBackground(Material.BLACK_STAINED_GLASS_PANE)
    .setItems(listOf(
        Button(
            slot = 11,
            material = Material.DIAMOND,
            name = Component.text("Shiny Button")
        ).setClick { player.sendMessage("You clicked a diamond!") },

        Button(
            slot = 15,
            material = Material.REDSTONE,
            name = Component.text("Exit")
        ).setClick { player.closeInventory() }
    ))
    .build()

gui.open() // Opens the inventory to the player
```

---

### `PaginatedGUI`

`PaginatedGUI` is an extension of `InventoryGUI` that allows you to display a large number of items across multiple pages.  
You can define custom slot positions, set how many items to show per page, and even configure navigation buttons like "Next" and "Previous".

This is especially useful for player lists, shop menus, or any collection that doesn't fit in a single inventory screen.

**Key features:**
- Customizable slot layout using `setSlotPositions`.
- Define how many items to show per page (`setItemsPerPage`).
- Add navigation buttons that handle page switching.
- Support for a fallback `emptyMessageButton` if there are no items to display.

---

**Example:**

```kotlin
val itemList = (1..100).mapIndexed { i, n ->
    Button(
        slot = 0, // will be overridden by slotPositions
        material = Material.PAPER,
        name = Component.text("Item #$n")
    ).setClick { player.sendMessage("You selected Item #$n") }
}

val gui = PaginatedGUI(player)
    .setTitle(Component.text("Paginated List"))
    .setSize(54)
    .setBackground(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
    .setSlotPositions((10..16) + (19..25) + (28..34)) // 3 rows of 7 centered slots
    .setItemsPerPage(21)
    .setPageItems(itemList)
    .setEmptyMessageButton(
        Button(slot = 22, material = Material.BARRIER, name = Component.text("No items"))
    )
    .setPageButtons(
        prevButton = Button(slot = 45, material = Material.ARROW, name = Component.text("Previous")),
        nextButton = Button(slot = 53, material = Material.ARROW, name = Component.text("Next"))
    )
    .build()

gui.open() // Opens the inventory to the player
```

---

### `DynamicGUI`

`DynamicGUI` is a powerful state-based GUI system built on top of `InventoryGUI`.  
It allows you to create flexible inventory interfaces that can change layout and behavior depending on the current state (usually defined as an enum class).

This is perfect for multistep menus, configuration panels, or any situation where the inventory UI needs to change contextually.

**Key features:**
- Maps enum states to button layouts.
- Easily switches between states using `switchState(...)`.
- Fully compatible with the rest of the IGF ecosystem (`Button`, `PersistentData`, etc.).
- Backgrounds and layout are preserved across state transitions.

---

**Example:**

```kotlin
enum class MenuState {
    MAIN, SETTINGS
}

val gui = DynamicGUI(MenuState::class, player)
    .setTitle(Component.text("Dynamic Menu"))
    .setSize(27)
    .setBackground(Material.GRAY_STAINED_GLASS_PANE)
    .setButtonMappings(mapOf(
        MenuState.MAIN to listOf(
            Button(slot = 11, material = Material.EMERALD, name = Component.text("Settings"))
                .setClick { gui.switchState(MenuState.SETTINGS) }
        ),
        MenuState.SETTINGS to listOf(
            Button(slot = 13, material = Material.REDSTONE, name = Component.text("Back"))
                .setClick { gui.switchState(MenuState.MAIN) }
        )
    ))
    .setState(MenuState.MAIN)
    .build()

gui.open() // Opens the inventory to the player
```

---

### `Button`

A `Button` in IGF represents a clickable item inside the GUI.  
Each button includes:

- The **slot** it appears in
- The **material** and **name** for visual display
- Optional **persistent data**
- An **onClick** handler (lambda)
- A flag `skipGUIListenerCall` to control event propagation (default: `true`)

Buttons can be created and customized fluently with extension functions.

---

**Basic example:**

```kotlin
val button = Button(
    slot = 13,
    material = Material.DIAMOND,
    name = Component.text("Click me!")
).setClick {
    player.sendMessage("You clicked the diamond!")
}
```

---

### Button Extensions

IGF provides extension functions to enhance how you build and modify buttons.

#### `setClick`

Sets or replaces the click handler for the button.

```kotlin
button.setClick { player ->
    player.sendMessage("Clicked!")
}
```

---

#### `appendClick`

Add another click handler *without replacing* the existing one.

```kotlin
button.appendClick { player ->
    player.playSound(player.location, Sound.UI_BUTTON_CLICK, 1f, 1f)
}
```

---

#### `setData` / `addData`

Attaches persistent data to the button’s ItemStack using `NamespacedKey`.

```kotlin
val key = NamespacedKey(plugin, "my_data")

button.setData(key, DataWrapper(PersistentDataType.INTEGER, 123))
```

or with multiple entries:

```kotlin
button.addData(
    key to DataWrapper(PersistentDataType.STRING, "value"),
    anotherKey to DataWrapper(PersistentDataType.BYTE, 1)
)
```

---

### Accessing persistent data from an `ItemStack`

You can retrieve data from any `ItemStack` created by a `Button` like so:

```kotlin
val value: Int? = itemStack.getValue(key, PersistentDataType.INTEGER)
```

Or, if you want the `DataWrapper` itself:

```kotlin
val wrapper = itemStack.getWrapper(key, PersistentDataType.INTEGER)
```

---

### ❗ Deprecated: `GUIListener`

IGF previously supported per-GUI event handling through the `GUIListener` interface.  
However, this is now **deprecated** in favor of more modular and scalable **button-level click handlers** using `Button#setClick { ... }`.

```kotlin
@Deprecated("Use Button-based click handlers instead")
interface GUIListener {
    fun onInventoryClick(event: InventoryClickEvent, gui: InventoryGUI) {}
    fun onInventoryClose(event: InventoryCloseEvent, gui: InventoryGUI) {}
    fun onInventoryOpen(event: InventoryOpenEvent, gui: InventoryGUI) {}
}
```

You can still set a GUI-level listener if needed:

```kotlin
gui.setListener(object : GUIListener {
    override fun onInventoryClose(event: InventoryCloseEvent, gui: InventoryGUI) {
        println("Inventory closed")
    }
})
```

But this is discouraged unless you have a very specific need (e.g., tracking open/close events globally).  
For most use cases, prefer this style:

```kotlin
Button(slot, material, name).setClick { player ->
    player.sendMessage("Button clicked!")
}
```