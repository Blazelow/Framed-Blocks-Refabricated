package xfacthd.framedblocks.common.data;

import org.jetbrains.annotations.Nullable;
import xfacthd.framedblocks.api.util.Utils;

import java.util.Objects;

public enum FramedToolType
{
    HAMMER("framed_hammer", Utils.ACTION_WRENCH_EMPTY),
    WRENCH("framed_wrench", Utils.ACTION_WRENCH_ROTATE),
    BLUEPRINT("framed_blueprint", null),
    KEY("framed_key", null),
    SCREWDRIVER("framed_screwdriver", Utils.ACTION_WRENCH_CONFIGURE),
    ;

    private final String name;
    @Nullable
    private final String ability;

    FramedToolType(String name, @Nullable String ability)
    {
        this.name = name;
        this.ability = ability;
    }

    public String getName()
    {
        return name;
    }

    public boolean hasAbility()
    {
        return ability != null;
    }

    public String getAbility()
    {
        return Objects.requireNonNull(ability);
    }
}
