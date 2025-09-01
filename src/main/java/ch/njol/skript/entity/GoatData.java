package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Goat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GoatData extends EntityData<Goat> {

	private static final Patterns<Kleenean> PATTERNS = new Patterns<>(new Object[][]{
		{"goat", Kleenean.UNKNOWN},
		{"screaming goat", Kleenean.TRUE},
		{"quiet goat", Kleenean.FALSE}
	});

	static {
		EntityData.register(GoatData.class, "goat", Goat.class, 0, PATTERNS.getPatterns());
	}

	private Kleenean screaming = Kleenean.UNKNOWN;

	public GoatData() {}

	public GoatData(@Nullable Kleenean screaming) {
		this.screaming = screaming != null ? screaming : Kleenean.UNKNOWN;
		super.codeNameIndex = PATTERNS.getMatchedPattern(this.screaming, 0).orElseThrow();
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		screaming = PATTERNS.getInfo(matchedCodeName);
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Goat> entityClass, @Nullable Goat goat) {
		if (goat != null) {
			screaming = Kleenean.get(goat.isScreaming());
			super.codeNameIndex = PATTERNS.getMatchedPattern(screaming, 0).orElseThrow();
		}
		return true;
	}

	@Override
	public void set(Goat goat) {
		goat.setScreaming(screaming.isTrue());
	}

	@Override
	protected boolean match(Goat goat) {
		return kleeneanMatch(screaming, goat.isScreaming());
	}

	@Override
	public Class<? extends Goat> getType() {
		return Goat.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new GoatData();
	}

	@Override
	protected int hashCode_i() {
		return screaming.hashCode();
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof GoatData other))
			return false;
		return screaming == other.screaming;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof GoatData other))
			return false;
		return kleeneanMatch(screaming, other.screaming);
	}

}
