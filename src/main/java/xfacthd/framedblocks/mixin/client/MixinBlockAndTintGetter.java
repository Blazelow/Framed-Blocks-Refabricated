package xfacthd.framedblocks.mixin.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xfacthd.framedblocks.api.block.blockentity.FramedBlockEntity;
import xfacthd.framedblocks.client.model.compat.ModelData;

/**
 * Injects {@code getModelData(BlockPos)} into {@link BlockAndTintGetter}.
 * On NeoForge this method exists natively; on Fabric we synthesize it from
 * the block entity's {@link FramedBlockEntity#getModelData()} method.
 */
@Mixin(BlockAndTintGetter.class)
public interface MixinBlockAndTintGetter
{
    @Shadow
    BlockEntity getBlockEntity(BlockPos pos);

    default ModelData getModelData(BlockPos pos)
    {
        BlockEntity be = getBlockEntity(pos);
        if (be instanceof FramedBlockEntity fbe)
        {
            return fbe.getModelData();
        }
        return ModelData.EMPTY;
    }
}
