package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Strider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class StriderData extends EntityData<Strider> {

	private static final EntityPatterns<Kleenean> PATTERNS = new EntityPatterns<>(new Object[][]{
		{"strider", Kleenean.UNKNOWN},
		{"warm strider", Kleenean.FALSE},
		{"shivering strider", Kleenean.TRUE}
	});

	static {
		register(StriderData.class, "strider", Strider.class, 0, PATTERNS.getPatterns());
	}

	private Kleenean shivering = Kleenean.UNKNOWN;

	public StriderData() {}

	public StriderData(@Nullable Kleenean shivering) {
		this.shivering = shivering != null ? shivering : Kleenean.UNKNOWN;
		super.dataCodeName = PATTERNS.getMatchedPatterns(shivering)[0];
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		shivering = PATTERNS.getInfo(matchedCodeName);
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Strider> entityClass, @Nullable Strider strider) {
		if (strider != null) {
			shivering = Kleenean.get(strider.isShivering());
			super.dataCodeName = PATTERNS.getMatchedPatterns(shivering)[0];
		}
		return true;
	}

	@Override
	public void set(Strider entity) {
		entity.setShivering(shivering.isTrue());
	}

	@Override
	protected boolean match(Strider strider) {
		return shivering.isUnknown() || shivering == Kleenean.get(strider.isShivering());
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
		return Objects.hash(shivering);
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof StriderData other))
			return false;
		return shivering == other.shivering;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof StriderData other))
			return false;
		return shivering.isUnknown() || shivering == other.shivering;
	}

}
