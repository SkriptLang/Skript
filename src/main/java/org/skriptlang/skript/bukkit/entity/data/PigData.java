package org.skriptlang.skript.bukkit.entity.data;

import ch.njol.skript.classes.registry.RegistryClassInfo;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.collect.Iterators;
import org.bukkit.entity.EntityType;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Pig.Variant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.EntityData;

import java.util.Objects;

public class PigData extends EntityData<Pig> {

	private static Variant[] VARIANTS;

	private static final EntityDataPatterns<Kleenean> GROUPS = new EntityDataPatterns<>(
		new PatternGroup<>(0, "pig:s @a", Kleenean.UNKNOWN,
			"<age> [%-pigvariant%] pig[plural:s]", "[%-pigvariant%] <age> pig[plural:s]",
			"baby:[%-pigvariant%] piglet[plural:s]"),
		new PatternGroup<>(1, "saddled pig:s @a", Kleenean.TRUE,
			"saddled [%-pigvariant%] pig[plural:s]", "[%-pigvariant%] saddled pig[plural:s]"),
		new PatternGroup<>(2, "unsaddled pig:s @an", Kleenean.FALSE,
			"unsaddled [%-pigvariant%] pig[plural:s]", "[%-pigvariant%] unsaddled pig[plural:s]")
	);

	public static void register() {
		var pigVariantInfo = new RegistryClassInfo<>(Variant.class, RegistryKey.PIG_VARIANT, "pigvariant", "pig variants");
		Classes.registerClass(pigVariantInfo
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

		VARIANTS = Iterators.toArray(pigVariantInfo.getSupplier().get(), Pig.Variant.class);
	}
	
	private Kleenean saddled = Kleenean.UNKNOWN;
	private @Nullable Variant variant = null;

	public PigData() {}

	public PigData(@Nullable Kleenean saddled, @Nullable Variant variant) {
		this.saddled = saddled != null ? saddled : Kleenean.UNKNOWN;
		this.variant = variant;
		super.groupIndex = GROUPS.getIndex(this.saddled);
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedGroup, int matchedPattern, ParseResult parseResult) {
		saddled = GROUPS.getData(matchedGroup);
		if (exprs[0] != null) {
			//noinspection unchecked
			variant = ((Literal<Variant>) exprs[0]).getSingle();
		}
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Pig> entityClass, @Nullable Pig pig) {
		if (pig != null) {
			saddled = Kleenean.get(pig.hasSaddle());
			super.groupIndex = GROUPS.getIndex(saddled);
			variant = pig.getVariant();
		}
		return true;
	}
	
	@Override
	public void set(Pig pig) {
		pig.setSaddle(saddled.isTrue());
		Variant finalVariant = variant != null ? variant : CollectionUtils.getRandom(VARIANTS);
		assert finalVariant != null;
		pig.setVariant(finalVariant);
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

}
