package org.skriptlang.skript.bukkit.mannequin;

import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Mannequin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MannequinData extends EntityData<Mannequin> {

	public static void register() {
		register(MannequinData.class, "mannequin", Mannequin.class, 0, "mannequin");
	}

	private Kleenean immovable = Kleenean.UNKNOWN;

	public MannequinData() {}

	public MannequinData(@Nullable Kleenean immovable) {
		this.immovable = immovable == null ? Kleenean.UNKNOWN : immovable;
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		if (matchedPattern == 1) {
			immovable = Kleenean.TRUE;
		} else if (matchedPattern == 2) {
			immovable = Kleenean.FALSE;
		}
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Mannequin> entityClass, @Nullable Mannequin mannequin) {
		if (mannequin != null)
			immovable = Kleenean.get(mannequin.isImmovable());
		return true;
	}

	@Override
	public void set(Mannequin mannequin) {
		mannequin.setImmovable(immovable.isTrue());
	}

	@Override
	protected boolean match(Mannequin mannequin) {
		return kleeneanMatch(immovable, mannequin.isImmovable());
	}

	@Override
	public Class<? extends Mannequin> getType() {
		return Mannequin.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new MannequinData();
	}

	@Override
	protected int hashCode_i() {
		return immovable.hashCode();
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof MannequinData other))
			return false;
		return immovable == other.immovable;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof MannequinData other))
			return false;
		return kleeneanMatch(immovable, other.immovable);
	}

}
