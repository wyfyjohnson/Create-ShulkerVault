package dev.wyfy.shulkervault.screen.custom;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.wyfy.shulkervault.ShulkerVault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Screen for AdvancedShulkerVault with wider texture to accommodate package slot.
 */
public class AdvancedShulkerVaultScreen extends AbstractContainerScreen<AdvancedShulkerVaultMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ShulkerVault.MOD_ID, "textures/gui/advanced_shulker_vault.png");

    public AdvancedShulkerVaultScreen(AdvancedShulkerVaultMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        // Wider image to accommodate package slot on right
        this.imageWidth = 194;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);

        // Display item logic - package slot takes priority if non-empty (per CLAUDE.md)
        ItemStack itemToDisplay = ItemStack.EMPTY;

        // Check package slot first (slot index 63 = 36 player + 27 vault)
        ItemStack packageItem = this.menu.getBlockEntity().getPackageSlot().getStackInSlot(0);
        if (!packageItem.isEmpty()) {
            itemToDisplay = packageItem;
        } else if (!this.menu.slots.isEmpty() && this.menu.slots.size() > 36) {
            // Default to first vault slot (index 36 = after player inventory)
            itemToDisplay = this.menu.slots.get(36).getItem();
        }

        // Override with hovered slot if it's a vault or package slot
        if (this.hoveredSlot != null) {
            int menuIndex = this.menu.slots.indexOf(this.hoveredSlot);
            // Vault slots are 36-62, package slot is 63
            if (menuIndex >= 36 && menuIndex <= 63) {
                itemToDisplay = this.hoveredSlot.getItem();
            }
        }

        // Hand the actual item to the Block Entity for renderer to display
        this.menu.getBlockEntity().setClientDisplayItem(itemToDisplay);
    }
}
