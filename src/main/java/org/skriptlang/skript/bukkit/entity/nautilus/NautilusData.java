package org.skriptlang.skript.bukkit.entity.nautilus;

import org.bukkit.entity.EntityType;
import org.skriptlang.skript.bukkit.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Nautilus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class NautilusData extends EntityData<Nautilus> {

	private static final EntityDataPatterns<?> GROUP = EntityDataPatterns.of("nautilus¦es @a", "[:tamed] <age> nautilus[plural:es]");

	public static void register() {
		registerInfo(
			infoBuilder(NautilusData.class, "nautilus")
				.dataPatterns(GROUP)
				.entityType(EntityType.NAUTILUS)
				.entityClass(Nautilus.class)
				.supplier(NautilusData::new)
				.build()
		);
	}

	private Kleenean isTamed = Kleenean.UNKNOWN;

	public NautilusData() { }

	public NautilusData(@Nullable Kleenean isTamed) {
		this.isTamed = isTamed != null ? isTamed : Kleenean.UNKNOWN;
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedGroup, int matchedPattern, ParseResult parseResult) {
		if (parseResult.hasTag("tamed")) {
			isTamed = Kleenean.TRUE;
		}
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Nautilus> entityClass, @Nullable Nautilus nautilus) {
		if (nautilus != null) {
			isTamed = Kleenean.get(nautilus.isTamed());
		}
		return true;
	}

	@Override
	public void set(Nautilus nautilus) {
		nautilus.setTamed(isTamed.isTrue());
	}

	@Override
	protected boolean match(Nautilus nautilus) {
		return kleeneanMatch(isTamed, nautilus.isTamed());
	}

	@Override
	public Class<? extends Nautilus> getType() {
		return Nautilus.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new NautilusData();
	}

	@Override
	protected int hashCode_i() {
		return Objects.hashCode(isTamed);
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof NautilusData other)) {
			return false;
		}
		return isTamed == other.isTamed;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof NautilusData other)) {
			return false;
		}
		return kleeneanMatch(isTamed, other.isTamed);
	}

}
