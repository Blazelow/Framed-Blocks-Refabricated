package xfacthd.framedblocks.client.data.extensions.item;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import xfacthd.framedblocks.client.render.item.TankItemRenderer;

public final class TankClientItemExtensions implements IClientItemExtensions
{
    private final BlockEntityWithoutLevelRenderer renderer = new TankItemRenderer();

    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer()
    {
        return renderer;
    }
}
