package xfacthd.framedblocks.client.render.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.*;
import net.fabricmc.loader.api.FabricLoader;
import xfacthd.framedblocks.api.block.blockentity.FramedBlockEntity;
import xfacthd.framedblocks.api.render.debug.AttachDebugRenderersEvent;
import xfacthd.framedblocks.api.render.debug.BlockDebugRenderer;
import xfacthd.framedblocks.common.config.DevToolsConfig;

import java.util.*;

public final class FramedBlockDebugRenderer
{
    private static final Map<BlockEntityType<? extends FramedBlockEntity>, Set<BlockDebugRenderer<? extends FramedBlockEntity>>> RENDERERS_BY_TYPE = new IdentityHashMap<>();

    @SuppressWarnings("unchecked")
    public static void render(DeltaTracker delta, Camera camera, PoseStack poseStack)
    {
        HitResult hit = Minecraft.getInstance().hitResult;
        if (!(hit instanceof BlockHitResult blockHit)) return;

        ClientLevel level = Objects.requireNonNull(Minecraft.getInstance().level);
        BlockPos pos = blockHit.getBlockPos();
        if (!(level.getBlockEntity(pos) instanceof FramedBlockEntity be)) return;

        Set<BlockDebugRenderer<? extends FramedBlockEntity>> renderers = RENDERERS_BY_TYPE.get(be.getType());
        if (renderers.isEmpty()) return;

        Vec3 offset = Vec3.atLowerCornerOf(pos).subtract(camera.getPosition());
        poseStack.pushPose();
        poseStack.translate(offset.x, offset.y, offset.z);

        float partialTick = delta.getGameTimeDeltaPartialTick(false);
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        for (BlockDebugRenderer<? extends FramedBlockEntity> renderer : renderers)
        {
            if (!renderer.isEnabled()) continue;

            poseStack.pushPose();
            ((BlockDebugRenderer<FramedBlockEntity>) renderer).render(be, blockHit, partialTick, poseStack, buffer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
        poseStack.popPose();
        buffer.endBatch();
    }

    /** Called before each frame render. Registered via WorldRenderEvents.START in init(). */
    private static void onRenderFramePre(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext ctx)
    {
        if (DevToolsConfig.VIEW.isDoubleBlockPartHitDebugRendererEnabled())
        {
            Minecraft.getInstance().levelRenderer.requestOutlineEffect();
        }
    }

    public static void init()
    {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) return;

        /* TODO (Step 7): fire AttachDebugRenderersEvent( */ new AttachDebugRenderersEvent((type, renderer) ->
                RENDERERS_BY_TYPE.computeIfAbsent(type, $ -> new ReferenceOpenHashSet<>()).add(renderer)
        ));

        net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.START.register(
                FramedBlockDebugRenderer::onRenderFramePre);
    }

    private FramedBlockDebugRenderer() { }
}
