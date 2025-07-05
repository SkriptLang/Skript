package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Frog.Variant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class FrogData extends EntityData<Frog> {

	private static final EntityPatterns<Variant> PATTERNS = new EntityPatterns<>(new Object[][]{
		{"frog", null},
		{"temperate frog", Variant.TEMPERATE},
		{"warm frog", Variant.WARM},
		{"cold frog", Variant.COLD}
	});

	private static final Variant[] VARIANTS;

	static {
		EntityData.register(FrogData.class, "frog", Frog.class, 0, PATTERNS.getPatterns());
		VARIANTS = List.of(Variant.TEMPERATE, Variant.WARM, Variant.COLD).toArray(Variant[]::new);
	}

	private @Nullable Variant variant = null;

	public FrogData() {}

	public FrogData(@Nullable Variant variant) {
		this.variant = variant;
		super.dataCodeName = PATTERNS.getMatchedPatterns(variant)[0];
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		variant = PATTERNS.getInfo(matchedCodeName);
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Frog> entityClass, @Nullable Frog frog) {
		if (frog != null) {
			variant = frog.getVariant();
			super.dataCodeName = PATTERNS.getMatchedPatterns(variant)[0];
		}
		return true;
	}

	@Override
	public void set(Frog frog) {
		Variant variant = this.variant;
		if (variant == null)
			variant = CollectionUtils.getRandom(VARIANTS);
		assert variant != null;
		frog.setVariant(variant);
	}

	@Override
	protected boolean match(Frog frog) {
		return variant == null || variant == frog.getVariant();
	}

	@Override
	public Class<? extends Frog> getType() {
		return Frog.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new FrogData();
	}

	@Override
	protected int hashCode_i() {
		return Objects.hashCode(variant);
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof FrogData other))
			return false;
		return variant == other.variant;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof FrogData other))
			return false;
		return variant == null || variant == other.variant;
	}

}
