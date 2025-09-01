package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Bee;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class BeeData extends EntityData<Bee> {

	public record BeeState(Kleenean angry, Kleenean nectar) {}

	private static final Patterns<BeeState> PATTERNS = new Patterns<>(new Object[][]{
		{"bee", new BeeState(Kleenean.UNKNOWN, Kleenean.UNKNOWN)},
		{"no nectar bee", new BeeState(Kleenean.UNKNOWN, Kleenean.FALSE)},
		{"nectar bee", new BeeState(Kleenean.UNKNOWN, Kleenean.TRUE)},
		{"happy bee", new BeeState(Kleenean.FALSE, Kleenean.UNKNOWN)},
		{"happy nectar bee", new BeeState(Kleenean.FALSE, Kleenean.TRUE)},
		{"happy no nectar bee", new BeeState(Kleenean.FALSE, Kleenean.FALSE)},
		{"angry bee", new BeeState(Kleenean.TRUE, Kleenean.UNKNOWN)},
		{"angry no nectar bee", new BeeState(Kleenean.TRUE, Kleenean.FALSE)},
		{"angry nectar bee", new BeeState(Kleenean.TRUE, Kleenean.TRUE)}
	});

	static {
		EntityData.register(BeeData.class, "bee", Bee.class, 0, PATTERNS.getPatterns());
	}

	private Kleenean hasNectar = Kleenean.UNKNOWN;
	private Kleenean isAngry = Kleenean.UNKNOWN;

	public BeeData() {}

	public BeeData(@Nullable Kleenean isAngry, @Nullable Kleenean hasNectar) {
		this.isAngry = isAngry != null ? isAngry : Kleenean.UNKNOWN;
		this.hasNectar = hasNectar != null ? hasNectar : Kleenean.UNKNOWN;
		super.codeNameIndex = PATTERNS.getMatchedPattern(new BeeState(this.isAngry, this.hasNectar), 0).orElseThrow();
	}

	public BeeData(@Nullable BeeState beeState) {
		if (beeState != null) {
			this.isAngry = beeState.angry;
			this.hasNectar = beeState.nectar;
			super.codeNameIndex = PATTERNS.getMatchedPattern(beeState, 0).orElse(0);
		} else {
			this.isAngry = Kleenean.UNKNOWN;
			this.hasNectar = Kleenean.UNKNOWN;
			super.codeNameIndex = PATTERNS.getMatchedPattern(new BeeState(Kleenean.UNKNOWN, Kleenean.UNKNOWN), 0).orElseThrow();
		}
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		BeeState state = PATTERNS.getInfo(matchedCodeName);
		assert state != null;
		hasNectar = state.nectar;
		isAngry = state.angry;
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Bee> entityClass, @Nullable Bee bee) {
		if (bee != null) {
			isAngry = Kleenean.get(bee.getAnger() > 0);
			hasNectar = Kleenean.get(bee.hasNectar());
			super.codeNameIndex = PATTERNS.getMatchedPattern(new BeeState(isAngry, hasNectar), 0).orElse(0);
		}
		return true;
	}
	
	@Override
	public void set(Bee bee) {
		int anger = 0;
		if (isAngry.isTrue())
			anger = new Random().nextInt(400) + 400;
		bee.setAnger(anger);
		bee.setHasNectar(hasNectar.isTrue());
	}
	
	@Override
	protected boolean match(Bee bee) {
		if (!kleeneanMatch(isAngry, bee.getAnger() > 0))
			return false;
		return kleeneanMatch(hasNectar, bee.hasNectar());
	}
	
	@Override
	public Class<? extends Bee> getType() {
		return Bee.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new BeeData();
	}

	@Override
	protected int hashCode_i() {
		int prime = 31;
		int result = 1;
		result = prime * result + isAngry.hashCode();
		result = prime * result + hasNectar.hashCode();
		return result;
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof BeeData other))
			return false;
		return isAngry == other.isAngry && hasNectar == other.hasNectar;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof BeeData other))
			return false;
		if (!kleeneanMatch(isAngry, other.isAngry))
			return false;
		return kleeneanMatch(hasNectar, other.hasNectar);
	}

}
