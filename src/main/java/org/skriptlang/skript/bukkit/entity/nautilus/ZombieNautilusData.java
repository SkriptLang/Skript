package org.skriptlang.skript.bukkit.entity.nautilus;

import ch.njol.skript.classes.registry.RegistryClassInfo;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Registry;
import org.bukkit.entity.ZombieNautilus;
import org.bukkit.entity.ZombieNautilus.Variant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ZombieNautilusData extends EntityData<ZombieNautilus> {

	private static Variant[] VARIANTS;

	public static void register() {
		EntityData.register(ZombieNautilusData.class, "zombie nautilus", ZombieNautilus.class, 0, "zombie nautilus");
		Variables.yggdrasil.registerSingleClass(Variant.class,  "ZombieNautilus.Variant");

		Registry<@NotNull Variant> variantRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ZOMBIE_NAUTILUS_VARIANT);
		VARIANTS = variantRegistry.stream().toArray(Variant[]::new);
		Classes.registerClass(new RegistryClassInfo<>(Variant.class, variantRegistry, "zombienautilusvariant", "zombie nautilus variants")
			.user("zombie ?nautilus ?variants?")
			.name("Zombie Nautilus Variant")
			.description("Represents the variant of a zombie nautilus.")
			.since("INSERT VERSION")
			.documentationId("ZombieNautilusVariant"));
	}

	private Kleenean isTamed = Kleenean.UNKNOWN;
	private @Nullable Variant variant = null;

	public ZombieNautilusData() { }

	public ZombieNautilusData(@Nullable Variant variant) {
		this.variant = variant;
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		if (parseResult.hasTag("tamed")) {
			isTamed = Kleenean.TRUE;
		}
		if (exprs[0] != null) {
			//noinspection unchecked
			variant = ((Literal<ZombieNautilus.Variant>) exprs[0]).getSingle();
		}
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends ZombieNautilus> entityClass, @Nullable ZombieNautilus zombieNautilus) {
		if (zombieNautilus != null) {
			isTamed = Kleenean.get(zombieNautilus.isTamed());
			variant = zombieNautilus.getVariant();
		}
		return true;
	}

	@Override
	public void set(ZombieNautilus zombieNautilus) {
		zombieNautilus.setTamed(isTamed.isTrue());
		Variant variant = this.variant;
		if (variant == null) {
			variant = CollectionUtils.getRandom(VARIANTS);
		}
		assert variant != null;
		zombieNautilus.setVariant(variant);
	}

	@Override
	protected boolean match(ZombieNautilus zombieNautilus) {
		return kleeneanMatch(isTamed, zombieNautilus.isTamed()) &&
			dataMatch(variant, zombieNautilus.getVariant());
	}

	@Override
	public Class<? extends ZombieNautilus> getType() {
		return ZombieNautilus.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new ZombieNautilusData();
	}

	@Override
	protected int hashCode_i() {
		return Objects.hash(isTamed, variant);
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof ZombieNautilusData other)) {
			return false;
		}
		return isTamed == other.isTamed && variant == other.variant;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof ZombieNautilusData other)) {
			return false;
		}
		return kleeneanMatch(isTamed, other.isTamed) && dataMatch(variant, other.variant);
	}

}
