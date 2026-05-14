package xfacthd.framedblocks.common.compat.diagonalblocks;

import fuzs.diagonalblocks.api.v2.DiagonalBlockType;
import fuzs.diagonalblocks.api.v2.DiagonalBlockTypes;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.loader.api.FabricLoader;
import xfacthd.framedblocks.FramedBlocks;
import xfacthd.framedblocks.api.block.render.FramedBlockRenderProperties;
import xfacthd.framedblocks.api.model.wrapping.RegisterModelWrappersEvent;
import xfacthd.framedblocks.api.model.wrapping.WrapHelper;
import xfacthd.framedblocks.api.util.Utils;
import xfacthd.framedblocks.api.block.render.FramedBlockColor;
import xfacthd.framedblocks.common.FBContent;
import xfacthd.framedblocks.common.block.pane.FramedPaneBlock;
import xfacthd.framedblocks.common.block.pillar.FramedFenceBlock;

import java.util.Optional;

public final class DiagonalBlocksCompat
{
    private static boolean loaded = false;

    public static void init()
    {
        if (FabricLoader.getInstance().isModLoaded("diagonalblocks"))
        {
            try
            {
                GuardedAccess.init(modBus);
                if (FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.CLIENT)
                {
                    GuardedClientAccess.init(modBus);
                }
                loaded = true;
            }
            catch (Throwable t)
            {
                FramedBlocks.LOGGER.error("Failed to initialized Diagonal Blocks integration");
            }
        }
    }

    public static boolean isFramedFence(BlockState state)
    {
        return (loaded && GuardedAccess.isFramedFence(state)) || state.getBlock() instanceof FramedFenceBlock;
    }

    public static boolean isFramedPane(BlockState state)
    {
        return (loaded && GuardedAccess.isFramedPane(state)) || state.getBlock() instanceof FramedPaneBlock;
    }



    private static final class GuardedAccess
    {
        public static void init()
        {
            DiagonalBlockTypes.FENCE.registerBlockFactory(
                    Utils.getKeyOrThrow(FBContent.BLOCK_FRAMED_FENCE).location(),
                    FramedDiagonalFenceBlock::new
            );
            DiagonalBlockTypes.WINDOW.registerBlockFactory(
                    Utils.getKeyOrThrow(FBContent.BLOCK_FRAMED_PANE).location(),
                    FramedDiagonalGlassPaneBlock::new
            );
            DiagonalBlockTypes.WINDOW.disableBlockFactory(Utils.getKeyOrThrow(FBContent.BLOCK_FRAMED_BARS).location());
            DiagonalBlockTypes.WALL.disableBlockFactory(Utils.getKeyOrThrow(FBContent.BLOCK_FRAMED_WALL).location());

        }

        /**
         * TODO: On Fabric, BlockEntityType does not support post-registration block additions.
         * The diagonal fence/pane blocks should be added to BE_TYPE_FRAMED_BLOCK's block set
         * at registration time in FBContent, but only when diagonalblocks is loaded.
         * For now, BE_TYPE_FRAMED_BLOCK will not cover diagonal variants.
         */
        private static void registerDiagonalBlockEntityBlocks()
        {
            // Stub: Fabric has no BlockEntityTypeAddBlocksEvent equivalent
        }

        public static boolean isFramedFence(BlockState state)
        {
            return state.getBlock() instanceof FramedDiagonalFenceBlock;
        }

        public static boolean isFramedPane(BlockState state)
        {
            return state.getBlock() instanceof FramedDiagonalGlassPaneBlock;
        }

        private static Optional<Holder.Reference<Block>> getBlock(DiagonalBlockType type, Holder<Block> srcBlock)
        {
            ResourceLocation srcName = Utils.getKeyOrThrow(srcBlock).location();
            ResourceLocation destName = type.id(srcName.getNamespace() + "/" + srcName.getPath());
            return BuiltInRegistries.BLOCK.getHolder(ResourceKey.create(Registries.BLOCK, destName));
        }



        private GuardedAccess() { }
    }

    private static final class GuardedClientAccess
    {
        public static void init()
        {
            onRegisterModelWrappers();
            onRegisterBlockColors();
            onRegisterClientExtensions();
        }

        static void onRegisterModelWrappers()
        {
            GuardedAccess.getBlock(DiagonalBlockTypes.FENCE, FBContent.BLOCK_FRAMED_FENCE).ifPresent(
                    holder -> WrapHelper.wrap(holder, FramedDiagonalFenceGeometry::new, WrapHelper.IGNORE_WATERLOGGED_LOCK)
            );
            GuardedAccess.getBlock(DiagonalBlockTypes.WINDOW, FBContent.BLOCK_FRAMED_PANE).ifPresent(
                    holder -> WrapHelper.wrap(holder, FramedDiagonalPaneGeometry::new, WrapHelper.IGNORE_WATERLOGGED_LOCK)
            );
        }

        static void onRegisterBlockColors()
        {
            GuardedAccess.getBlock(DiagonalBlockTypes.FENCE, FBContent.BLOCK_FRAMED_FENCE).ifPresent(
                    holder -> net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry.BLOCK
                            .register(FramedBlockColor.INSTANCE, holder.value())
            );
            GuardedAccess.getBlock(DiagonalBlockTypes.WINDOW, FBContent.BLOCK_FRAMED_PANE).ifPresent(
                    holder -> net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry.BLOCK
                            .register(FramedBlockColor.INSTANCE, holder.value())
            );
        }

        static void onRegisterClientExtensions()
        {
            // TODO: IClientBlockExtensions has no Fabric equivalent; diagonal framed blocks
            // will use default render properties. Re-evaluate when FRAPI mixin approach is available.
        }
    }



    private DiagonalBlocksCompat() { }
}
