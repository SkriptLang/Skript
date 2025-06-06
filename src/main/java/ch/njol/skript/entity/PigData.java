package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.collect.Iterators;
import org.bukkit.entity.Pig;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PigData extends EntityData<Pig> {

	public enum SaddleState {
		NOT_SADDLED, UNKNOWN, SADDLED
	}

	private static final SaddleState[] SADDLE_STATES = SaddleState.values();
	private static boolean variantsEnabled = false;
	private static Object[] variants;

	static {
		EntityData.register(PigData.class, "pig", Pig.class, 1, "unsaddled pig", "pig", "saddled pig");
		if (Skript.classExists("org.bukkit.entity.Pig$Variant")) {
			variantsEnabled = true;
			variants = Iterators.toArray(Classes.getExactClassInfo(Pig.Variant.class).getSupplier().get(), Pig.Variant.class);
		}
	}
	
	private SaddleState saddled = SaddleState.UNKNOWN;
	private @Nullable Object variant = null;

	public PigData() {}

	// TODO: When safe, 'variant' should have the type changed to 'Pig.Variant'
	public PigData(SaddleState saddled, @Nullable Object variant) {
		this.saddled = saddled;
		this.variant = variant;
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		saddled = SADDLE_STATES[matchedPattern];
		if (exprs[0] != null && variantsEnabled)
			//noinspection unchecked
			variant = ((Literal<Pig.Variant>) exprs[0]).getSingle();
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Pig> entityClass, @Nullable Pig pig) {
		saddled = SaddleState.UNKNOWN;
		if (pig != null) {
			saddled = pig.hasSaddle() ? SaddleState.SADDLED : SaddleState.NOT_SADDLED;
			if (variantsEnabled)
				variant = pig.getVariant();
		}
		return true;
	}

	@Override
	protected boolean deserialize(String string) {
		return true;
	}
	
	@Override
	public void set(Pig pig) {
		pig.setSaddle(saddled == SaddleState.SADDLED);
		if (variantsEnabled) {
			Object finalVariant = variant != null ? variant : CollectionUtils.getRandom(variants);
			pig.setVariant((Pig.Variant) finalVariant);
		}
	}
	
	@Override
	protected boolean match(Pig pig) {
		return (saddled == SaddleState.UNKNOWN || (pig.hasSaddle() ? SaddleState.SADDLED : SaddleState.NOT_SADDLED) == saddled)
			&& (variant == null || variant == pig.getVariant());
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
		return variant == null || variant == other.variant;
	}
	
	@Override
	protected int hashCode_i() {
		return saddled.ordinal() + Objects.hashCode(variant);
	}
	
	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof PigData other))
			return false;
		if (saddled != SaddleState.UNKNOWN && saddled != other.saddled)
			return false;
		return variant == null || variant == other.variant;
	}
	
	@Override
	public EntityData<Pig> getSuperType() {
		return new PigData();
	}

	/**
	 * A dummy/placeholder class to ensure working operation on MC versions that do not have `Pig.Variant`
	 */
	public static class PigVariantDummy {}
	
}
