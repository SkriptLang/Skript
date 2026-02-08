package org.skriptlang.skript.bukkit.entity.data;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.BukkitUtils;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.collect.Iterators;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.EntityData;

import java.util.Objects;

public class PigData extends EntityData<Pig> {

	private static boolean VARIANTS_ENABLED;
	private static Object[] VARIANTS;

	private static final EntityDataPatterns<Kleenean> GROUPS = new EntityDataPatterns<>(
		new PatternGroup<>(0, "pig¦s @a", Kleenean.UNKNOWN,
			"<age> [%-pigvariant%] pig[plural:s]", "[%-pigvariant%] <age> pig[plural:s]"),
		new PatternGroup<>(1, "saddled pig¦s @a", Kleenean.TRUE,
			"saddled [%-pigvariant%] pig[plural:s]", "[%-pigvariant%] saddled pig[plural:s]"),
		new PatternGroup<>(2, "unsaddled pig¦s @an", Kleenean.FALSE,
			"unsaddled [%-pigvariant%] pig[plural:s]", "[%-pigvariant%] unsaddled pig[plural:s]")
	);

	public static void register() {
		ClassInfo<?> pigVariantClassInfo = BukkitUtils.getRegistryClassInfo(
			"org.bukkit.entity.Pig$Variant",
			"PIG_VARIANT",
			"pigvariant",
			"pig variants"
		);
		if (pigVariantClassInfo == null) {
			// Registers a dummy/placeholder class to ensure working operation on MC versions that do not have 'Pig.Variant' (1.21.4-)
			pigVariantClassInfo = new ClassInfo<>(PigVariantDummy.class, "pigvariant");
		}
		Classes.registerClass(pigVariantClassInfo
			.user("pig ?variants?")
			.name("Pig Variant")
			.description("Represents the variant of a pig entity.",
				"NOTE: Minecraft namespaces are supported, ex: 'minecraft:warm'.")
			.since("2.12")
			.requiredPlugins("Minecraft 1.21.5+")
			.documentationId("PigVariant"));

		registerInfo(
			infoBuilder(PigData.class, "pig")
				.dataPatterns(GROUPS)
				.entityType(EntityType.PIG)
				.entityClass(Pig.class)
				.supplier(PigData::new)
				.build()
		);
		if (Skript.classExists("org.bukkit.entity.Pig$Variant")) {
			VARIANTS_ENABLED = true;
			VARIANTS = Iterators.toArray(Classes.getExactClassInfo(Pig.Variant.class).getSupplier().get(), Pig.Variant.class);
		} else {
			VARIANTS_ENABLED = false;
			VARIANTS = null;
		}
	}
	
	private Kleenean saddled = Kleenean.UNKNOWN;
	private @Nullable Object variant = null;

	public PigData() {}

	// TODO: When safe, 'variant' should have the type changed to 'Pig.Variant' when 1.21.5 is minimum supported version
	public PigData(@Nullable Kleenean saddled, @Nullable Object variant) {
		this.saddled = saddled != null ? saddled : Kleenean.UNKNOWN;
		this.variant = variant;
		super.groupIndex = GROUPS.getIndex(this.saddled);
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedGroup, int matchedPattern, ParseResult parseResult) {
		saddled = GROUPS.getData(matchedGroup);
		if (VARIANTS_ENABLED && exprs[0] != null) {
			//noinspection unchecked
			variant = ((Literal<Pig.Variant>) exprs[0]).getSingle();
		}
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Pig> entityClass, @Nullable Pig pig) {
		if (pig != null) {
			saddled = Kleenean.get(pig.hasSaddle());
			super.groupIndex = GROUPS.getIndex(saddled);
			if (VARIANTS_ENABLED)
				variant = pig.getVariant();
		}
		return true;
	}
	
	@Override
	public void set(Pig pig) {
		pig.setSaddle(saddled.isTrue());
		if (VARIANTS_ENABLED) {
			Object finalVariant = variant != null ? variant : CollectionUtils.getRandom(VARIANTS);
			assert finalVariant != null;
			pig.setVariant((Pig.Variant) finalVariant);
		}
	}
	
	@Override
	protected boolean match(Pig pig) {
		if (!kleeneanMatch(saddled, pig.hasSaddle()))
			return false;
		return variant == null || variant == pig.getVariant();
	}
	
	@Override
	public Class<? extends Pig> getType() {
		return Pig.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new PigData();
	}

	@Override
	protected int hashCode_i() {
		return saddled.ordinal() + Objects.hashCode(variant);
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof PigData other))
			return false;
		if (saddled != other.saddled)
			return false;
		return variant == other.variant;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof PigData other))
			return false;
		if (!kleeneanMatch(saddled, other.saddled))
			return false;
		return variant == null || variant == other.variant;
	}

	/**
	 * A dummy/placeholder class to ensure working operation on MC versions that do not have `Pig.Variant`
	 */
	public static class PigVariantDummy {}
	
}
