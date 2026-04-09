# Shulker Vault Mod

## Project Overview
Shulker Vault is a NeoForge-first Minecraft mod for Java Edition that adds Create-inspired storage blocks combining ideas from:

- Create's Item Vault
- Create's Toolbox
- Vanilla Shulker Boxes
- Quick Right-Click / Easy Shulker Boxes style inventory interaction
- Sophisticated Backpacks style hinting and discoverability

The goal is not just to make an animated container. The goal is to create physically interactive, automation-friendly, quality-of-life storage blocks that feel natural alongside both vanilla shulker boxes and the Create mod ecosystem.

The mod currently centers around two related blocks:

- `Shulker Vault`
- `Advanced Shulker Vault`

The Advanced Shulker Vault is an upgraded form of the Shulker Vault that adds package-aware logistics behavior and more advanced automation support.

## Current Direction
This project is currently **NeoForge only**.

Fabric support is not a current target and should not influence architecture decisions right now. If Fabric support happens later, it should come after the NeoForge implementation is stable and the backend architecture is proven.

## Block Tiers

### Shulker Vault
The base Shulker Vault is the core storage block.

It should provide:
- 27 inventory slots
- Configurable per-slot stack multiplier
- Default stack multiplier of `4`
- Shulker-style opening lid behavior
- Create-compatible automation support
- Quick-access and quality-of-life interactions

### Advanced Shulker Vault
The Advanced Shulker Vault is an upgraded version of the Shulker Vault.

It should provide everything the base Shulker Vault does, plus:
- package-aware inventory memory behavior
- direct package receiving and unpacking
- no packager requirement for receiving packages through Frogports or item insertion systems
- a dedicated single package slot for package holding / overflow cases
- distinct textures for the block and menu

The Advanced Shulker Vault is intended for players who want more Create logistics functionality without requiring a separate packager at the destination.

## Upgrade Path

### Crafting Upgrade
The Advanced Shulker Vault should be crafted by combining:

- an Electron Tube from Create
- a Packager from Create
- the existing Shulker Vault being upgraded

The crafting recipe is the following set in a vertical fashion, while using the mechanical crafters:
- Electron Tube above
- Packager in the middle
- Shulker Vault below

### State Preservation
Upgrading a Shulker Vault into an Advanced Shulker Vault must preserve its full contents and relevant state.

This means:
- an empty Shulker Vault upgrades into an empty Advanced Shulker Vault
- a filled Shulker Vault upgrades into an Advanced Shulker Vault containing exactly the same inventory
- any relevant stored metadata should be preserved where appropriate

Inventory preservation is mandatory. The upgrade should behave like a true block upgrade, not a destroy-and-replace loss event.

## Design Goals

### Core Identity
The Shulker Vault family should feel like:

- a shulker box in shape and physical behavior
- a Create storage block in terms of automation support
- a toolbox/vault hybrid in terms of stack sizing and utility
- a modern quality-of-life container in terms of player interaction

### Storage Behavior
- 27 inventory slots, matching a vanilla shulker box
- configurable per-slot stack multiplier
- default stack multiplier is `4`
- storage rules should be consistent across player interaction, hopper IO, and Create automation
- serialization must cleanly preserve contents when the block is picked up, replaced, or upgraded

### World Behavior
- the lid physically opens upward in the world
- the collision / hitbox must increase with lid movement
- a player standing on top of the block should be lifted by the opening lid, like a vanilla shulker box
- the player should **not** be kicked off at full open; collision updates and entity displacement must be smooth and continuous
- visual animation must follow the same progression as server-side collision logic

## Create Integration

### Base Integration
The Shulker Vault should support Create-style interaction patterns where practical:

- chutes
- funnels
- hoppers
- packager-adjacent item interaction
- Create wrench pickup behavior

Picking up the placed block with a Create wrench should preserve its stored inventory and relevant state.

### Advanced Integration
The Advanced Shulker Vault extends Create compatibility with package-handling behavior.

It should support:
- receiving Create packages directly from Frogports
- receiving Create packages through funnels, hoppers, and similar insertion systems
- unpacking received packages internally without requiring a separate Packager block

The intent is that the Advanced Shulker Vault acts as its own package destination and unpacking endpoint.

## Advanced Package Behavior

