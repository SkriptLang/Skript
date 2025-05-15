package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.collect.Iterators;
import org.bukkit.entity.Chicken;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ChickenData extends EntityData<Chicken> {

	private static boolean variantsEnabled = false;
	private static Object[] variants;

	static {
		register(ChickenData.class, "chicken", Chicken.class, "chicken");
		if (Skript.classExists("org.bukkit.entity.Chicken$Variant")) {
			variantsEnabled = true;
			variants = Iterators.toArray(Classes.getExactClassInfo(Chicken.Variant.class).getSupplier().get(), Chicken.Variant.class);
		}
	}

	private @Nullable Object variant = null;

	public ChickenData() {}

	// TODO: When safe, 'variant' should have the type changed to 'Chicken.Variant'
	public ChickenData(@Nullable Object variant) {
		this.variant = variant;
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		if (exprs[0] != null && variantsEnabled)
			//noinspection unchecked
			variant = ((Literal<Chicken.Variant>) exprs[0]).getSingle();
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Chicken> entityClass, @Nullable Chicken chicken) {
		if (chicken != null) {
			if (variantsEnabled)
				variant = chicken.getVariant();
		}
		return true;
	}

	@Override
	public void set(Chicken chicken) {
		if (variantsEnabled) {
			Object finalVariant = variant != null ? variant : CollectionUtils.getRandom(variants);
			assert finalVariant != null;
			chicken.setVariant((Chicken.Variant) finalVariant);
		}
	}

	@Override
	protected boolean match(Chicken chicken) {
		return variant == null || variant == chicken.getVariant();
	}

	@Override
	public Class<? extends Chicken> getType() {
		return Chicken.class;
	}

	@Override
	public EntityData<Chicken> getSuperType() {
		return new ChickenData();
	}

	@Override
	protected int hashCode_i() {
		return Objects.hashCode(variant);
	}

	@Override
	protected boolean equals_i(EntityData<?> obj) {
		if (!(obj instanceof ChickenData other))
			return false;
		return variant == null || variant == other.variant;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof ChickenData other))
			return false;
		return variant == null || variant == other.variant;
	}

	/**
	 * A dummy/placeholder class to ensure working operation on MC versions that do not have `Chicken.Variant`
	 */
	public static class ChickenVariantDummy {}

}
