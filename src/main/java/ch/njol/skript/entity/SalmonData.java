package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Salmon;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SalmonData extends EntityData<Salmon> {

	private static final boolean SUPPORT_SALMON_VARIANTS = Skript.classExists("org.bukkit.entity.Salmon$Variant");
	private static Object[] variants;

	static {
		List<String> patternList = new ArrayList<>();
		patternList.add("salmon");
		if (SUPPORT_SALMON_VARIANTS) {
			variants = Salmon.Variant.values();
			patternList.add("any salmon");
			for (Object object : variants) {
				Salmon.Variant variant = (Salmon.Variant) object;
				patternList.add(variant.toString().replace("_", " ").toLowerCase(Locale.ENGLISH) + " salmon");
			}
		}

		EntityData.register(SalmonData.class, "salmon", Salmon.class, 0, patternList.toArray(String[]::new));
	}

	private @Nullable Object variant;

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		if (matchedPattern > 1)
			variant = variants[matchedPattern - 2];
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Salmon> c, @Nullable Salmon salmon) {
		if (salmon != null && SUPPORT_SALMON_VARIANTS)
			variant = salmon.getVariant();
		return true;
	}

	@Override
	public void set(Salmon entity) {
		if (SUPPORT_SALMON_VARIANTS) {
			Object variantSet = variant != null ? variant : CollectionUtils.getRandom(variants);
			entity.setVariant((Salmon.Variant) variantSet);
		}
	}

	@Override
	protected boolean match(Salmon entity) {
		return matchedPattern <= 1 || variant == entity.getVariant();
	}

	@Override
	public Class<? extends Salmon> getType() {
		return Salmon.class;
	}

	@Override
	public EntityData getSuperType() {
		return new SalmonData();
	}

	@Override
	protected int hashCode_i() {
		return matchedPattern <= 1 ? 0 : matchedPattern;
	}

	@Override
	protected boolean equals_i(EntityData<?> obj) {
		if (!(obj instanceof SalmonData salmonData))
			return false;
		if (matchedPattern > 1 && variant != salmonData.variant)
			return false;
		return true;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entity) {
		if (entity instanceof SalmonData salmonData)
			return matchedPattern <= 1 || variant == salmonData.variant;
		return false;
	}

}
