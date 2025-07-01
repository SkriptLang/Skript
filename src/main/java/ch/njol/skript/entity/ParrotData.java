package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Parrot.Variant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParrotData extends EntityData<Parrot> {

	private static final Variant[] VARIANTS = Variant.values();
	private static final EntityPatterns<Variant> PATTERNS = new EntityPatterns<>(new Object[][]{
		{"parrot", null},
		{"red parrot", Variant.RED},
		{"blue parrot", Variant.BLUE},
		{"green parrot", Variant.GREEN},
		{"cyan parrot", Variant.CYAN},
		{"gray parrot", Variant.GRAY}
	});
	
	static {
		EntityData.register(ParrotData.class, "parrot", Parrot.class, 0,
			PATTERNS.getPatterns());
	}

	private @Nullable Variant variant = null;
	
	public ParrotData() {}
	
	public ParrotData(@Nullable Variant variant) {
		this.variant = variant;
		super.dataCodeName = PATTERNS.getMatchedPatterns(variant)[0];
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		variant = PATTERNS.getInfo(matchedCodeName);
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Parrot> entityClass, @Nullable Parrot parrot) {
		if (parrot != null) {
			variant = parrot.getVariant();
			super.dataCodeName = PATTERNS.getMatchedPatterns(variant)[0];
		}
		return true;
	}

	@Override
	public void set(Parrot parrot) {
		Variant variant = this.variant;
		if (variant == null)
			variant = CollectionUtils.getRandom(VARIANTS);
		assert variant != null;
		parrot.setVariant(variant);
	}

	@Override
	protected boolean match(Parrot parrot) {
		return variant == null || variant == parrot.getVariant();
	}

	@Override
	public Class<? extends Parrot> getType() {
		return Parrot.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new ParrotData();
	}

	@Override
	protected int hashCode_i() {
		return variant == null ? 0 : variant.hashCode();
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof ParrotData other))
			return false;
		return variant == other.variant;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof ParrotData other))
			return false;
		return variant == null || variant == other.variant;
	}
	
}
