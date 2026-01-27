package org.skriptlang.skript.bukkit.entity.data;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Patterns;
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
	private static Patterns<Object> CODE_NAMES;

	public static void register() {
		if (SUPPORT_SALMON_VARIANTS) {
			VARIANTS = Salmon.Variant.values();
			CODE_NAMES = new Patterns<>(new Object[][]{
				{"salmon", null},
				{"any salmon", null},
				{"small salmon", Variant.SMALL},
				{"medium salmon", Variant.MEDIUM},
				{"large salmon", Variant.LARGE}
			});

			Variables.yggdrasil.registerSingleClass(Variant.class, "Salmon.Variant");
		} else {
			VARIANTS = null;
			CODE_NAMES = new Patterns<>(new Object[][]{
				{"salmon", null}
			});
		}

		registerInfo(
			infoBuilder(SalmonData.class, "salmon")
				.addCodeNames(CODE_NAMES.getPatterns())
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
		super.codeNameIndex = CODE_NAMES.getMatchedPattern(variant, 0).orElse(0);
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		variant = CODE_NAMES.getInfo(matchedCodeName);
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Salmon> entityClass, @Nullable Salmon salmon) {
		if (salmon != null && SUPPORT_SALMON_VARIANTS) {
			variant = salmon.getVariant();
			super.codeNameIndex = CODE_NAMES.getMatchedPattern(variant, 0).orElse(0);
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
