package org.skriptlang.skript.bukkit.entity.data;

import ch.njol.skript.bukkitutil.BukkitUtils;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Frog.Variant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.EntityData;

import java.util.Objects;

public class FrogData extends EntityData<Frog> {

	private static final Variant[] VARIANTS = new Variant[]{Variant.TEMPERATE, Variant.WARM, Variant.COLD};

	private static final EntityDataPatterns<Variant> GROUPS = new EntityDataPatterns<>(
		new PatternGroup<>(0, "frog¦s @a", getPatterns("")),
		new PatternGroup<>(1, "temperate frog¦s @a", Variant.TEMPERATE, getPatterns("temperate")),
		new PatternGroup<>(2, "warm frog¦s @a", Variant.WARM, getPatterns("warm")),
		new PatternGroup<>(3, "cold frog¦s @a", Variant.COLD, getPatterns("cold"))
	);

	private static String[] getPatterns(String prefix) {
		String first = "<age> frog[plural:s]";
		String second = "baby:frog (kid[plural:s]|child[plural:ren])";
		if (!prefix.isEmpty()) {
			first = "<age> " + prefix + " frog[plural:s]";
			second = "baby:" + prefix + " frog (kid[plural:s]|child[plural:ren])";
		}
		return new String[]{first, second};
	}

	public static void register() {
		registerInfo(
			infoBuilder(FrogData.class, "frog")
				.dataPatterns(GROUPS)
				.entityType(EntityType.FROG)
				.entityClass(Frog.class)
				.supplier(FrogData::new)
				.build()
		);
		ClassInfo<?> frogVariantClassInfo = BukkitUtils.getRegistryClassInfo(
			"org.bukkit.entity.Frog$Variant",
			"FROG_VARIANT",
			"frogvariant",
			"frog variants"
		);
		assert frogVariantClassInfo != null;
		Classes.registerClass(frogVariantClassInfo
			.user("frog ?variants?")
			.name("Frog Variant")
			.description("Represents the variant of a frog entity.",
				"NOTE: Minecraft namespaces are supported, ex: 'minecraft:warm'.")
			.since("2.13")
			.documentationId("FrogVariant")
		);
	}

	private @Nullable Variant variant = null;

	public FrogData() {}

	public FrogData(@Nullable Variant variant) {
		this.variant = variant;
		super.groupIndex = GROUPS.getIndex(variant);
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedGroup, int matchedPattern, ParseResult parseResult) {
		variant = GROUPS.getData(matchedGroup);
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Frog> entityClass, @Nullable Frog frog) {
		if (frog != null) {
			variant = frog.getVariant();
			super.groupIndex = GROUPS.getIndex(variant);
		}
		return true;
	}

	@Override
	public void set(Frog frog) {
		Variant variant = this.variant;
		if (variant == null)
			variant = CollectionUtils.getRandom(VARIANTS);
		assert variant != null;
		frog.setVariant(variant);
	}

	@Override
	protected boolean match(Frog frog) {
		return dataMatch(variant, frog.getVariant());
	}

	@Override
	public Class<? extends Frog> getType() {
		return Frog.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new FrogData();
	}

	@Override
	protected int hashCode_i() {
		return Objects.hashCode(variant);
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof FrogData other))
			return false;
		return variant == other.variant;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof FrogData other))
			return false;
		return dataMatch(variant, other.variant);
	}

}
