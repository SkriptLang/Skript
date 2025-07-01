package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.collect.Iterators;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class CowData extends EntityData<Cow> {

	private static final boolean VARIANTS_ENABLED;
	private static final Object[] variants;
	private static final Class<Cow> COW_CLASS;
	private static final @Nullable Method getVariantMethod;
	private static final @Nullable Method setVariantMethod;

	static {
		Class<Cow> cowClass = null;

		try {
			//noinspection unchecked
			cowClass = (Class<Cow>) Class.forName("org.bukkit.entity.Cow");
		} catch (Exception ignored) {}

		COW_CLASS = cowClass;
		register(CowData.class, "cow", COW_CLASS, 0, "cow");
		if (Skript.classExists("org.bukkit.entity.Cow$Variant")) {
			VARIANTS_ENABLED = true;
			variants = Iterators.toArray(Classes.getExactClassInfo(Cow.Variant.class).getSupplier().get(), Cow.Variant.class);
			try {
				getVariantMethod = COW_CLASS.getDeclaredMethod("getVariant");
				setVariantMethod = COW_CLASS.getDeclaredMethod("setVariant", Cow.Variant.class);
			} catch (Exception e) {
				throw new RuntimeException("Could not retrieve get/set variant methods for Cow.", e);
			}
		} else {
			VARIANTS_ENABLED = false;
			variants = null;
			getVariantMethod = null;
			setVariantMethod = null;
		}
	}

	private @Nullable Object variant = null;

	public CowData() {}

	// TODO: When the api-version is 1.21.5, 'variant' should have the type changed to 'Cow.Variant' and reflection can be removed
	public CowData(@Nullable Object variant) {
		this.variant = variant;
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		if (VARIANTS_ENABLED) {
			Literal<?> expr = null;
			if (exprs[0] != null) { // cow
				expr = exprs[0];
			} else if (exprs[1] != null) { // calf
				expr = exprs[1];
			}
			if (expr != null)
				//noinspection unchecked
				variant = ((Literal<Cow.Variant>) expr).getSingle();
		}
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Cow> entityClass, @Nullable Cow cow) {
		if (cow != null && VARIANTS_ENABLED) {
			variant = getVariant(cow);
		}
		return true;
	}

	@Override
	public void set(Cow entity) {
		if (!VARIANTS_ENABLED)
			return;
		Object finalVariant = variant != null ? variant : CollectionUtils.getRandom(variants);
		assert finalVariant != null;
		setVariant(entity, finalVariant);
	}

	@Override
	protected boolean match(Cow entity) {
		return variant == null || getVariant(entity) == variant;
	}

	@Override
	public Class<Cow> getType() {
		return COW_CLASS;
	}

	@Override
	public EntityData<Cow> getSuperType() {
		return new CowData();
	}

	@Override
	protected int hashCode_i() {
		return Objects.hashCode(variant);
	}

	@Override
	protected boolean equals_i(EntityData<?> obj) {
		if (!(obj instanceof CowData other))
			return false;
		return variant == other.variant;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof CowData other))
			return false;
		return variant == null || variant == other.variant;
	}

	/**
	 * Due to the addition of 'AbstractCow' and 'api-version' being '1.19'
	 * This helper method is required in order to set the {@link #variant} of the {@link Cow}
	 * @param cow The {@link Cow} to set the variant
	 */
	public void setVariant(Cow cow) {
		setVariant(cow, variant);
	}

	/**
	 * Due to the addition of 'AbstractCow' and 'api-version' being '1.19'
	 * This helper method is required in order to set the {@code object} of the {@link Cow}
	 * @param cow The {@link Cow} to set the variant
	 * @param object The 'Cow.Variant'
	 */
	public void setVariant(Cow cow, Object object) {
		if (!VARIANTS_ENABLED || setVariantMethod == null)
			return;
		Entity entity = COW_CLASS.cast(cow);
		try {
			setVariantMethod.invoke(entity, (Cow.Variant) object);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Due to the addition of 'AbstractCow' and 'api-version' being '1.19'
	 * This helper method is required in order to get the 'Cow.Variant' of the {@link Cow}
	 * @param cow The {@link Cow} to get the variant
	 * @return The 'Cow.Variant'
	 */
	public @Nullable Object getVariant(Cow cow) {
		if (!VARIANTS_ENABLED || getVariantMethod == null)
			return null;
		Entity entity = COW_CLASS.cast(cow);
		try {
			return getVariantMethod.invoke(entity);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * A dummy/placeholder class to ensure working operation on MC versions that do not have 'Cow.Variant'
	 */
	public static class CowVariantDummy {}

}
