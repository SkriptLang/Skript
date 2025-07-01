package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.entity.Strider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class StriderData extends EntityData<Strider> {

	public enum ShiveringState {
		UNKNOWN, WARM, COLD
	}

	private static final EntityPatterns<ShiveringState> PATTERNS = new EntityPatterns<>(new Object[][]{
		{"strider", ShiveringState.UNKNOWN},
		{"warm strider", ShiveringState.WARM},
		{"shivering strider", ShiveringState.COLD}
	});

	static {
		register(StriderData.class, "strider", Strider.class, 0, PATTERNS.getPatterns());
	}

	private ShiveringState state = ShiveringState.UNKNOWN;

	public StriderData() {}

	public StriderData(@Nullable ShiveringState state) {
		this.state = state != null ? state : ShiveringState.UNKNOWN;
		super.dataCodeName = PATTERNS.getMatchedPatterns(state)[0];
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		state = PATTERNS.getInfo(matchedCodeName);
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Strider> entityClass, @Nullable Strider entity) {
		state = ShiveringState.UNKNOWN;
		if (entity != null) {
			state = entity.isShivering() ? ShiveringState.COLD : ShiveringState.WARM;
			super.dataCodeName = PATTERNS.getMatchedPatterns(state)[0];
		}
		return true;
	}

	@Override
	public void set(Strider entity) {
		entity.setShivering(state == ShiveringState.COLD);
	}

	@Override
	protected boolean match(Strider entity) {
		return state == ShiveringState.UNKNOWN || state == (entity.isShivering() ? ShiveringState.COLD : ShiveringState.WARM);
	}

	@Override
	public Class<? extends Strider> getType() {
		return Strider.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new StriderData();
	}

	@Override
	protected int hashCode_i() {
		return Objects.hash(state);
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof StriderData other))
			return false;
		return state == other.state;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof StriderData other))
			return false;
		return state == ShiveringState.UNKNOWN || state == other.state;
	}

}
