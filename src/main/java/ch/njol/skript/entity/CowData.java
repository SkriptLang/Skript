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

	private static boolean variantsEnabled = false;
	private static Object[] variants;
	private static Class<Cow> cowClass;
	private static @Nullable Method getVariantMethod = null;
	private static @Nullable Method setVariantMethod = null;

	static {
		try {
			//noinspection unchecked
			cowClass = (Class<Cow>) Class.forName("org.bukkit.entity.Cow");
		} catch (Exception ignored) {}

		register(CowData.class, "cow", cowClass, 0, "cow");
		if (Skript.classExists("org.bukkit.entity.Cow$Variant")) {
			variantsEnabled = true;
			variants = Iterators.toArray(Classes.getExactClassInfo(Cow.Variant.class).getSupplier().get(), Cow.Variant.class);
			try {
				getVariantMethod = cowClass.getDeclaredMethod("getVariant");
				setVariantMethod = cowClass.getDeclaredMethod("setVariant", Cow.Variant.class);
			} catch (Exception ignored) {}
		}
	}

	private @Nullable Object variant = null;

	public CowData() {}

	// TODO: When safe, 'variant' should have the type changed to 'Cow.Variant'
	public CowData(@Nullable Object variant) {
		this.variant = variant;
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		if (exprs[0] != null && variantsEnabled)
			//noinspection unchecked
			variant = ((Literal<Cow.Variant>) exprs[0]).getSingle();
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Cow> entityClass, @Nullable Cow entity) {
		if (entity != null) {
			if (variantsEnabled) {
				variant = getVariant(entity);
			}
		}
		return true;
	}

	@Override
	public void set(Cow entity) {
		if (variantsEnabled) {
			Object finalVariant = variant != null ? variant : CollectionUtils.getRandom(variants);
			assert finalVariant != null;
			setVariant(entity, finalVariant);
		}
	}

	@Override
	protected boolean match(Cow entity) {
		if (!variantsEnabled)
			return true;
		return variant == null || getVariant(entity) == variant;
	}

	@Override
	public Class<Cow> getType() {
		return cowClass;
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
		if (variantsEnabled && variant != other.variant)
			return false;
		return true;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof CowData other))
			return false;
		if (variantsEnabled)
			return variant == other.variant;
		return true;
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
		if (!variantsEnabled || setVariantMethod == null)
			return;
		Entity entity = cowClass.cast(cow);
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
		if (!variantsEnabled || getVariantMethod == null)
			return null;
		Entity entity = cowClass.cast(cow);
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