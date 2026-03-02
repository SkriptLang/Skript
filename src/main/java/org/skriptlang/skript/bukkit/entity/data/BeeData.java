package org.skriptlang.skript.bukkit.entity.data;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Bee;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.EntityData;

import java.util.Random;

public class BeeData extends EntityData<Bee> {

	public record BeeState(Kleenean angry, Kleenean nectar) {}

	private static final EntityDataPatterns<BeeState> GROUPS = new EntityDataPatterns<>(
		new PatternGroup<>(0, "bee¦s @a", new BeeState(Kleenean.UNKNOWN, Kleenean.UNKNOWN), "<age> bee[plural:s]"),
		new PatternGroup<>(1, "no nectar bee¦s @a", new BeeState(Kleenean.UNKNOWN, Kleenean.FALSE), "<age> bee[plural:s] with(out| no) nectar"),
		new PatternGroup<>(2, "nectar bee¦s @a", new BeeState(Kleenean.UNKNOWN, Kleenean.TRUE), "<age> bee[plural:s] with nectar"),
		new PatternGroup<>(3, "happy bee¦s @a", new BeeState(Kleenean.FALSE, Kleenean.UNKNOWN), "<age> happy bee[plural:s]"),
		new PatternGroup<>(4, "happy nectar bee¦s @a", new BeeState(Kleenean.FALSE, Kleenean.TRUE), "<age> happy bee[plural:s] with nectar"),
		new PatternGroup<>(5, "happy no nectar bee¦s @a", new BeeState(Kleenean.FALSE, Kleenean.FALSE), "<age> happy bee[plural:s] with(out| no) nectar"),
		new PatternGroup<>(6, "angry bee¦s @a", new BeeState(Kleenean.TRUE, Kleenean.UNKNOWN), "<age> angry bee[plural:s]"),
		new PatternGroup<>(7, "angry no nectar bee¦s @a", new BeeState(Kleenean.TRUE, Kleenean.FALSE), "<age> angry bee[plural:s] with(out| no) nectar"),
		new PatternGroup<>(8, "angry nectar bee¦s @a", new BeeState(Kleenean.TRUE, Kleenean.TRUE), "<age> angry bee[plural:s] with nectar")
	);

	public static void register() {
		registerInfo(
			infoBuilder(BeeData.class,  "bee")
				.dataPatterns(GROUPS)
				.entityType(EntityType.BEE)
				.entityClass(Bee.class)
				.supplier(BeeData::new)
				.build()
		);
	}

	private Kleenean hasNectar = Kleenean.UNKNOWN;
	private Kleenean isAngry = Kleenean.UNKNOWN;

	public BeeData() {}

	public BeeData(@Nullable Kleenean isAngry, @Nullable Kleenean hasNectar) {
		this.isAngry = isAngry != null ? isAngry : Kleenean.UNKNOWN;
		this.hasNectar = hasNectar != null ? hasNectar : Kleenean.UNKNOWN;
		super.groupIndex = GROUPS.getIndex(new BeeState(this.isAngry, this.hasNectar));
	}

	public BeeData(@Nullable BeeState beeState) {
		if (beeState != null) {
			this.isAngry = beeState.angry;
			this.hasNectar = beeState.nectar;
			super.groupIndex = GROUPS.getIndex(beeState);
		} else {
			this.isAngry = Kleenean.UNKNOWN;
			this.hasNectar = Kleenean.UNKNOWN;
			super.groupIndex = GROUPS.getIndex(new BeeState(Kleenean.UNKNOWN, Kleenean.UNKNOWN));
		}
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedGroup, int matchedPattern, ParseResult parseResult) {
		BeeState state = GROUPS.getData(matchedGroup);
		assert state != null;
		hasNectar = state.nectar;
		isAngry = state.angry;
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Bee> entityClass, @Nullable Bee bee) {
		if (bee != null) {
			isAngry = Kleenean.get(bee.getAnger() > 0);
			hasNectar = Kleenean.get(bee.hasNectar());
			super.groupIndex = GROUPS.getIndex(new BeeState(this.isAngry, this.hasNectar));
		}
		return true;
	}
	
	@Override
	public void set(Bee bee) {
		int anger = 0;
		if (isAngry.isTrue())
			anger = new Random().nextInt(400) + 400;
		bee.setAnger(anger);
		bee.setHasNectar(hasNectar.isTrue());
	}
	
	@Override
	protected boolean match(Bee bee) {
		if (!kleeneanMatch(isAngry, bee.getAnger() > 0))
			return false;
		return kleeneanMatch(hasNectar, bee.hasNectar());
	}
	
	@Override
	public Class<? extends Bee> getType() {
		return Bee.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new BeeData();
	}

	@Override
	protected int hashCode_i() {
		int prime = 31;
		int result = 1;
		result = prime * result + isAngry.hashCode();
		result = prime * result + hasNectar.hashCode();
		return result;
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof BeeData other))
			return false;
		return isAngry == other.isAngry && hasNectar == other.hasNectar;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof BeeData other))
			return false;
		if (!kleeneanMatch(isAngry, other.isAngry))
			return false;
		return kleeneanMatch(hasNectar, other.hasNectar);
	}

}
