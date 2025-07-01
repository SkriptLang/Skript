package ch.njol.skript.entity;

import org.bukkit.entity.Creeper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;

import java.util.Objects;


public class CreeperData extends EntityData<Creeper> {

	public enum PoweredState {
		UNKNOWN, POWERED, UNPOWERED
	}

	private static final EntityPatterns<PoweredState> PATTERNS = new EntityPatterns<>(new Object[][]{
		{"creeper", PoweredState.UNKNOWN},
		{"powered creeper", PoweredState.POWERED},
		{"unpowered creeper", PoweredState.UNPOWERED}
	});

	static {
		EntityData.register(CreeperData.class, "creeper", Creeper.class, 0, PATTERNS.getPatterns());
	}
	
	private PoweredState state = PoweredState.UNKNOWN;

	public CreeperData() {}

	public CreeperData(@Nullable CreeperData.PoweredState state)  {
		this.state = state != null ? state : PoweredState.UNKNOWN;
		super.dataCodeName = PATTERNS.getMatchedPatterns(this.state)[0];
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		state = PATTERNS.getInfo(matchedCodeName);
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Creeper> entityClass, @Nullable Creeper creeper) {
		state = PoweredState.UNKNOWN;
		if (creeper != null) {
			state = creeper.isPowered() ? PoweredState.POWERED : PoweredState.UNPOWERED;
			super.dataCodeName = PATTERNS.getMatchedPatterns(state)[0];
		}
		return true;
	}
	
	@Override
	public void set(Creeper creeper) {
		creeper.setPowered(state == PoweredState.POWERED);
	}
	
	@Override
	public boolean match(Creeper creeper) {
		return state == PoweredState.UNKNOWN || creeper.isPowered() == (state == PoweredState.POWERED);
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
		return Objects.hashCode(state);
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof CreeperData other))
			return false;
		return state == other.state;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof CreeperData other))
			return false;
		return state == other.state;
	}

}
