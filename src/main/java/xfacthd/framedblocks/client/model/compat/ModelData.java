package xfacthd.framedblocks.client.model.compat;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Fabric replacement for {@code net.neoforged.neoforge.client.model.data.ModelData}.
 * A typed key-value store passed to {@code BakedModel.getQuads()} so geometry can
 * access per-block-entity data (camo content, reinforcement state, etc.).
 * <p>
 * Drop-in replacement: same {@code get(ModelProperty)}, {@code builder()}, {@code EMPTY} API.
 */
public final class ModelData
{
    public static final ModelData EMPTY = new ModelData(Map.of());

    private final Map<ModelProperty<?>, Object> data;

    private ModelData(Map<ModelProperty<?>, Object> data)
    {
        this.data = data;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T get(ModelProperty<T> property)
    {
        return (T) data.get(property);
    }

    public boolean has(ModelProperty<?> property)
    {
        return data.containsKey(property);
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public Builder toBuilder()
    {
        Builder b = new Builder();
        b.values.putAll(data);
        return b;
    }

    public static final class Builder
    {
        private final Map<ModelProperty<?>, Object> values = new HashMap<>();

        public <T> Builder with(ModelProperty<T> property, T value)
        {
            values.put(property, value);
            return this;
        }

        public ModelData build()
        {
            return values.isEmpty() ? EMPTY : new ModelData(Map.copyOf(values));
        }
    }
}
