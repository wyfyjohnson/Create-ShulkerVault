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

public class ShulkerVaultScreen extends AbstractContainerScreen<ShulkerVaultMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ShulkerVault.MOD_ID, "textures/gui/shulker_vault.png");

    public ShulkerVaultScreen(ShulkerVaultMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
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
        // 1. Default to the item in the very first vault slot

        ItemStack itemToDisplay = ItemStack.EMPTY;
        if (!this.menu.slots.isEmpty()) {
            itemToDisplay = this.menu.slots.get(0).getItem();
        }

        // 2. If hovering over a vault slot, use that item instead
        if (this.hoveredSlot != null) {
            int menuIndex = this.menu.slots.indexOf(this.hoveredSlot);
            if (menuIndex >= 0 && menuIndex < 27) { // 0-26 are the vault slots
                itemToDisplay = this.hoveredSlot.getItem();
            }
        }

        // 3. Hand the actual item to the Block Entity
        this.menu.getBlockEntity().setClientDisplaySlot(itemToDisplay);
    }
}