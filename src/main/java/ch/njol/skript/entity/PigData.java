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

	private static boolean variantsEnabled = false;
	private static Object[] variants;

	static {
		EntityData.register(PigData.class, "pig", Pig.class, 1, "unsaddled pig", "pig", "saddled pig");
		if (Skript.classExists("org.bukkit.entity.Pig$Variant")) {
			variantsEnabled = true;
			variants = Iterators.toArray(Classes.getExactClassInfo(Pig.Variant.class).getSupplier().get(), Pig.Variant.class);
		}
	}
	
	private int saddled = 0;
	private @Nullable Object variant;

	public PigData() {}

	public PigData(int saddled, @Nullable Object variant) {
		this.saddled = saddled;
		this.variant = variant;
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		saddled = matchedPattern - 1;
		if (exprs[0] != null && variantsEnabled)
			//noinspection unchecked
			variant = ((Literal<Pig.Variant>) exprs[0]).getSingle();
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Pig> entityClass, @Nullable Pig pig) {
		saddled = 0;
		if (pig != null) {
			saddled = pig.hasSaddle() ? 1 : -1;
			if (variantsEnabled)
				variant = pig.getVariant();
		}
		return true;
	}

	@Override
	protected boolean deserialize(String string) {
		try {
			saddled = Integer.parseInt(string);
			return Math.abs(saddled) <= 1;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	@Override
	public void set(Pig pig) {
		if (saddled != 0)
			pig.setSaddle(saddled == 1);
		Object finalVariant = null;
		if (variantsEnabled) {
			finalVariant = variant != null ? variant : CollectionUtils.getRandom(variants);
			pig.setVariant((Pig.Variant) finalVariant);
		}
	}
	
	@Override
	protected boolean match(Pig pig) {
		return (saddled == 0 || pig.hasSaddle() == (saddled == 1))
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
		return saddled + Objects.hashCode(variant);
	}
	
	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof PigData other))
			return false;
		if (saddled != 0 && saddled != other.saddled)
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
