package dev.wyfy.shulkervault.item.custom;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

/**
 * Custom BlockItem for Shulker Vaults that prevents NBT inception.
 * By returning false from canFitInsideContainerItems(), vanilla shulker boxes
 * and bundles will reject this item, preventing nested container corruption.
 */
public class ShulkerVaultBlockItem extends BlockItem {

    public ShulkerVaultBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public boolean canFitInsideContainerItems() {
        // Prevent this item from being placed inside shulker boxes, bundles, etc.
        return false;
    }
}