### Remembered Inventory Behavior
The Advanced Shulker Vault should be able to remember inventory intent, inspired by Create's Toolbox and similar storage memory systems.

The exact UX can be refined later, but the core goal is:
- the block can retain package-related or routing-relevant inventory memory
- it should behave predictably when repeatedly receiving the same kinds of packaged deliveries

This memory behavior should be treated as an advanced feature of the upgraded block, not part of the basic Shulker Vault.

### Direct Package Unpacking
When a valid Create package is inserted into an Advanced Shulker Vault, the package should be unpacked and its contents inserted into the vault's main inventory.

This should work for packages received through:
- Frogports
- funnels
- hoppers
- similar supported item insertion methods

### Dedicated Package Slot
The Advanced Shulker Vault must have one additional dedicated slot used for package storage.

This slot is not part of the normal 27-slot storage grid. It exists specifically to hold a package item when unpacking cannot complete safely.

Rules:
- only one package item may be stored there
- this slot is for package handling, not general storage
- the Advanced Shulker Vault menu should visually expose this slot
- the Advanced Shulker Vault should use a distinct menu texture to reflect this added functionality
- when given a redstone signal the block will then make a package to be extracted

### Incomplete Unpacking Rule
If an incoming package cannot be fully unpacked into the main inventory, the system must not partially unpack and scatter items unpredictably.

Instead:
- if unpacking can complete fully, unpack and insert all contents
- if unpacking cannot complete fully, stop the unpack operation
- store the package item itself in the dedicated package slot
- do not insert any of the package's contents into the main inventory

This rule is important for predictability and item safety.

The package slot is effectively a safe holding area for blocked deliveries.

### Full State Handling
If the Advanced Shulker Vault is full and a package arrives:
- the package should remain intact
- the package should be moved into the dedicated package slot if that slot is empty
- if the dedicated package slot is already occupied, the incoming package should not be consumed

No items should be voided, partially inserted, or duplicated.

## Quality of Life
The block family should support quick interaction patterns inspired by:

- Quick Right-Click
- Easy Shulker Boxes
- Sophisticated Backpacks

Desired behavior:
- fast access to inventory where appropriate
- good inventory click behavior and convenience interactions by default
- built-in interaction behavior should not hard-conflict with Easy Shulker Boxes or similar mods if they are installed
- hint and discoverability features should be subtle, optional, and suppressible

## Architecture Priorities

### Backend First
The backend architecture is more important than the renderer.

This mod should be designed around a strong storage and interaction model first, with animation layered on top. Avoid coupling inventory logic, world collision logic, GUI code, package logic, and Create integration into one class.

### Recommended Separation
The codebase should be structured around separate responsibilities:
```
java/
└── dev.wyfy.shulkervault/
├── block/
│   ├── custom/
│   │   ├── AdvancedShulkerVaultBlock     // Contains specific block behaviors (e.g., right-click interactions, placement logic).
│   │   └── ShulkerVaultBlock             // The base block logic for the standard vault.
│   ├── entity/
│   │   ├── ModBlockEntities              // The DeferredRegister for your block entities. Purely registration.
│   │   └── ShulkerVaultBlockEntity       // The actual data container holding the inventory/NBT data for the vault in the world.
│   └── ModBlocks                         // The DeferredRegister for all blocks.
│
├── datagen/                              // EXCELLENT setup here. Automating these prevents massive JSON headaches.
│   ├── DataGenerators                    // The main bus subscriber that triggers all the providers below.
│   ├── ModBlockLootTableProvider         // Generates what drops when the vault is broken (crucial for keeping inventory vs dropping it).
│   ├── ModBlockStateProvider             // Generates the blockstate JSONs and simple block models.
│   ├── ModBlockTagProvider               // Assigns blocks to tags (e.g., mineable/pickaxe).
│   ├── ModItemModelProvider              // Generates the JSONs for how items render in hand/inventory.
│   ├── ModItemTagProvider                // Assigns items to tags.
│   └── ModRecipeProvider                 // Generates the crafting recipes.
│   // ⚠️ MISSING: ModSoundProvider       // (Adding shortly once I start to work on sound events, opening/closing)
│
├── event/
│   └── ModCapabilities                   // Isolating capabilities (like item handlers/forge energy) here keeps your main class clean.
│
├── item/
│   ├── custom/                           // Empty for now, but ready for complex items (like a vault upgrade template).
│   ├── ModCreativeModeTabs               // Registers your custom creative tab to hold the mod's items.
│   └── ModItems                          // The DeferredRegister for all items.
│
├── screen/
│   ├── custom/
│   │   ├── ShulkerVaultMenu              // The SERVER/COMMON logic. Handles slot indices, shift-clicking, and syncing inventory data.
│   │   └── ShulkerVaultScreen            // The CLIENT logic. Handles rendering the background texture, tooltips, and x/y coordinates.
│   └── ModMenuTypes                      // The DeferredRegister tying the Menu and Screen together.
│
├── sound/
│   └── ModSoundEvents                    // The DeferredRegister for custom SoundEvents (e.g., opening/closing the vault).
│
├── storage/
│   └── ShulkerVaultStorage               // Custom implementation for handling the underlying inventory logic (likely wrapping ItemStackHandler).
│
├── util/
│   └── ModTags                           // Helper class for referencing custom tags cleanly in code without hardcoding ResourceLocations.
│
├── Config                                // NeoForge config setup for server/client/common configuration values.
└── ShulkerVault                          // The main entry point. Should ONLY contain the mod constructor and Event Bus bus setups.
```

