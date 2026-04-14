package dev.wyfy.shulkervault.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.wyfy.shulkervault.ShulkerVault;
import dev.wyfy.shulkervault.block.custom.AdvancedShulkerVaultBlock;
import dev.wyfy.shulkervault.block.custom.ShulkerVaultBlock;
import dev.wyfy.shulkervault.block.entity.ShulkerVaultBlockEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class ShulkerVaultRenderer implements BlockEntityRenderer<ShulkerVaultBlockEntity> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ShulkerVault.MOD_ID, "textures/block/shulker_vault.png");

    private static final  ResourceLocation ADVANCED_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(ShulkerVault.MOD_ID, "textures/block/advanced_shulker_vault.png");

    private final ModelPart lid;
    private final ModelPart base;

    public ShulkerVaultRenderer(BlockEntityRendererProvider.Context context) {
        // Use vanilla's ShulkerModel layer (matches 64x64 texture with vanilla shulker UVs)
        ModelPart root = context.bakeLayer(ModelLayers.SHULKER);
        this.lid = root.getChild("lid");
        this.base = root.getChild("base");
    }

    @Override
    public void render(ShulkerVaultBlockEntity blockEntity, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {

        Direction direction = Direction.UP;
        BlockState state = blockEntity.getBlockState();
        if (state.getBlock() instanceof ShulkerVaultBlock) {
            direction = state.getValue(ShulkerVaultBlock.FACING);
        }

        float progress = blockEntity.getProgress(partialTick);

        poseStack.pushPose();

        // Vanilla ShulkerBoxRenderer transforms
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.scale(0.9995F, 0.9995F, 0.9995F);
        poseStack.mulPose(direction.getRotation());
        poseStack.scale(1.0F, -1.0F, -1.0F);
        poseStack.translate(0.0F, -1.0F, 0.0F);

        // Animate lid (vanilla pattern)
        this.lid.setPos(0.0F, 24.0F - progress * 0.5F * 16.0F, 0.0F);
        this.lid.yRot = 270.0F * progress * ((float) Math.PI / 180.0F);

        // Add Advanced Shulker Vault's texture
        ResourceLocation texture = (state.getBlock() instanceof AdvancedShulkerVaultBlock)
                ? ADVANCED_TEXTURE
                : TEXTURE;
        VertexConsumer vertexConsumer = bufferSource.getBuffer(
                RenderType.entityCutoutNoCull(texture));

        // Render with custom texture
        this.lid.render(poseStack, vertexConsumer, packedLight, packedOverlay);
        this.base.render(poseStack, vertexConsumer, packedLight, packedOverlay);

        poseStack.popPose();
    }
}
