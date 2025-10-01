package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.BukkitUtils;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.collect.Iterators;
import org.bukkit.entity.Pig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PigData extends EntityData<Pig> {

	private static final boolean VARIANTS_ENABLED;
	private static final Object[] VARIANTS;
	private static final Patterns<Kleenean> PATTERNS = new Patterns<>(new Object[][]{
		{"pig", Kleenean.UNKNOWN},
		{"saddled pig", Kleenean.TRUE},
		{"unsaddled pig", Kleenean.FALSE}
	});

	static {
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

		register(PigData.class, "pig", Pig.class, 0, PATTERNS.getPatterns());
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
		super.codeNameIndex = PATTERNS.getMatchedPattern(this.saddled, 0).orElse(0);
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		saddled = PATTERNS.getInfo(matchedCodeName);
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
			super.codeNameIndex = PATTERNS.getMatchedPattern(saddled, 0).orElse(0);
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
