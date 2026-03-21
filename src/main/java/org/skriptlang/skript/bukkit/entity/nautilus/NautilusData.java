package org.skriptlang.skript.bukkit.entity.nautilus;

import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Nautilus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class NautilusData extends EntityData<Nautilus> {

	public static void register() {
		EntityData.register(NautilusData.class, "nautilus", Nautilus.class, 0, "nautilus");
	}

	private Kleenean isTamed = Kleenean.UNKNOWN;

	public NautilusData() { }

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
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
