package org.skriptlang.skript.bukkit.entity.data;

import ch.njol.skript.bukkitutil.BukkitUtils;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Color;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.collect.Iterators;
import org.bukkit.DyeColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Wolf.Variant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.EntityData;

import java.util.Objects;

public class WolfData extends EntityData<Wolf> {

	public record WolfStates(Kleenean angry, Kleenean tamed) {}

	private static final EntityDataPatterns<WolfStates> GROUPS = new EntityDataPatterns<>(
		new PatternGroup<>(0, "wol¦f¦ves @a", new WolfStates(Kleenean.UNKNOWN, Kleenean.UNKNOWN),
			"<age> [%-wolfvariant%] wol(f|plural:ves) [[with collar] colo[u]r[ed] %-color%]"),
		new PatternGroup<>(1, "wild wol¦f¦ves @a", new WolfStates(Kleenean.UNKNOWN, Kleenean.FALSE),
			"(wild|untamed) <age> [%-wolfvariant%] wol(f|plural:ves) [[with collar] colo[u]r[ed] %-color%]"),
		new PatternGroup<>(2, "tamed wol¦f¦ves @a", new WolfStates(Kleenean.UNKNOWN, Kleenean.TRUE),
			"<age> [%-wolfvariant%] dog[plural:s] [[with collar] colo[u]r[ed] %-color%]",
			"tamed <age> [%-wolfvariant%] wol(f|plural:ves) [[with collar] colo[u]r[ed] %-color%]",
			"baby:[%-wolfvariant%] [wolf] pup[py|plural:pies] [[with collar] colo[u]r[ed] %-color%]"),
		new PatternGroup<>(3, "angry wol¦f¦ves @an", new WolfStates(Kleenean.TRUE, Kleenean.UNKNOWN),
			"(angry|aggressive) <age> [%-wolfvariant%] wol(f|plural:ves) [[with collar] colo[u]r[ed] %-color%]"),
		new PatternGroup<>(4, "peaceful wol¦f¦ves @a", new WolfStates(Kleenean.FALSE, Kleenean.UNKNOWN),
			"(peaceful|neutral|unaggressive) <age> [%-wolfvariant%] wol(f|plural:ves) [[with collar] colo[u]r[ed] %-color%]")
	);

	private static Variant[] VARIANTS;

	public static void register() {
		ClassInfo<?> wolfVariantClassInfo = BukkitUtils.getRegistryClassInfo(
			"org.bukkit.entity.Wolf$Variant",
			"WOLF_VARIANT",
			"wolfvariant",
			"wolf variants"
		);
		Classes.registerClass(wolfVariantClassInfo
			.user("wolf ?variants?")
			.name("Wolf Variant")
			.description("Represents the variant of a wolf entity.",
				"NOTE: Minecraft namespaces are supported, ex: 'minecraft:ashen'.")
			.since("2.10")
			.requiredPlugins("Minecraft 1.21+")
			.documentationId("WolfVariant"));

		VARIANTS = Iterators.toArray(Classes.getExactClassInfo(Variant.class).getSupplier().get(), Variant.class);

		registerInfo(
			infoBuilder(WolfData.class, "wolf")
				.dataPatterns(GROUPS)
				.entityType(EntityType.WOLF)
				.entityClass(Wolf.class)
				.supplier(WolfData::new)
				.build()
		);
	}

	private @Nullable Object variant = null;
	private @Nullable DyeColor collarColor = null;
	private Kleenean isAngry = Kleenean.UNKNOWN;
	private Kleenean isTamed = Kleenean.UNKNOWN;

	public WolfData() {}

	public WolfData(@Nullable Kleenean isAngry, @Nullable Kleenean isTamed) {
		this.isAngry = isAngry != null ? isAngry : Kleenean.UNKNOWN;
		this.isTamed = isTamed != null ? isTamed : Kleenean.UNKNOWN;
		super.groupIndex = GROUPS.getIndex(new WolfStates(this.isAngry, this.isTamed));
	}

	public WolfData(@Nullable WolfStates wolfState) {
		if (wolfState != null) {
			this.isAngry = wolfState.angry;
			this.isTamed = wolfState.tamed;
			super.groupIndex = GROUPS.getIndex(wolfState);
		} else {
			this.isAngry = Kleenean.UNKNOWN;
			this.isTamed = Kleenean.UNKNOWN;
			super.groupIndex = GROUPS.getIndex(new WolfStates(Kleenean.UNKNOWN, Kleenean.UNKNOWN));
		}
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedGroup, int matchedPattern, ParseResult parseResult) {
		WolfStates state = GROUPS.getData(matchedGroup);
		assert state != null;
		isAngry = state.angry;
		isTamed = state.tamed;
		if (exprs[0] != null) {
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
			variant = wolf.getVariant();
			super.groupIndex = GROUPS.getIndex(new WolfStates(this.isAngry, this.isTamed));
		}
		return true;
	}

	@Override
	public void set(Wolf wolf) {
		wolf.setAngry(isAngry.isTrue());
		wolf.setTamed(isTamed.isTrue());
		if (collarColor != null)
			wolf.setCollarColor(collarColor);
		Object variantSet = variant != null ? variant : CollectionUtils.getRandom(VARIANTS);
		assert variantSet != null;
		wolf.setVariant((Wolf.Variant) variantSet);
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

}
