package xfacthd.framedblocks.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import xfacthd.framedblocks.api.block.FramedProperties;
import xfacthd.framedblocks.common.blockentity.special.FramedTankBlockEntity;
import xfacthd.framedblocks.common.capability.TankFluidHandler;
import xfacthd.framedblocks.common.data.component.FluidContent;

public class FramedTankRenderer implements BlockEntityRenderer<FramedTankBlockEntity>
{
    private static final float OFFSET = .01F;
    private static final float MIN_XZ = OFFSET;
    private static final float MAX_XZ = 1F - OFFSET;

    public FramedTankRenderer(@SuppressWarnings("unused") BlockEntityRendererProvider.Context ctx) { }

    @Override
    public void render(FramedTankBlockEntity be, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int light, int overlay)
    {
        FluidContent fluid = be.getContents();
        if (fluid.isEmpty() || be.getLevel() == null) return;

        Fluid f = fluid.fluid();
        FluidRenderHandler handler = FluidRenderHandlerRegistry.INSTANCE.get(f);
        if (handler == null) return;

        FluidState fluidState = f.defaultFluidState();
        TextureAtlasSprite[] sprites = handler.getFluidSprites(be.getLevel(), be.getBlockPos(), fluidState);
        int tint = handler.getFluidColor(be.getLevel(), be.getBlockPos(), fluidState);

        ResourceLocation stillTex  = sprites[0].contents().name();
        ResourceLocation flowTex   = sprites[1].contents().name();
        RenderType renderType      = ItemBlockRenderTypes.getRenderLayer(fluidState);

        renderContents(poseStack, buffer, renderType, light, fluid.amount(), sprites[0], sprites[1], tint);
    }

    public static void renderContents(
            PoseStack poseStack,
            MultiBufferSource buffer,
            RenderType renderType,
            int light,
            long fluidAmount,
            TextureAtlasSprite stillSprite,
            TextureAtlasSprite flowSprite,
            int tint
    )
    {
        float height = Mth.clamp(fluidAmount / (float) TankFluidHandler.CAPACITY, OFFSET, 1F - OFFSET);
        boolean sameTex = stillSprite.contents().name().equals(flowSprite.contents().name());

        VertexConsumer consumer = buffer.getBuffer(renderType);
        PoseStack.Pose pose = poseStack.last();

        TextureAtlasSprite sprite = flowSprite;
        float minU = sprite.getU(MIN_XZ);
        float maxU = sameTex ? sprite.getU(MAX_XZ) : sprite.getU(8F / 16F - OFFSET);
        float minV = sameTex ? sprite.getV(1F - height) : sprite.getV(8F / 16F * (1F - height));
        float maxV = sameTex ? sprite.getV(MAX_XZ) : sprite.getV(8F / 16F - OFFSET);

        int r = (tint >> 16) & 0xFF, g = (tint >> 8) & 0xFF, b = tint & 0xFF, a = (tint >> 24) & 0xFF;
        if (a == 0) a = 255;

        // West
        consumer.addVertex(pose, MIN_XZ, height, MIN_XZ).setColor(r,g,b,a).setUv(minU, minV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1F, 0F, 0F);
        consumer.addVertex(pose, MIN_XZ, OFFSET, MIN_XZ).setColor(r,g,b,a).setUv(minU, maxV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1F, 0F, 0F);
        consumer.addVertex(pose, MIN_XZ, OFFSET, MAX_XZ).setColor(r,g,b,a).setUv(maxU, maxV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1F, 0F, 0F);
        consumer.addVertex(pose, MIN_XZ, height, MAX_XZ).setColor(r,g,b,a).setUv(maxU, minV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, -1F, 0F, 0F);
        // East
        consumer.addVertex(pose, MAX_XZ, height, MAX_XZ).setColor(r,g,b,a).setUv(maxU, minV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1F, 0F, 0F);
        consumer.addVertex(pose, MAX_XZ, OFFSET, MAX_XZ).setColor(r,g,b,a).setUv(maxU, maxV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1F, 0F, 0F);
        consumer.addVertex(pose, MAX_XZ, OFFSET, MIN_XZ).setColor(r,g,b,a).setUv(minU, maxV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1F, 0F, 0F);
        consumer.addVertex(pose, MAX_XZ, height, MIN_XZ).setColor(r,g,b,a).setUv(minU, minV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 1F, 0F, 0F);
        // North
        consumer.addVertex(pose, MAX_XZ, height, MIN_XZ).setColor(r,g,b,a).setUv(maxU, minV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0F, 0F, -1F);
        consumer.addVertex(pose, MAX_XZ, OFFSET, MIN_XZ).setColor(r,g,b,a).setUv(maxU, maxV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0F, 0F, -1F);
        consumer.addVertex(pose, MIN_XZ, OFFSET, MIN_XZ).setColor(r,g,b,a).setUv(minU, maxV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0F, 0F, -1F);
        consumer.addVertex(pose, MIN_XZ, height, MIN_XZ).setColor(r,g,b,a).setUv(minU, minV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0F, 0F, -1F);
        // South
        consumer.addVertex(pose, MIN_XZ, height, MAX_XZ).setColor(r,g,b,a).setUv(minU, minV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0F, 0F, 1F);
        consumer.addVertex(pose, MIN_XZ, OFFSET, MAX_XZ).setColor(r,g,b,a).setUv(minU, maxV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0F, 0F, 1F);
        consumer.addVertex(pose, MAX_XZ, OFFSET, MAX_XZ).setColor(r,g,b,a).setUv(maxU, maxV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0F, 0F, 1F);
        consumer.addVertex(pose, MAX_XZ, height, MAX_XZ).setColor(r,g,b,a).setUv(maxU, minV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0F, 0F, 1F);

        // Use still sprite for top/bottom faces
        sprite = sameTex ? flowSprite : stillSprite;
        minU = sprite.getU(MIN_XZ); maxU = sprite.getU(MAX_XZ);
        minV = sprite.getV(MIN_XZ); maxV = sprite.getV(MAX_XZ);

        // Up
        consumer.addVertex(pose, MAX_XZ, height, MAX_XZ).setColor(r,g,b,a).setUv(maxU, maxV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0F, 1F, 0F);
        consumer.addVertex(pose, MAX_XZ, height, MIN_XZ).setColor(r,g,b,a).setUv(maxU, minV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0F, 1F, 0F);
        consumer.addVertex(pose, MIN_XZ, height, MIN_XZ).setColor(r,g,b,a).setUv(minU, minV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0F, 1F, 0F);
        consumer.addVertex(pose, MIN_XZ, height, MAX_XZ).setColor(r,g,b,a).setUv(minU, maxV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0F, 1F, 0F);
        // Down
        consumer.addVertex(pose, MIN_XZ, OFFSET, MAX_XZ).setColor(r,g,b,a).setUv(minU, maxV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0F, -1F, 0F);
        consumer.addVertex(pose, MIN_XZ, OFFSET, MIN_XZ).setColor(r,g,b,a).setUv(minU, minV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0F, -1F, 0F);
        consumer.addVertex(pose, MAX_XZ, OFFSET, MIN_XZ).setColor(r,g,b,a).setUv(maxU, minV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0F, -1F, 0F);
        consumer.addVertex(pose, MAX_XZ, OFFSET, MAX_XZ).setColor(r,g,b,a).setUv(maxU, maxV).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0F, -1F, 0F);
    }

    @Override
    public boolean shouldRender(FramedTankBlockEntity be, Vec3 camera)
    {
        return !be.getBlockState().getValue(FramedProperties.SOLID) && BlockEntityRenderer.super.shouldRender(be, camera);
    }
}
