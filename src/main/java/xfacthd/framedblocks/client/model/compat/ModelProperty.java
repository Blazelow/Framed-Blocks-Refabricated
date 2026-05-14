package xfacthd.framedblocks.client.model.compat;

/**
 * Fabric replacement for {@code net.neoforged.neoforge.client.model.data.ModelProperty<T>}.
 * A typed key used to store and retrieve values in a {@link ModelData} instance.
 * <p>
 * Identity-based — each instance is its own unique key.
 */
public final class ModelProperty<T>
{
    private final String name;

    public ModelProperty(String name)
    {
        this.name = name;
    }

    public ModelProperty()
    {
        this.name = "ModelProperty@" + Integer.toHexString(System.identityHashCode(this));
    }

    @Override
    public String toString()
    {
        return "ModelProperty[" + name + "]";
    }
}
