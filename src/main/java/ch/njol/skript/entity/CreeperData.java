package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Creeper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CreeperData extends EntityData<Creeper> {

	private static final Patterns<Kleenean> PATTERNS = new Patterns<>(new Object[][]{
		{"creeper", Kleenean.UNKNOWN},
		{"powered creeper", Kleenean.TRUE},
		{"unpowered creeper", Kleenean.FALSE}
	});

	static {
		EntityData.register(CreeperData.class, "creeper", Creeper.class, 0, PATTERNS.getPatterns());
	}
	
	private Kleenean powered = Kleenean.UNKNOWN;

	public CreeperData() {}

	public CreeperData(@Nullable Kleenean powered)  {
		this.powered = powered != null ? powered : Kleenean.UNKNOWN;
		super.codeNameIndex = PATTERNS.getMatchedPattern(this.powered, 0).orElseThrow();
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		powered = PATTERNS.getInfo(matchedCodeName);
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Creeper> entityClass, @Nullable Creeper creeper) {
		if (creeper != null) {
			powered = Kleenean.get(creeper.isPowered());
			super.codeNameIndex = PATTERNS.getMatchedPattern(powered, 0).orElseThrow();
		}
		return true;
	}
	
	@Override
	public void set(Creeper creeper) {
		creeper.setPowered(powered.isTrue());
	}
	
	@Override
	public boolean match(Creeper creeper) {
		return kleeneanMatch(powered, creeper.isPowered());
	}
	
	@Override
	public Class<Creeper> getType() {
		return Creeper.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new CreeperData();
	}

	@Override
	protected int hashCode_i() {
		return powered.hashCode();
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof CreeperData other))
			return false;
		return powered == other.powered;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof CreeperData other))
			return false;
		return kleeneanMatch(powered, other.powered);
	}

}
