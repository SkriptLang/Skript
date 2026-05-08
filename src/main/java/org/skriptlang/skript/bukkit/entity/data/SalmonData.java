package org.skriptlang.skript.bukkit.entity.data;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Salmon;
import org.bukkit.entity.Salmon.Variant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.EntityData;

import java.util.Objects;

public class SalmonData extends EntityData<Salmon> {

	private static final boolean SUPPORT_SALMON_VARIANTS = Skript.classExists("org.bukkit.entity.Salmon$Variant");
	private static Object[] VARIANTS;

	private static EntityDataPatterns<Object> GROUPS;

	public static void register() {
		if (SUPPORT_SALMON_VARIANTS) {
			VARIANTS = Salmon.Variant.values();
			GROUPS = new EntityDataPatterns<>(
				new PatternGroup<>(0, "salmon¦s @a", "[any] salmon[plural:s]"),
				new PatternGroup<>(1, "small salmon¦s @a", Variant.SMALL, "small salmon[plural:s]"),
				new PatternGroup<>(2, "medium salmon¦s @a", Variant.MEDIUM, "medium salmon[plural:s]"),
				new PatternGroup<>(3, "large salmon¦s @a", Variant.LARGE, "large salmon[plural:s]")
			);

			Variables.yggdrasil.registerSingleClass(Variant.class, "Salmon.Variant");
		} else {
			VARIANTS = null;
			//noinspection unchecked
			GROUPS = (EntityDataPatterns<Object>) EntityDataPatterns.of("salmon¦s @a", "salmon[plural:s]");
		}

		registerInfo(
			infoBuilder(SalmonData.class, "salmon")
				.dataPatterns(GROUPS)
				.entityType(EntityType.SALMON)
				.entityClass(Salmon.class)
				.supplier(SalmonData::new)
				.build()
		);
	}

	private @Nullable Object variant = null;

	public SalmonData() {}

	// TODO: When safe, 'variant' should have the type changed to 'Salmon.Variant' when 1.21.2 is minimum supported version
	public SalmonData(@Nullable Object variant) {
		this.variant = variant;
		super.groupIndex = GROUPS.getIndex(variant);
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedGroup, int matchedPattern, ParseResult parseResult) {
		variant = GROUPS.getData(matchedGroup);
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Salmon> entityClass, @Nullable Salmon salmon) {
		if (salmon != null && SUPPORT_SALMON_VARIANTS) {
			variant = salmon.getVariant();
			super.groupIndex = GROUPS.getIndex(variant);
		}
		return true;
	}

	@Override
	public void set(Salmon entity) {
		if (SUPPORT_SALMON_VARIANTS) {
			Variant variant = (Variant) this.variant;
			if (variant == null)
				variant = (Variant) CollectionUtils.getRandom(VARIANTS);
			assert variant != null;
			entity.setVariant(variant);
		}
	}

	@Override
	protected boolean match(Salmon entity) {
		return variant == null || variant == entity.getVariant();
	}

	@Override
	public Class<? extends Salmon> getType() {
		return Salmon.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new SalmonData();
	}

	@Override
	protected int hashCode_i() {
		return Objects.hashCode(variant);
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof SalmonData other))
			return false;
        return variant == other.variant;
    }

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof SalmonData other))
			return false;
		return dataMatch(variant, other.variant);
	}

}
