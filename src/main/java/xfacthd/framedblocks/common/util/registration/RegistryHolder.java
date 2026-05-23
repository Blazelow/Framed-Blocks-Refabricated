package xfacthd.framedblocks.common.util.registration;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import com.mojang.datafixers.util.Either;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Fabric replacement for NeoForge's DeferredHolder.
 * <p>
 * Wraps a {@link Holder.Reference} once the entry has been registered.
 * Before registration completes the holder is in an "unbound" state — calling
 * {@link #value()} before {@code FabricRegistrar.registerAll()} will throw.
 */
public class RegistryHolder<R, T extends R> implements Holder<T>
{
    private final ResourceKey<R> key;
    private Holder.Reference<T> delegate;

    protected RegistryHolder(ResourceKey<R> key)
    {
        this.key = key;
    }

    @SuppressWarnings("unchecked")
    void bind(Holder.Reference<? extends R> ref)
    {
        this.delegate = (Holder.Reference<T>) ref;
    }

    private Holder.Reference<T> delegate()
    {
        if (delegate == null)
        {
            throw new IllegalStateException("RegistryHolder for " + key + " has not been bound yet. " +
                    "Was FabricRegistrar.registerAll() called?");
        }
        return delegate;
    }

    // ---- Holder<T> implementation ----

    @Override
    public @NotNull T value()
    {
        return delegate().value();
    }

    @Override
    public boolean isBound()
    {
        return delegate != null && delegate.isBound();
    }

    @Override
    public boolean is(@NotNull ResourceLocation id)
    {
        return key.location().equals(id);
    }

    @Override
    public boolean is(@NotNull ResourceKey<T> key)
    {
        return this.key.equals(key);
    }

    @Override
    public boolean is(@NotNull Predicate<ResourceKey<T>> predicate)
    {
        //noinspection unchecked
        return predicate.test((ResourceKey<T>) key);
    }

    @Override
    public boolean is(@NotNull TagKey<T> tag)
    {
        return delegate().is(tag);
    }

    @Override
    public boolean is(Holder<T> other)
    {
        return delegate().is(other);
    }

    @Override
    public @NotNull Stream<TagKey<T>> tags()
    {
        return delegate().tags();
    }

    @Override
    public @NotNull Either<ResourceKey<T>, T> unwrap()
    {
        //noinspection unchecked
        return Either.left((ResourceKey<T>) key);
    }

    @Override
    public @NotNull Optional<ResourceKey<T>> unwrapKey()
    {
        //noinspection unchecked
        return Optional.of((ResourceKey<T>) key);
    }

    @Override
    public @NotNull Kind kind()
    {
        return Kind.REFERENCE;
    }

    @Override
    public boolean canSerializeIn(@NotNull HolderOwner<T> owner)
    {
        return delegate().canSerializeIn(owner);
    }

    @Override
    public @NotNull Optional<Registry<T>> unwrapRegistry()
    {
        return delegate().unwrapRegistry();
    }

    /** Exposes the underlying ResourceKey (mirrors NeoForge DeferredHolder.getKey()) */
    public ResourceKey<R> getKey()
    {
        return key;
    }

    @Override
    public String toString()
    {
        return "RegistryHolder{" + key + ", bound=" + isBound() + "}";
    }
}
