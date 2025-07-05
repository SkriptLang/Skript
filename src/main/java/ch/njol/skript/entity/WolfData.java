package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Color;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.collect.Iterators;
import org.bukkit.DyeColor;
import org.bukkit.entity.Wolf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class WolfData extends EntityData<Wolf> {

	public enum WolfState {
		WOLF(Kleenean.UNKNOWN, Kleenean.UNKNOWN),
		WILD(Kleenean.UNKNOWN, Kleenean.FALSE),
		TAMED(Kleenean.UNKNOWN, Kleenean.TRUE),
		ANGRY(Kleenean.TRUE, Kleenean.UNKNOWN),
		PEACEFUL(Kleenean.FALSE, Kleenean.UNKNOWN);

		private final Kleenean angry;
		private final Kleenean tamed;

		WolfState(Kleenean angry, Kleenean tamed) {
			this.angry = angry;
			this.tamed = tamed;
		}

		public static WolfState getWolfState(Kleenean angry, Kleenean tamed) {
			for (WolfState wolfState : values()) {
				if (wolfState.angry == angry && wolfState.tamed == tamed)
					return wolfState;
			}
			return null;
		}
	}

	private static final EntityPatterns<WolfState> PATTERNS = new EntityPatterns<>(new Object[][]{
		{"wolf", WolfState.WOLF},
		{"wild wolf", WolfState.WILD},
		{"tamed wolf", WolfState.TAMED},
		{"angry wolf", WolfState.ANGRY},
		{"peaceful wolf", WolfState.PEACEFUL}
	});

	private static final boolean VARIANTS_ENABLED;
	private static final Object[] VARIANTS;


	static {
		EntityData.register(WolfData.class, "wolf", Wolf.class, 0, PATTERNS.getPatterns());
		if (Skript.classExists("org.bukkit.entity.Wolf$Variant")) {
			VARIANTS_ENABLED = true;
			VARIANTS = Iterators.toArray(Classes.getExactClassInfo(Wolf.Variant.class).getSupplier().get(), Wolf.Variant.class);
		} else {
			VARIANTS_ENABLED = false;
			VARIANTS = null;
		}
	}

	private @Nullable Object variant = null;
	private @Nullable DyeColor collarColor = null;
	private Kleenean isAngry = Kleenean.UNKNOWN;
	private Kleenean isTamed = Kleenean.UNKNOWN;

	public WolfData() {}

	public WolfData(@Nullable Kleenean isAngry, @Nullable Kleenean isTamed) {
		this.isAngry = isAngry != null ? isAngry : Kleenean.UNKNOWN;
		this.isTamed = isTamed != null ? isTamed : Kleenean.UNKNOWN;
		super.dataCodeName = PATTERNS.getMatchedPatterns(WolfState.getWolfState(this.isAngry, this.isTamed))[0];
	}

	public WolfData(@Nullable WolfState wolfState) {
		if (wolfState != null) {
			this.isAngry = wolfState.angry;
			this.isTamed = wolfState.tamed;
			super.dataCodeName = PATTERNS.getMatchedPatterns(wolfState)[0];
		} else {
			this.isAngry = Kleenean.UNKNOWN;
			this.isTamed = Kleenean.UNKNOWN;
			super.dataCodeName = PATTERNS.getMatchedPatterns(WolfState.WOLF)[0];
		}
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		WolfState state = PATTERNS.getInfo(matchedCodeName);
		assert state != null;
		isAngry = state.angry;
		isTamed = state.tamed;
		if (exprs[0] != null && VARIANTS_ENABLED) {
			//noinspection unchecked
			variant = ((Literal<Wolf.Variant>) exprs[0]).getSingle();
		}
		if (exprs[1] != null) {
			//noinspection unchecked
			collarColor = ((Literal<Color>) exprs[1]).getSingle().asDyeColor();
		}
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Wolf> entityClass, @Nullable Wolf wolf) {
		if (wolf != null) {
			isAngry = Kleenean.get(wolf.isAngry());
			isTamed = Kleenean.get(wolf.isTamed());
			collarColor = wolf.getCollarColor();
			if (VARIANTS_ENABLED)
				variant = wolf.getVariant();
			super.dataCodeName = PATTERNS.getMatchedPatterns(WolfState.getWolfState(isAngry, isTamed))[0];
		}
		return true;
	}

	@Override
	public void set(Wolf wolf) {
		wolf.setAngry(isAngry.isTrue());
		wolf.setTamed(isTamed.isTrue());
		if (collarColor != null)
			wolf.setCollarColor(collarColor);
		if (VARIANTS_ENABLED) {
			Object variantSet = variant != null ? variant : CollectionUtils.getRandom(VARIANTS);
			assert variantSet != null;
			wolf.setVariant((Wolf.Variant) variantSet);
		}
	}

	@Override
	public boolean match(Wolf wolf) {
		if (!isAngry.isUnknown() && isAngry != Kleenean.get(wolf.isAngry()))
			return false;
		if (!isTamed.isUnknown() && isTamed != Kleenean.get(wolf.isTamed()))
			return false;
		if (collarColor != null && collarColor != wolf.getCollarColor())
			return false;
		return variant == null || variant == wolf.getVariant();
	}

	@Override
	public Class<Wolf> getType() {
		return Wolf.class;
	}

	@Override
	public @NotNull EntityData<Wolf> getSuperType() {
		return new WolfData();
	}

	@Override
	protected int hashCode_i() {
		int prime = 31, result = 1;
		result = prime * result + Objects.hashCode(isAngry);
		result = prime * result + Objects.hashCode(isTamed);;
		result = prime * result + (collarColor == null ? 0 : collarColor.hashCode());
		if (VARIANTS_ENABLED)
			result = prime * result + (variant == null ? 0 : Objects.hashCode(variant));
		return result;
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof WolfData other))
			return false;
		if (isAngry != other.isAngry)
			return false;
		if (isTamed != other.isTamed)
			return false;
		if (collarColor != other.collarColor)
			return false;
		return variant == other.variant;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof WolfData other))
			return false;
		if (!isAngry.isUnknown() && isAngry != other.isAngry)
			return false;
		if (!isTamed.isUnknown() && isTamed != other.isTamed)
			return false;
		if (collarColor != null && collarColor != other.collarColor)
			return false;
		return variant == null || variant == other.variant;
	}

	/**
	 * A dummy/placeholder class to ensure working operation on MC versions that do not have `Wolf.Variant`
	 */
	public static class WolfVariantDummy {};

}
