package org.skriptlang.skript.bukkit.entity.data;

import ch.njol.skript.classes.registry.RegistryClassInfo;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.collect.Iterators;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Frog.Variant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.EntityData;

import java.util.Objects;

public class FrogData extends EntityData<Frog> {

	private static Variant[] VARIANTS;

	private static final EntityDataPatterns<Variant> GROUPS = new EntityDataPatterns<>(
		new PatternGroup<>(0, "frog:s @a", getPatterns("")),
		new PatternGroup<>(1, "temperate frog:s @a", Variant.TEMPERATE, getPatterns("temperate ")),
		new PatternGroup<>(2, "warm frog:s @a", Variant.WARM, getPatterns("warm ")),
		new PatternGroup<>(3, "cold frog:s @a", Variant.COLD, getPatterns("cold "))
	);

	private static String[] getPatterns(String prefix) {
		return new String[]{
			"<age> " + prefix + "frog[plural:s]",
			"baby:" + prefix + "frog (kid[plural:s]|child[plural:ren])"
		};
	}

	public static void register() {
		var frogVariantInfo = new RegistryClassInfo<>(Variant.class, RegistryKey.FROG_VARIANT, "frogvariant", "frog variants");
		Classes.registerClass(frogVariantInfo
			.user("frog ?variants?")
			.name("Frog Variant")
			.description("Represents the variant of a frog entity.",
				"NOTE: Minecraft namespaces are supported, ex: 'minecraft:warm'.")
			.since("2.13")
			.documentationId("FrogVariant")
		);

		registerInfo(
			infoBuilder(FrogData.class, "frog")
				.dataPatterns(GROUPS)
				.entityType(EntityType.FROG)
				.entityClass(Frog.class)
				.supplier(FrogData::new)
				.build()
		);

		VARIANTS = Iterators.toArray(frogVariantInfo.getSupplier().get(), Variant.class);
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
