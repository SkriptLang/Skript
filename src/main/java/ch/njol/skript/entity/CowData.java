package ch.njol.skript.entity;

import ch.njol.skript.classes.registry.RegistryClassInfo;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.collect.Iterators;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Cow.Variant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CowData extends EntityData<Cow> {

	private static final Object[] VARIANTS;

	static {
		var cowVariantInfo = new RegistryClassInfo<>(Variant.class, RegistryKey.COW_VARIANT, "cowvariant", "cow variants");
		Classes.registerClass(cowVariantInfo
			.user("cow ?variants?")
			.name("Cow Variant")
			.description("Represents the variant of a cow entity.",
				"NOTE: Minecraft namespaces are supported, ex: 'minecraft:warm'.")
			.since("2.12")
			.requiredPlugins("Minecraft 1.21.5+")
			.documentationId("CowVariant")
		);
		VARIANTS = Iterators.toArray(cowVariantInfo.getSupplier().get(), Variant.class);

		register(CowData.class, "cow", Cow.class, 0, "cow");
	}

	private @Nullable Variant variant = null;

	public CowData() {}

	public CowData(@Nullable Variant variant) {
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
	protected boolean init(@Nullable Class<? extends Cow> entityClass, @Nullable Cow cow) {
		if (cow != null) {
			variant = cow.getVariant();
		}
		return true;
	}

	@Override
	public void set(Cow cow) {
		Variant variant = this.variant;
		if (variant == null)
			variant = (Variant) CollectionUtils.getRandom(VARIANTS);
		assert variant != null;
		cow.setVariant(variant);
	}

	@Override
	protected boolean match(Cow cow) {
		return variant == null || cow.getVariant() == variant;
	}

	@Override
	public Class<Cow> getType() {
		return Cow.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new CowData();
	}

	@Override
	protected int hashCode_i() {
		return Objects.hashCode(variant);
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof CowData other))
			return false;
		return variant == other.variant;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof CowData other))
			return false;
		return dataMatch(variant, other.variant);
	}

}
