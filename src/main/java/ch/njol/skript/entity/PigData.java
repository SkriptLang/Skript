package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.collect.Iterators;
import org.bukkit.entity.Pig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PigData extends EntityData<Pig> {

	private static final boolean VARIANTS_ENABLED;
	private static final Object[] VARIANTS;
	private static final Patterns<Kleenean> PATTERNS = new Patterns<>(new Object[][]{
		{"pig", Kleenean.UNKNOWN},
		{"saddled pig", Kleenean.TRUE},
		{"unsaddled pig", Kleenean.FALSE}
	});

	static {
		EntityData.register(PigData.class, "pig", Pig.class, 0, PATTERNS.getPatterns());
		if (Skript.classExists("org.bukkit.entity.Pig$Variant")) {
			VARIANTS_ENABLED = true;
			VARIANTS = Iterators.toArray(Classes.getExactClassInfo(Pig.Variant.class).getSupplier().get(), Pig.Variant.class);
		} else {
			VARIANTS_ENABLED = false;
			VARIANTS = null;
		}
	}
	
	private Kleenean saddled = Kleenean.UNKNOWN;
	private @Nullable Object variant = null;

	public PigData() {}

	// TODO: When safe, 'variant' should have the type changed to 'Pig.Variant'
	public PigData(@Nullable Kleenean saddled, @Nullable Object variant) {
		this.saddled = saddled != null ? saddled : Kleenean.UNKNOWN;
		this.variant = variant;
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		saddled = PATTERNS.getInfo(matchedPattern);
		if (VARIANTS_ENABLED) {
			Literal<?> expr = null;
			if (exprs[0] != null) { // pig, saddled pig, unsaddled pig
				expr = exprs[0];
			} else if (exprs.length >= 2 && exprs[1] != null) { // piglet
				expr = exprs[1];
			}
			if (expr != null) {
				//noinspection unchecked
				variant = ((Literal<Pig.Variant>) expr).getSingle();
			}
		}
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Pig> entityClass, @Nullable Pig pig) {
		saddled = Kleenean.UNKNOWN;
		if (pig != null) {
			saddled = Kleenean.get(pig.hasSaddle());
			if (VARIANTS_ENABLED)
				variant = pig.getVariant();
		}
		return true;
	}
	
	@Override
	public void set(Pig pig) {
		pig.setSaddle(saddled.isTrue());
		if (VARIANTS_ENABLED) {
			Object finalVariant = variant != null ? variant : CollectionUtils.getRandom(VARIANTS);
			pig.setVariant((Pig.Variant) finalVariant);
		}
	}
	
	@Override
	protected boolean match(Pig pig) {
		if (!saddled.isUnknown() && saddled != Kleenean.get(pig.hasSaddle()))
			return false;
		return variant == null || variant == pig.getVariant();
	}
	
	@Override
	public Class<? extends Pig> getType() {
		return Pig.class;
	}
	
	@Override
	protected boolean equals_i(EntityData<?> obj) {
		if (!(obj instanceof PigData other))
			return false;
		if (saddled != other.saddled)
			return false;
		return variant == other.variant;
	}
	
	@Override
	protected int hashCode_i() {
		return saddled.ordinal() + Objects.hashCode(variant);
	}
	
	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof PigData other))
			return false;
		if (!saddled.isUnknown() && saddled != other.saddled)
			return false;
		return variant == null || variant == other.variant;
	}
	
	@Override
	public @NotNull EntityData<Pig> getSuperType() {
		return new PigData();
	}

	/**
	 * A dummy/placeholder class to ensure working operation on MC versions that do not have `Pig.Variant`
	 */
	public static class PigVariantDummy {}
	
}
