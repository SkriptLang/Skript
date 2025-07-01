package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.entity.Goat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class GoatData extends EntityData<Goat> {

	public enum ScreamingState {
		UNKNOWN, SCREAMING, QUIET
	}

	private static final EntityPatterns<ScreamingState> PATTERNS = new EntityPatterns<>(new Object[][]{
		{"goat", ScreamingState.UNKNOWN},
		{"screaming goat", ScreamingState.SCREAMING},
		{"quiet goat", ScreamingState.QUIET}
	});

	static {
		EntityData.register(GoatData.class, "goat", Goat.class, 0, PATTERNS.getPatterns());
	}

	private ScreamingState state = ScreamingState.UNKNOWN;

	public GoatData() {}

	public GoatData(@Nullable GoatData.ScreamingState state) {
		this.state = state != null ? state : ScreamingState.UNKNOWN;
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		state = PATTERNS.getInfo(matchedCodeName);
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Goat> entityClass, @Nullable Goat goat) {
		state = ScreamingState.UNKNOWN;
		if (goat != null) {
			state = goat.isScreaming() ? ScreamingState.SCREAMING : ScreamingState.QUIET;
			super.dataCodeName = PATTERNS.getMatchedPatterns(state)[0];
		}
		return true;
	}

	@Override
	public void set(Goat goat) {
		goat.setScreaming(state == ScreamingState.SCREAMING);
	}

	@Override
	protected boolean match(Goat goat) {
		return state == ScreamingState.UNKNOWN || goat.isScreaming() == (state == ScreamingState.SCREAMING);
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
		return Objects.hashCode(state);
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof GoatData other))
			return false;
		return state == other.state;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof GoatData other))
			return false;
		return state == ScreamingState.UNKNOWN || state == other.state;
	}

}
