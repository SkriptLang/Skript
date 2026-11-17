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
				"The size of a slime type or phantom entity. Minimum and maximum size differ for each entity and newer Minecraft versions.",
				Skript.instance(),
				new LivingEntitySizeHandler());
	}


	private static class LivingEntitySizeHandler implements ExpressionPropertyHandler<LivingEntity, Integer> {
		//<editor-fold desc="living entity size handler" defaultstate="collapsed">

		// TODO: Remove 'Slime" usage in favor of 'AbstractCubeMob' when MC 26.2+ is minimum supported version.
		private static final boolean CUBE_MOB_EXISTS = Skript.classExists("org.bukkit.entity.AbstractCubeMob");
		private static final int MAXIMUM_SLIME_SIZE = 127;
		private static final int MAXIMUM_PHANTOM_SIZE = 64;

		@Override
		public @Nullable Integer convert(LivingEntity entity) {
			if (entity instanceof Phantom phantom) {
				return phantom.getSize();
			} else if (entity instanceof Slime slime) {
				return slime.getSize();
			} else if (CUBE_MOB_EXISTS && entity instanceof AbstractCubeMob cubeMob) {
				return cubeMob.getSize();
			}
			return null;
		}

		@Override
		public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
			return switch (mode) {
				case ADD, REMOVE, SET -> CollectionUtils.array(Integer.class);
				case RESET -> CollectionUtils.array();
				default -> null;
			};
		}

		@Override
		public void change(LivingEntity entity, Object @Nullable [] delta, ChangeMode mode) {
			if (delta == null && mode != ChangeMode.RESET)
				return;

			int sizeDelta = delta != null ? ((Integer) delta[0]) : 0;
			if (mode == ChangeMode.REMOVE)
				sizeDelta = -sizeDelta;

			if (entity instanceof Phantom phantom) {
				phantom.setSize(getNewSize(mode, sizeDelta, phantom.getSize(), MAXIMUM_PHANTOM_SIZE));
			} else if (entity instanceof Slime slime) {
				slime.setSize(getNewSize(mode, sizeDelta, slime.getSize(), MAXIMUM_SLIME_SIZE));
			} else if (CUBE_MOB_EXISTS && entity instanceof AbstractCubeMob cubeMob) {
				cubeMob.setSize(getNewSize(mode, sizeDelta, cubeMob.getSize(), MAXIMUM_SLIME_SIZE));
			}
		}

		private int getNewSize(ChangeMode mode, int delta, int current, int max) {
			if (mode == ChangeMode.SET)
				current = 0;
			return Math2.fit(0, current + delta, max);
		}

		@Override
		public @NotNull Class<Integer> returnType() {
			return Integer.class;
		}

		//</editor-fold>
	}

}
