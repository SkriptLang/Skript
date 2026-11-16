package org.skriptlang.skript.bukkit.types;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.AbstractCubeMob;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Slime;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.skriptlang.skript.bukkit.types.EntityClassInfo.EntityChanger;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

public class LivingEntityClassInfo extends ClassInfo<LivingEntity> {

	public LivingEntityClassInfo() {
		super(LivingEntity.class, "livingentity");
		this.user("living ?entit(y|ies)")
			.name("Living Entity")
			.description("A living <a href='#entity'>entity</a>, i.e. a mob or <a href='#player'>player</a>, " +
					"not inanimate entities like <a href='#projectile'>projectiles</a> or dropped items.")
			.usage("see <a href='#entity'>entity</a>, but ignore inanimate objects")
			.examples("spawn 5 powered creepers",
					"shoot a zombie from the creeper")
			.since("1.0")
			.defaultExpression(new EventValueExpression<>(LivingEntity.class))
			.changer(new EntityChanger())
			.property(Property.SIZE,
				"The size of a slime type or phantom entity.",
				Skript.instance(),
				new LivingEntitySizeHandler());
	}

	private static class LivingEntitySizeHandler implements ExpressionPropertyHandler<LivingEntity, Integer> {

		// TODO: Remove 'Slime" usage in favor of 'AbstractCubeMob' when MC 26.2+ is minimum supported version.
		private static final boolean CUBE_MOB_EXISTS = Skript.classExists("org.bukkit.entity.AbstractCubeMob");
		private static final int MAXIMUM_SLIME_SIZE = 127;
		private static final int MAXIMUM_PHANTOM_SIZE = 64;

		@Override
		public @Nullable Integer convert(LivingEntity entity) {
			if (entity instanceof Phantom phantom) {
				return phantom.getSize();
			} else if (entity instanceof Slime slime) {
				// Skript follows the nbt format of 0-126 for slimes, as bukkit uses a 1-127 value
				return slime.getSize() - 1;
			} else if (CUBE_MOB_EXISTS && entity instanceof AbstractCubeMob cubeMob) {
				return cubeMob.getSize() - 1;
			}
			return null;
		}

		@Override
		public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
			return switch (mode) {
				case ADD, REMOVE, SET -> CollectionUtils.array(Number.class);
				case RESET -> CollectionUtils.array();
				default -> null;
			};
		}

		@Override
		public void change(LivingEntity entity, Object @Nullable [] delta, ChangeMode mode) {
			if (delta == null && mode != ChangeMode.RESET)
				return;

			double deltaSizeDouble = delta != null ? ((Number) delta[0]).doubleValue() : 0;
			if (Double.isNaN(deltaSizeDouble) || Double.isInfinite(deltaSizeDouble))
				return;
			// Due to how double to int conversions happen, we are required to reassign from delta to prevent integer overflows
			int sizeDelta = delta != null ? ((Number) delta[0]).intValue() : 0;
			if (mode == ChangeMode.REMOVE)
				sizeDelta = -sizeDelta;

			switch (mode) {
				case ADD, REMOVE -> {
					if (entity instanceof Phantom phantom) {
						int newSize = Math2.fit(0, (phantom.getSize() + sizeDelta), MAXIMUM_PHANTOM_SIZE);
						phantom.setSize(newSize);
					} else if (entity instanceof Slime slime) {
						int newSize = Math2.fit(1, (slime.getSize() + sizeDelta), MAXIMUM_SLIME_SIZE);
						slime.setSize(newSize);
					} else if (CUBE_MOB_EXISTS && entity instanceof AbstractCubeMob cubeMob) {
						int newSize = Math2.fit(1, (cubeMob.getSize() + sizeDelta), MAXIMUM_SLIME_SIZE);
						cubeMob.setSize(newSize);
					}
				}
				case SET, RESET -> {
					if (entity instanceof Phantom phantom) {
						phantom.setSize(Math2.fit(0, sizeDelta, MAXIMUM_PHANTOM_SIZE));
					} else if (entity instanceof Slime slime) {
						// Skript follows the nbt format of 0-126 for slimes, as bukkit uses a 1-127 value
						slime.setSize(Math2.fit(1, sizeDelta+1, MAXIMUM_SLIME_SIZE));
					} else if (CUBE_MOB_EXISTS && entity instanceof AbstractCubeMob cubeMob) {
						// Skript follows the nbt format of 0-126 for slimes, as bukkit uses a 1-127 value
						cubeMob.setSize(Math2.fit(1, sizeDelta+1, MAXIMUM_SLIME_SIZE));
					}
				}
			}
		}

		@Override
		public @NotNull Class<Integer> returnType() {
			return Integer.class;
		}
	}

}
