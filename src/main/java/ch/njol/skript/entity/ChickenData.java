package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.BukkitUtils;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.collect.Iterators;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Chicken.Variant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ChickenData extends EntityData<Chicken> {

	private static final boolean VARIANTS_ENABLED;
	private static final Object[] VARIANTS;

	static {
		ClassInfo<?> chickenVariantClassInfo = BukkitUtils.getRegistryClassInfo(
			"org.bukkit.entity.Chicken$Variant",
			"CHICKEN_VARIANT",
			"chickenvariant",
			"chicken variants"
		);
		if (chickenVariantClassInfo == null) {
			// Registers a dummy/placeholder class to ensure working operation on MC versions that do not have 'Chicken.Variant' (1.21.4-)
			chickenVariantClassInfo = new ClassInfo<>(ChickenVariantDummy.class,  "chickenvariant");
		}
		Classes.registerClass(chickenVariantClassInfo
			.user("chicken ?variants?")
			.name("Chicken Variant")
			.description("Represents the variant of a chicken entity.",
				"NOTE: Minecraft namespaces are supported, ex: 'minecraft:warm'.")
			.since("2.12")
			.requiredPlugins("Minecraft 1.21.5+")
			.documentationId("ChickenVariant")
		);

		register(ChickenData.class, "chicken", Chicken.class, "chicken");
		if (Skript.classExists("org.bukkit.entity.Chicken$Variant")) {
			VARIANTS_ENABLED = true;
			VARIANTS = Iterators.toArray(Classes.getExactClassInfo(Chicken.Variant.class).getSupplier().get(), Chicken.Variant.class);
		} else {
			VARIANTS_ENABLED = false;
			VARIANTS = null;
		}
	}

	private @Nullable Object variant = null;

	public ChickenData() {}

	// TODO: When safe, 'variant' should have the type changed to 'Chicken.Variant' when 1.21.6 is minimum supported version
	public ChickenData(@Nullable Object variant) {
		this.variant = variant;
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		if (VARIANTS_ENABLED && exprs[0] != null) {
			//noinspection unchecked
			variant = ((Literal<Chicken.Variant>) exprs[0]).getSingle();
		}
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Chicken> entityClass, @Nullable Chicken chicken) {
		if (chicken != null && VARIANTS_ENABLED) {
			variant = chicken.getVariant();
		}
		return true;
	}

	@Override
	public void set(Chicken chicken) {
		if (VARIANTS_ENABLED) {
			Variant variant = (Variant) this.variant;
			if (variant == null)
				variant = (Variant) CollectionUtils.getRandom(VARIANTS);
			assert variant != null;
			chicken.setVariant(variant);
		}
	}

	@Override
	protected boolean match(Chicken chicken) {
		return variant == null || variant == chicken.getVariant();
	}

	@Override
	public Class<? extends Chicken> getType() {
		return Chicken.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new ChickenData();
	}

	@Override
	protected int hashCode_i() {
		return Objects.hashCode(variant);
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof ChickenData other))
			return false;
		return variant == other.variant;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof ChickenData other))
			return false;
		return dataMatch(variant, other.variant);
	}

	/**
	 * A dummy/placeholder class to ensure working operation on MC versions that do not have `Chicken.Variant`
	 */
	public static class ChickenVariantDummy {}

}
