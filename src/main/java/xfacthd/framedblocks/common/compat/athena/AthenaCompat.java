package xfacthd.framedblocks.common.compat.athena;

import earth.terrarium.athena.api.client.neoforge.AthenaBakedModel;
import net.fabricmc.loader.api.FabricLoader;
import xfacthd.framedblocks.FramedBlocks;
import xfacthd.framedblocks.client.data.ConTexDataHandler;

public final class AthenaCompat
{
    public static void init()
    {
        if (FabricLoader.getInstance().isModLoaded("athena"))
        {
            try
            {
                if (FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.CLIENT)
                {
                    GuardedClientAccess.init();
                }
            }
            catch (Throwable e)
            {
                FramedBlocks.LOGGER.warn("An error occured while initializing Athena integration!", e);
            }
        }
    }

    private static final class GuardedClientAccess
    {
        public static void init()
        {
            ConTexDataHandler.addConTexProperty("athena", AthenaBakedModel.DATA);
        }
    }



    private AthenaCompat() { }
}