### Important Rule
Do **not** let the block entity become the entire system.

The block entity should coordinate state, not own every rule directly. Storage logic should be reusable and testable independently from rendering, menus, collision behavior, and Create integration.

The Advanced Shulker Vault should reuse as much of the base vault storage model as possible rather than fork it carelessly.

## Technical Focus

### Loader
- NeoForge only

### Minecraft / Java
- Minecraft 1.21.1
- Java 21

### GUI
- vanilla `AbstractContainerMenu` and `ContainerScreen` APIs

### Animation
- Using the item, not only brings up the GUI texture for storage, but while the vault is being used the lid moves up in a spinning fashion just like the shulker box.
- When the GUI is closed, just like the shulker box, the lid of the vault closes.
- BUT, while the vault is open, the item in the first slot renders between the lid and the base
- When the user mouses over another item in the gui, the item that is rendered, follows suit
- With the advanced vault, the first item to render is the package slot, displaying the package in that slot, if there is one.

## Collision and Lid Behavior
This is a critical gameplay feature, not polish.

The lid opening system must be implemented so that:
- open progress is tracked over time
- collision shape grows with that progress
- entity displacement is handled continuously
- players standing on top are lifted vertically with the lid
- horizontal ejection only happens when truly necessary

The server-side collision model is the source of truth. Client animation should follow it, not replace it.

## Compatibility Philosophy

### Create
Create integration should feel native, but Create-specific behavior should stay modular where possible.

### Easy Shulker Boxes / Quick Right-Click
Built-in QoL features should be designed to avoid double-handling interactions when similar mods are present. Prefer compatibility guards, config toggles, and conservative event consumption.

### Hinting
Hints should work like Sophisticated Backpacks:
- useful
- lightweight
- easy to disable
- not spammy

## Configuration
The following should be configurable:

- per-slot stack multiplier
- quick interaction enable/disable
- hint enable/disable
- compatibility behavior toggles if needed

Potential future advanced configuration:
- package auto-unpack behavior
- remembered inventory behavior rules
- package slot behavior tuning if needed

## Non-Goals Right Now
The following are not current priorities:

- Fabric support
- multi-loader abstraction
- broad cross-version support
- fancy architecture for loaders that are not being actively targeted

## Current Development Phase
The project likely needs a significant backend refactor.

Focus areas:
1. clean up storage architecture
2. separate block entity state from inventory logic
3. rebuild lid collision behavior around smooth progress and entity lifting
4. add Create-compatible item IO in a backend-safe way
5. define the Advanced Shulker Vault as a proper extension, not a hacked-on variant
6. implement safe package receiving and package-slot fallback behavior
7. layer animation and QoL features on top of a stable core

## Development Notes
- prefer correctness to visual polish
- prefer stable backend abstractions over premature platform abstraction
- avoid implementing interaction logic in multiple places
- keep the inventory model authoritative and reusable
- treat animation, GUI, automation, package handling, and quick-access behavior as separate consumers of the same storage core
- preserve inventory and relevant state during upgrade paths
- package handling must always prioritize safety and predictability over convenience