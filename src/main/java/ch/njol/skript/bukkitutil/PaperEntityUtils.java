/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.bukkitutil;

import java.util.EnumSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;

import ch.njol.skript.Skript;
import io.papermc.paper.entity.LookAnchor;

public class PaperEntityUtils {

	private static final boolean LOOK_ANCHORS = Skript.classExists("io.papermc.paper.entity.LookAnchor");
	private static final boolean LOOK_AT = Skript.methodExists(Mob.class, "lookAt", Entity.class);

	/**
	 * Utility method for usage only from this class.
	 */
	private static void mobLookAt(Object target, @Nullable Float headRotationSpeed, @Nullable Float maxHeadPitch, Mob mob) {
		Bukkit.getMobGoals().getRunningGoals(mob, GoalType.LOOK).forEach(goal -> Bukkit.getMobGoals().removeGoal(mob, goal));
		float speed = headRotationSpeed != null ? headRotationSpeed : mob.getHeadRotationSpeed();
		float maxPitch = maxHeadPitch != null ? maxHeadPitch : mob.getMaxHeadPitch();
		if (target instanceof Location && !((Location) target).isWorldLoaded()) {
			Location location = (Location) target;
			target = new Location(mob.getWorld(), location.getX(), location.getY(), location.getZ());
		}
		Bukkit.getMobGoals().addGoal(mob, 0, new LookGoal(target, mob, speed, maxPitch));
	}

	/**
	 * Instruct a Mob (1.17+) to look at a specific vector/location/entity.
	 * Object can be a {@link org.bukkit.util.Vector}, {@link org.bukkit.Location} or {@link org.bukkit.entity.Entity}
	 * 
	 * @param target The vector/location/entity to make the livingentity look at.
	 * @param entities The living entities to make look at something.
	 */
	public static void lookAt(Object target, LivingEntity... entities) {
		lookAt(target, null, null, entities);
	}

	/**
	 * Instruct a Mob (1.17+) to look at a specific vector/location/entity.
	 * Object can be a {@link org.bukkit.util.Vector}, {@link org.bukkit.Location} or {@link org.bukkit.entity.Entity}
	 * 
	 * @param target The vector/location/entity to make the livingentity look at.
	 * @param headRotationSpeed The rotation speed at which the living entities will rotate their head to the target. Vanilla default values range from 10-50. Doesn't apply to players.
	 * @param maxHeadPitch The maximum pitch at which the eyes/feet can go to. Doesn't apply to players.
	 * @param entities The living entities to make look at something.
	 */
	public static void lookAt(Object target, @Nullable Float headRotationSpeed, @Nullable Float maxHeadPitch, LivingEntity... entities) {
		if (target == null || !LOOK_AT)
			return;
		// Use support for players if using Paper 1.19.1+
		if (LOOK_ANCHORS) {
			lookAt(LookAnchor.EYES, headRotationSpeed, maxHeadPitch, entities);
			return;
		}
		for (LivingEntity entity : entities) {
			if (!(entity instanceof Mob))
				continue;
			mobLookAt(target, headRotationSpeed, maxHeadPitch, (Mob) entity);
		}
	}

	/**
	 * Instruct a Mob (1.17+) or Players (1.19.1+) to look at a specific vector/location/entity.
	 * Object can be a {@link org.bukkit.util.Vector}, {@link org.bukkit.Location} or {@link org.bukkit.entity.Entity}
	 * THIS METHOD IS FOR 1.19.1+ ONLY. Use {@link lookAt(Object, Float, Float, LivingEntity...)} otherwise.
	 * 
	 * @param entityAnchor What part of the entity the player should face assuming the LivingEntity argument contains a player. Only for players.
	 * @param target The vector/location/entity to make the livingentity or player look at.
	 * @param headRotationSpeed The rotation speed at which the living entities will rotate their head to the target. Vanilla default values range from 10-50. Doesn't apply to players.
	 * @param maxHeadPitch The maximum pitch at which the eyes/feet can go to. Doesn't apply to players.
	 * @param entities The living entities to make look at something. Players can be involved in 1.19.1+
	 */
	public static void lookAt(LookAnchor entityAnchor, Object target, @Nullable Float headRotationSpeed, @Nullable Float maxHeadPitch, LivingEntity... entities) {
		if (target == null || !LOOK_AT || !LOOK_ANCHORS)
			return;
		for (LivingEntity entity : entities) {
			if (target instanceof Location && !((Location) target).isWorldLoaded()) {
				Location location = (Location) target;
				target = new Location(entity.getWorld(), location.getX(), location.getY(), location.getZ());
			}
			if (entity instanceof Player) {
				Player player = (Player) entity;
				if (target instanceof Vector) {
					Vector vector = (Vector) target;
					player.lookAt(player.getEyeLocation().add(vector), LookAnchor.EYES);
					player.lookAt(player.getEyeLocation().add(vector), LookAnchor.FEET);
				} else if (target instanceof Location) {
					player.lookAt((Location) target, LookAnchor.EYES);
					player.lookAt((Location) target, LookAnchor.FEET);
				} else if (target instanceof Entity) {
					player.lookAt((Entity) target, LookAnchor.EYES, entityAnchor);
					player.lookAt((Entity) target, LookAnchor.FEET, entityAnchor);
				}
			} else if (entity instanceof Mob) {
				mobLookAt(target, headRotationSpeed, maxHeadPitch, (Mob) entity);
			}
		}
	}

	public static class LookGoal implements Goal<Mob> {

		private static final GoalKey<Mob> SKRIPT_LOOK_KEY = GoalKey.of(Mob.class, new NamespacedKey(Skript.getInstance(), "skript_entity_look"));
		private static final EnumSet<GoalType> LOOK_GOAL = EnumSet.of(GoalType.LOOK);

		private static final int VECTOR = 0, LOCATION = 1, ENTITY = 2;

		private final float speed, maxPitch;
		private final Object target;
		private int ticks = 0, type;
		private final Mob mob;

		LookGoal(Object target, Mob mob, float speed, float maxPitch) {
			this.type = target instanceof Vector ? 0 : target instanceof Location ? 1 : 2;
			this.maxPitch = maxPitch;
			this.target = target;
			this.speed = speed;
			this.mob = mob;
		}

		@Override
		public boolean shouldActivate() {
			return ticks < 50;
		}

		@Override
		public void tick() {
			switch (type) {
				case VECTOR:
					Vector vector = ((Vector)target);
					mob.lookAt(mob.getEyeLocation().add(vector), speed, maxPitch);
					break;
				case LOCATION:
					mob.lookAt((Location) target, speed, maxPitch);
					break;
				case ENTITY:
					mob.lookAt((Entity) target, speed, maxPitch);
					break;
			}
			ticks++;
		}

		@Override
		public GoalKey<Mob> getKey() {
			return SKRIPT_LOOK_KEY;
		}

		@Override
		public EnumSet<GoalType> getTypes() {
			return LOOK_GOAL;
		}

	}

}
