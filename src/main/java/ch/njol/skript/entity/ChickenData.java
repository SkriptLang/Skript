package ch.njol.skript.entity;

import ch.njol.skript.classes.registry.RegistryClassInfo;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.collect.Iterators;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Chicken.Variant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ChickenData extends EntityData<Chicken> {

	private static final Variant[] VARIANTS;

	static {
		var chickenVariantInfo = new RegistryClassInfo<>(Variant.class, RegistryKey.CHICKEN_VARIANT, "chickenvariant", "chicken variants");
		Classes.registerClass(chickenVariantInfo
			.user("chicken ?variants?")
			.name("Chicken Variant")
			.description("Represents the variant of a chicken entity.",
				"NOTE: Minecraft namespaces are supported, ex: 'minecraft:warm'.")
			.since("2.12")
			.requiredPlugins("Minecraft 1.21.5+")
			.documentationId("ChickenVariant")
		);
		VARIANTS = Iterators.toArray(chickenVariantInfo.getSupplier().get(), Variant.class);

		register(ChickenData.class, "chicken", Chicken.class, "chicken");
	}

	private @Nullable Variant variant = null;

	public ChickenData() {}

	public ChickenData(@Nullable Variant variant) {
		this.variant = variant;
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		if (exprs[0] != null) {
			//noinspection unchecked
			variant = ((Literal<Variant>) exprs[0]).getSingle();
		}
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Chicken> entityClass, @Nullable Chicken chicken) {
		if (chicken != null) {
			variant = chicken.getVariant();
		}
		return true;
	}

	@Override
	public void set(Chicken chicken) {
		Variant variant = this.variant;
		if (variant == null)
			variant = CollectionUtils.getRandom(VARIANTS);
		assert variant != null;
		chicken.setVariant(variant);
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

}
