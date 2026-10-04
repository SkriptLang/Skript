package org.skriptlang.skript.bukkit.entity.data;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Goat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.EntityData;

public class GoatData extends EntityData<Goat> {

	private static final EntityDataPatterns<Kleenean> GROUPS = new EntityDataPatterns<>(
		new PatternGroup<>(0, "goat:s @a", Kleenean.UNKNOWN, getPatterns("")),
		new PatternGroup<>(1, "screaming goat:s @a", Kleenean.TRUE, getPatterns("screaming ")),
		new PatternGroup<>(2, "quiet goat:s @a", Kleenean.FALSE, getPatterns("quiet "))
	);

	private static String[] getPatterns(String prefix) {
		return new String[]{
			"<age> " + prefix + "goat[plural:s]",
			"baby:" + prefix + "goat (kid[plural:s]|child[plural:ren])"
		};
	}

	public static void register() {
		registerInfo(
			infoBuilder(GoatData.class, "goat")
				.dataPatterns(GROUPS)
				.entityType(EntityType.GOAT)
				.entityClass(Goat.class)
				.supplier(GoatData::new)
				.build()
		);
	}

	private Kleenean screaming = Kleenean.UNKNOWN;

	public GoatData() {}

	public GoatData(@Nullable Kleenean screaming) {
		this.screaming = screaming != null ? screaming : Kleenean.UNKNOWN;
		super.groupIndex = GROUPS.getIndex(this.screaming);
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedGroup, int matchedPattern, ParseResult parseResult) {
		screaming = GROUPS.getData(matchedGroup);
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Goat> entityClass, @Nullable Goat goat) {
		if (goat != null) {
			screaming = Kleenean.get(goat.isScreaming());
			super.groupIndex = GROUPS.getIndex(screaming);
		}
		return true;
	}

	@Override
	public void set(Goat goat) {
		goat.setScreaming(screaming.isTrue());
	}

	@Override
	protected boolean match(Goat goat) {
		return kleeneanMatch(screaming, goat.isScreaming());
	}

	@Override
	public Class<? extends Goat> getType() {
		return Goat.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new GoatData();
	}

	@Override
	protected int hashCode_i() {
		return screaming.hashCode();
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof GoatData other))
			return false;
		return screaming == other.screaming;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof GoatData other))
			return false;
		return kleeneanMatch(screaming, other.screaming);
	}

}
