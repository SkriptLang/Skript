package org.skriptlang.skript.bukkit.entity.strider;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Strider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.EntityData;

public class StriderData extends EntityData<Strider> {

	private static final EntityDataPatterns<Kleenean> GROUPS = new EntityDataPatterns<>(
		new PatternGroup<>(0, "strider¦s @a", Kleenean.UNKNOWN, getPatterns("")),
		new PatternGroup<>(1, "warm strider¦s @a", Kleenean.FALSE, getPatterns("warm")),
		new PatternGroup<>(2, "shivering strider¦s @a, cold strider¦s @a", Kleenean.TRUE, getPatterns("(cold|shivering)"))
	);

	private static String[] getPatterns(String prefix) {
		String first = "<age> strider[plural:s]";
		String second = "baby:strider (kid[plural:s]|child[plural:ren])";
		if (!prefix.isEmpty()) {
			first = "<age> " + prefix + " strider[plural:s]";
			second = "baby:" + prefix + " strider (kid[plural:s]|child[plural:ren])";
		}
		return new String[]{first, second};
	}

	public static void register() {
		registerInfo(
			infoBuilder(StriderData.class, "strider")
				.dataPatterns(GROUPS)
				.entityType(EntityType.STRIDER)
				.entityClass(Strider.class)
				.supplier(StriderData::new)
				.build()
		);
	}

	private Kleenean shivering = Kleenean.UNKNOWN;

	public StriderData() {}

	public StriderData(@Nullable Kleenean shivering) {
		this.shivering = shivering != null ? shivering : Kleenean.UNKNOWN;
		super.groupIndex = GROUPS.getIndex(this.shivering);
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedGroup, int matchedPattern, ParseResult parseResult) {
		shivering = GROUPS.getData(matchedGroup);
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Strider> entityClass, @Nullable Strider strider) {
		if (strider != null) {
			shivering = Kleenean.get(strider.isShivering());
			super.groupIndex = GROUPS.getIndex(shivering);
		}
		return true;
	}

	@Override
	public void set(Strider entity) {
		entity.setShivering(shivering.isTrue());
	}

	@Override
	protected boolean match(Strider strider) {
		return kleeneanMatch(shivering, strider.isShivering());
	}

	@Override
	public Class<? extends Strider> getType() {
		return Strider.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new StriderData();
	}

	@Override
	protected int hashCode_i() {
		return shivering.hashCode();
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof StriderData other))
			return false;
		return shivering == other.shivering;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof StriderData other))
			return false;
		return kleeneanMatch(shivering, other.shivering);
	}

}
