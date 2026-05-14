package xfacthd.framedblocks.client.render.special;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import xfacthd.framedblocks.FramedBlocks;
import xfacthd.framedblocks.api.block.IFramedBlock;
import xfacthd.framedblocks.api.render.RegisterOutlineRenderersEvent;
import xfacthd.framedblocks.api.type.IBlockType;
import xfacthd.framedblocks.api.render.OutlineRenderer;
import xfacthd.framedblocks.common.config.ClientConfig;
import xfacthd.framedblocks.common.config.DevToolsConfig;

import java.util.*;

public final class BlockOutlineRenderer
{
    private static final Map<IBlockType, OutlineRenderer> OUTLINE_RENDERERS = new IdentityHashMap<>();
    private static final Set<IBlockType> ERRORED_TYPES = new HashSet<>();

    /**
     * Registered via {@link net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents#BLOCK_OUTLINE}.
     * Fabric signature: (WorldRenderContext ctx, BlockOutlineContext outlineCtx)
     */
    public static boolean onRenderBlockHighlight(
            net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext ctx,
            net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.BlockOutlineContext outlineCtx)
    {
        if (!ClientConfig.VIEW.useFancySelectionBoxes() && !DevToolsConfig.VIEW.isOcclusionShapeDebugRenderingEnabled())
        {
            return;
        }

        net.minecraft.core.BlockPos hitPos = outlineCtx.blockPos();
        net.minecraft.phys.BlockHitResult result = new net.minecraft.phys.BlockHitResult(
                outlineCtx.hitPos(), outlineCtx.direction(), hitPos, false);
        BlockState state = Minecraft.getInstance().level.getBlockState(hitPos);
        if (!(state.getBlock() instanceof IFramedBlock block))
        {
            return;
        }

        if (DevToolsConfig.VIEW.isOcclusionShapeDebugRenderingEnabled())
        {
            VertexConsumer builder = ctx.consumers().getBuffer(RenderType.lines());
            VoxelShape shape = state.getOcclusionShape(Minecraft.getInstance().level, result.getBlockPos());
            Vec3 offset = Vec3.atLowerCornerOf(result.getBlockPos()).subtract(ctx.camera().getPosition());
            LevelRenderer.renderShape(ctx.matrixStack(), builder, shape, offset.x, offset.y, offset.z, 0F, 0F, 0F, .4F);
            return false; // cancel vanilla outline
            return;
        }

        IBlockType type = block.getBlockType();
        if (type.hasSpecialHitbox())
        {
            OutlineRenderer renderer = OUTLINE_RENDERERS.get(type);
            if (renderer == null)
            {
                if (ERRORED_TYPES.add(type))
                {
                    FramedBlocks.LOGGER.error("IBlockType '{}' requests custom outline rendering but no OutlineRender was registered!", type.getName());
                }
                return;
            }

            PoseStack mstack = ctx.matrixStack();
            Vec3 offset = Vec3.atLowerCornerOf(result.getBlockPos()).subtract(ctx.camera().getPosition());
            VertexConsumer builder = ctx.consumers().getBuffer(RenderType.lines());

            mstack.pushPose();
            mstack.translate(offset.x, offset.y, offset.z);
            mstack.translate(.5, .5, .5);
            renderer.rotateMatrix(mstack, state);
            mstack.translate(-.5, -.5, -.5);

            renderer.draw(state, Minecraft.getInstance().level, result.getBlockPos(), mstack, builder);

            mstack.popPose();

            return false; // cancel vanilla outline
        }
    }

    public static void init()
    {
        /* TODO (Step 7): fire RegisterOutlineRenderersEvent( */ new RegisterOutlineRenderersEvent((type, renderer) ->
        {
            Preconditions.checkArgument(
                    type.hasSpecialHitbox(),
                    "IBlockType %s doesn't return true from IBlockType#hasSpecialHitbox()",
                    type
            );
            OUTLINE_RENDERERS.put(type, renderer);
        }));
    }

    public static boolean hasOutlineRenderer(IBlockType type)
    {
        return OUTLINE_RENDERERS.containsKey(type);
    }



    private BlockOutlineRenderer() { }
}
