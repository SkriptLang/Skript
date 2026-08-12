package ch.njol.skript.entity;

import ch.njol.skript.classes.registry.RegistryClassInfo;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.google.common.collect.Iterators;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Pig.Variant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PigData extends EntityData<Pig> {

	private static final Variant[] VARIANTS;
	private static final Patterns<Kleenean> PATTERNS = new Patterns<>(new Object[][]{
		{"pig", Kleenean.UNKNOWN},
		{"saddled pig", Kleenean.TRUE},
		{"unsaddled pig", Kleenean.FALSE}
	});

	static {
		var pigVariantInfo = new RegistryClassInfo<>(Variant.class, RegistryKey.PIG_VARIANT, "pigvariant", "pig variants");
		Classes.registerClass(pigVariantInfo
			.user("pig ?variants?")
			.name("Pig Variant")
			.description("Represents the variant of a pig entity.",
				"NOTE: Minecraft namespaces are supported, ex: 'minecraft:warm'.")
			.since("2.12")
			.requiredPlugins("Minecraft 1.21.5+")
			.documentationId("PigVariant"));
		VARIANTS = Iterators.toArray(pigVariantInfo.getSupplier().get(), Pig.Variant.class);

		register(PigData.class, "pig", Pig.class, 0, PATTERNS.getPatterns());
	}
	
	private Kleenean saddled = Kleenean.UNKNOWN;
	private @Nullable Variant variant;

	public PigData() {}

	public PigData(@Nullable Kleenean saddled, @Nullable Variant variant) {
		this.saddled = saddled != null ? saddled : Kleenean.UNKNOWN;
		this.variant = variant;
		super.codeNameIndex = PATTERNS.getMatchedPattern(this.saddled, 0).orElse(0);
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		saddled = PATTERNS.getInfo(matchedCodeName);
		if (exprs[0] != null) {
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
