package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.BukkitUtils;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.collect.Iterators;
import org.bukkit.DyeColor;
import org.bukkit.entity.Wolf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class WolfData extends EntityData<Wolf> {

	public record WolfStates(Kleenean angry, Kleenean tamed) {}

	private static final Patterns<WolfStates> PATTERNS = new Patterns<>(new Object[][]{
		{"wolf", new WolfStates(Kleenean.UNKNOWN, Kleenean.UNKNOWN)},
		{"wild wolf", new WolfStates(Kleenean.UNKNOWN, Kleenean.FALSE)},
		{"tamed wolf", new WolfStates(Kleenean.UNKNOWN, Kleenean.TRUE)},
		{"angry wolf", new WolfStates(Kleenean.TRUE, Kleenean.UNKNOWN)},
		{"peaceful wolf", new WolfStates(Kleenean.FALSE, Kleenean.UNKNOWN)}
	});

	private static final boolean VARIANTS_ENABLED;
	private static final Object[] VARIANTS;

	static {
		ClassInfo<?> wolfVariantClassInfo = BukkitUtils.getRegistryClassInfo(
			"org.bukkit.entity.Wolf$Variant",
			"WOLF_VARIANT",
			"wolfvariant",
			"wolf variants"
		);
		if (wolfVariantClassInfo == null) {
			// Registers a dummy/placeholder class to ensure working operation on MC versions that do not have 'Wolf.Variant' (1.20.4-)
			wolfVariantClassInfo = new ClassInfo<>(WolfVariantDummy.class, "wolfvariant");
		}
		Classes.registerClass(wolfVariantClassInfo
			.user("wolf ?variants?")
			.name("Wolf Variant")
			.description("Represents the variant of a wolf entity.",
				"NOTE: Minecraft namespaces are supported, ex: 'minecraft:ashen'.")
			.since("2.10")
			.requiredPlugins("Minecraft 1.21+")
			.documentationId("WolfVariant"));

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
		super.codeNameIndex = PATTERNS.getMatchedPattern(new WolfStates(this.isAngry, this.isTamed), 0).orElseThrow();
	}

	public WolfData(@Nullable WolfStates wolfState) {
		if (wolfState != null) {
			this.isAngry = wolfState.angry;
			this.isTamed = wolfState.tamed;
			super.codeNameIndex = PATTERNS.getMatchedPattern(wolfState, 0).orElse(0);
		} else {
			this.isAngry = Kleenean.UNKNOWN;
			this.isTamed = Kleenean.UNKNOWN;
			super.codeNameIndex = PATTERNS.getMatchedPattern(new WolfStates(Kleenean.UNKNOWN, Kleenean.UNKNOWN), 0).orElse(0);
		}
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		WolfStates state = PATTERNS.getInfo(matchedCodeName);
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
			super.codeNameIndex = PATTERNS.getMatchedPattern(new WolfStates(isAngry, isTamed), 0).orElse(0);
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
		if (!kleeneanMatch(isAngry, wolf.isAngry()))
			return false;
		if (!kleeneanMatch(isTamed, wolf.isTamed()))
			return false;
		if (!dataMatch(collarColor, wolf.getCollarColor()))
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
		result = prime * result + isAngry.hashCode();
		result = prime * result + isTamed.hashCode();
		result = prime * result + Objects.hashCode(collarColor);
		if (VARIANTS_ENABLED)
			result = prime * result + Objects.hashCode(variant);
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
		if (!kleeneanMatch(isAngry, other.isAngry))
			return false;
		if (!kleeneanMatch(isTamed, other.isTamed))
			return false;
		if (!dataMatch(collarColor, other.collarColor))
			return false;
		return dataMatch(variant, other.variant);
	}

	/**
	 * A dummy/placeholder class to ensure working operation on MC versions that do not have `Wolf.Variant`
	 */
	public static class WolfVariantDummy {};

}
