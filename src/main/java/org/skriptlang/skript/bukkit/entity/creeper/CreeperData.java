package org.skriptlang.skript.bukkit.entity.creeper;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.EntityData;

public class CreeperData extends EntityData<Creeper> {

	private static final EntityDataPatterns<Kleenean> GROUPS = new EntityDataPatterns<>(
		new PatternGroup<>(0, "creeper¦s @a", Kleenean.UNKNOWN, "creeper[plural:s]"),
		new PatternGroup<>(1, "powered creeper¦s @a", Kleenean.TRUE, "(powered|charged) creeper[plural:s]"),
		new PatternGroup<>(2, "unpowered creeper¦s @an", Kleenean.FALSE, "un(powered|charged) creeper[plural:s]")
	);

	public static void register() {
		registerInfo(
			infoBuilder(CreeperData.class, "creeper")
				.dataPatterns(GROUPS)
				.entityType(EntityType.CREEPER)
				.entityClass(Creeper.class)
				.supplier(CreeperData::new)
				.build()
		);
	}
	
	private Kleenean powered = Kleenean.UNKNOWN;

	public CreeperData() {}

	public CreeperData(@Nullable Kleenean powered)  {
		this.powered = powered != null ? powered : Kleenean.UNKNOWN;
		super.groupIndex = GROUPS.getIndex(this.powered);
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedGroup, int matchedPattern, ParseResult parseResult) {
		powered = GROUPS.getData(matchedGroup);
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Creeper> entityClass, @Nullable Creeper creeper) {
		if (creeper != null) {
			powered = Kleenean.get(creeper.isPowered());
			super.groupIndex = GROUPS.getIndex(powered);
		}
		return true;
	}
	
	@Override
	public void set(Creeper creeper) {
		creeper.setPowered(powered.isTrue());
	}
	
	@Override
	public boolean match(Creeper creeper) {
		return kleeneanMatch(powered, creeper.isPowered());
	}
	
	@Override
	public Class<Creeper> getType() {
		return Creeper.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new CreeperData();
	}

	@Override
	protected int hashCode_i() {
		return powered.hashCode();
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof CreeperData other))
			return false;
		return powered == other.powered;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof CreeperData other))
			return false;
		return kleeneanMatch(powered, other.powered);
	}

}
