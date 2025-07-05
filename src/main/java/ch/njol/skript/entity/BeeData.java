package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Bee;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Random;

public class BeeData extends EntityData<Bee> {

	public enum BeeState {
		BEE(Kleenean.UNKNOWN, Kleenean.UNKNOWN),
		NO_NECTAR(Kleenean.UNKNOWN, Kleenean.FALSE),
		NECTAR(Kleenean.UNKNOWN, Kleenean.TRUE),
		HAPPY(Kleenean.FALSE, Kleenean.UNKNOWN),
		HAPPY_NECTAR(Kleenean.FALSE, Kleenean.TRUE),
		HAPPY_NO_NECTAR(Kleenean.FALSE, Kleenean.FALSE),
		ANGRY(Kleenean.TRUE, Kleenean.UNKNOWN),
		ANGRY_NO_NECTAR(Kleenean.TRUE, Kleenean.FALSE),
		ANGRY_NECTAR(Kleenean.TRUE, Kleenean.TRUE);

		private final Kleenean angry;
		private final Kleenean nectar;

		BeeState(Kleenean angry, Kleenean nectar) {
			this.angry = angry;
			this.nectar = nectar;
		}

		public static BeeState getBeeState(Kleenean angry, Kleenean nectar) {
			for (BeeState beeState : BeeState.values()) {
				if (beeState.angry == angry && beeState.nectar == nectar)
					return beeState;
			}
			return null;
		}

	}

	private static final EntityPatterns<BeeState> PATTERNS = new EntityPatterns<>(new Object[][]{
		{"bee", BeeState.BEE},
		{"no nectar bee", BeeState.NO_NECTAR},
		{"nectar bee", BeeState.NECTAR},
		{"happy bee", BeeState.HAPPY},
		{"happy nectar bee", BeeState.HAPPY_NECTAR},
		{"happy no nectar bee", BeeState.HAPPY_NO_NECTAR},
		{"angry bee", BeeState.ANGRY},
		{"angry no nectar bee", BeeState.ANGRY_NO_NECTAR},
		{"angry nectar bee", BeeState.ANGRY_NECTAR}
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
		super.dataCodeName = PATTERNS.getMatchedPatterns(BeeState.getBeeState(this.isAngry, this.hasNectar))[0];
	}

	public BeeData(@Nullable BeeState beeState) {
		if (beeState != null) {
			this.isAngry = beeState.angry;
			this.hasNectar = beeState.nectar;
			super.dataCodeName = PATTERNS.getMatchedPatterns(beeState)[0];
		} else {
			this.isAngry = Kleenean.UNKNOWN;
			this.hasNectar = Kleenean.UNKNOWN;
			super.dataCodeName = PATTERNS.getMatchedPatterns(BeeState.BEE)[0];
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
			super.dataCodeName = PATTERNS.getMatchedPatterns(BeeState.getBeeState(isAngry, hasNectar))[0];
		}
		return true;
	}
	
	@Override
	public void set(Bee bee) {
		int random = new Random().nextInt(400) + 400;
		bee.setAnger(isAngry.isTrue() ? random : 0);
		bee.setHasNectar(hasNectar.isTrue());
	}
	
	@Override
	protected boolean match(Bee bee) {
		if (!isAngry.isUnknown() && isAngry != Kleenean.get(bee.getAnger() > 0))
			return false;
		return hasNectar.isUnknown() || hasNectar == Kleenean.get(bee.hasNectar());
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
		result = prime * result + Objects.hashCode(isAngry);
		result = prime * result + Objects.hashCode(hasNectar);
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
		if (!isAngry.isUnknown() && isAngry != other.isAngry)
			return false;
		return hasNectar.isUnknown() || hasNectar == other.hasNectar;
	}

}
