package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Axolotl.Variant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class AxolotlData extends EntityData<Axolotl> {

	private static final EntityPatterns<Variant> PATTERNS = new EntityPatterns<>(new Object[][]{
		{"axolotl", null},
		{"lucy axolotl", Variant.LUCY},
		{"wild axolotl", Variant.WILD},
		{"gold axolotl", Variant.GOLD},
		{"cyan axolotl", Variant.CYAN},
		{"blue axolotl", Variant.BLUE}
	});
	private static final Variant[] VARIANTS = Variant.values();

	static {
		EntityData.register(AxolotlData.class, "axolotl", Axolotl.class, 0, PATTERNS.getPatterns());
	}

	private @Nullable Variant variant = null;

	public AxolotlData() {}

	public AxolotlData(@Nullable Variant variant) {
		this.variant = variant;
		super.dataCodeName = PATTERNS.getMatchedPatterns(variant)[0];
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		variant = PATTERNS.getInfo(matchedCodeName);
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Axolotl> entityClass, @Nullable Axolotl axolotl) {
		if (axolotl != null) {
			variant = axolotl.getVariant();
			super.dataCodeName = PATTERNS.getMatchedPatterns(variant)[0];
		}
		return true;
	}

	@Override
	public void set(Axolotl axolotl) {
		Variant variant = this.variant;
		if (variant == null)
			variant = CollectionUtils.getRandom(VARIANTS);
		assert variant != null;
		axolotl.setVariant(variant);
	}

	@Override
	protected boolean match(Axolotl axolotl) {
		return variant == null || variant == axolotl.getVariant();
	}

	@Override
	public Class<? extends Axolotl> getType() {
		return Axolotl.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new AxolotlData();
	}

	@Override
	protected int hashCode_i() {
		return variant == null ? 0 : Objects.hashCode(variant);
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof AxolotlData other))
			return false;
		return variant == other.variant;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof AxolotlData other))
			return false;
		return variant == null || variant == other.variant;
	}

}
