package xfacthd.framedblocks.common.compat;

import xfacthd.framedblocks.common.compat.additionalplacements.AdditionalPlacementsCompat;
import xfacthd.framedblocks.common.compat.ae2.AppliedEnergisticsCompat;
import xfacthd.framedblocks.common.compat.amendments.AmendmentsCompat;
import xfacthd.framedblocks.common.compat.athena.AthenaCompat;
import xfacthd.framedblocks.common.compat.atlasviewer.AtlasViewerCompat;
import xfacthd.framedblocks.common.compat.buildinggadgets.BuildingGadgetsCompat;
import xfacthd.framedblocks.common.compat.create.CreateCompat;
import xfacthd.framedblocks.common.compat.diagonalblocks.DiagonalBlocksCompat;
import xfacthd.framedblocks.common.compat.searchables.SearchablesCompat;

public final class CompatHandler
{
    public static void init()
    {
        AdditionalPlacementsCompat.init();
        AppliedEnergisticsCompat.init();
        AmendmentsCompat.init();
        AthenaCompat.init();
        AtlasViewerCompat.init();
        BuildingGadgetsCompat.init();
        CreateCompat.init();
        DiagonalBlocksCompat.init();
        SearchablesCompat.init();
    }

    public static void commonSetup()
    {
        CreateCompat.commonSetup();
    }



    private CompatHandler() { }
}
