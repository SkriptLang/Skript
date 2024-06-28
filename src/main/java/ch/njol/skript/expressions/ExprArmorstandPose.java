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
package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

@Name("Armor stand pose")
@Description("The pose of a specific part of an armor stand.")
@Examples( {
		"spawn an armor stand at player:",
		"    set the left arm pose of the armor stand to east",
})
@Since("INSERT VERSION")
public class ExprArmorstandPose extends SimpleExpression<Direction> {

	static {
		Skript.registerExpression(ExprArmorstandPose.class, Direction.class, ExpressionType.PROPERTY,
				"(0¦left arm|1¦right arm|2¦left leg|3¦right leg|4¦body|5¦head) pose of %livingentities%");
	}

	private Expression<Entity> entity;

	private ArmorStandPose pose;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		entity = (Expression<Entity>) exprs[0];
		pose = ArmorStandPose.getById(parseResult.mark);
		return true;
	}

	@Override
	protected @Nullable Direction[] get(Event event) {
		for (Entity entity : this.entity.getArray(event)) {
			if(!(entity instanceof ArmorStand))
				continue;
			ArmorStand armorStand = (ArmorStand) entity;
			EulerAngle eulerAngle = null;
			switch (pose) {
				case LEFT_ARM:
					eulerAngle = armorStand.getLeftArmPose();
					break;
				case RIGHT_ARM:
					eulerAngle = armorStand.getRightArmPose();
					break;
				case LEFT_LEG:
					eulerAngle = armorStand.getLeftLegPose();
					break;
				case RIGHT_LEG:
					eulerAngle = armorStand.getRightLegPose();
					break;
				case BODY:
					eulerAngle = armorStand.getBodyPose();
					break;
				case HEAD:
					eulerAngle = armorStand.getHeadPose();
					break;
			}

			if(eulerAngle == null)
				continue;
			return CollectionUtils.array(convertEulerAngleToDirection(eulerAngle, getYawOffset(armorStand)));
		}
		return null;
	}

	@Override
	public @Nullable Class<?>[] acceptChange(Changer.ChangeMode mode) {
		if(mode == Changer.ChangeMode.SET) {
			return CollectionUtils.array(Direction.class, Vector.class);
		}
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
		if(delta[0] == null || (!(delta[0] instanceof Direction) && !(delta[0] instanceof Vector)))
			return;
		for (Entity entity : entity.getArray(event)) {
			if(!(entity instanceof ArmorStand))
				continue;
			ArmorStand armorStand = (ArmorStand) entity;
			if(delta[0] instanceof Direction)
				setPose(armorStand, (Direction) delta[0]);
			else if(delta[0] instanceof Vector)
				setPose(armorStand, (Vector) delta[0]);
		}
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Direction> getReturnType() {
		return Direction.class;
	}

	private void setPose(ArmorStand armorStand, Vector vector) {
		switch (pose) {
			case LEFT_ARM:
				armorStand.setLeftArmPose(convertDirectionToEulerAngle(vector, getYawOffset(armorStand)));
				break;
			case RIGHT_ARM:
				armorStand.setRightArmPose(convertDirectionToEulerAngle(vector, getYawOffset(armorStand)));
				break;
			case LEFT_LEG:
				armorStand.setLeftLegPose(convertDirectionToEulerAngle(vector, getYawOffset(armorStand)));
				break;
			case RIGHT_LEG:
				armorStand.setRightLegPose(convertDirectionToEulerAngle(vector, getYawOffset(armorStand)));
				break;
			case BODY:
				armorStand.setBodyPose(convertDirectionToEulerAngle(vector, getYawOffset(armorStand)));
				break;
			case HEAD:
				armorStand.setHeadPose(convertDirectionToEulerAngle(vector, getYawOffset(armorStand)));
				break;
		}
	}

	private void setPose(ArmorStand armorStand, Direction direction) {
		setPose(armorStand, direction.getDirection());
	}

	/**
	 * @param vector Direction to convert to EulerAngle
	 * @param yawOffset The yaw adjustment (in degrees) to correct for the armor stand rotation.
	 * @return The euler angle representing the direction
	 */
	private @NotNull EulerAngle convertDirectionToEulerAngle(Vector vector, double yawOffset) {
		double x = vector.getX();
		double z = vector.getZ();
		double y = vector.getY();

		double yaw = Math.atan2(-x, z);
		double pitch = Math.atan2(y, Math.sqrt(x * x + z * z));

		return new EulerAngle(yaw + Math.toRadians(yawOffset), 0 , pitch + Math.toRadians(90));
	}

	/**
	 * @param eulerAngle EulerAngle to convert to Direction
	 * @param yawOffset The yaw adjustment (in degrees) to correct for the armor stand rotation.
	 * @return The direction (from normalized vector) representing the EulerAngle
	 */
	private @NotNull Direction convertEulerAngleToDirection(EulerAngle eulerAngle, double yawOffset) {
		double yaw = eulerAngle.getX();
		double pitch = eulerAngle.getZ();

		//Remove offset
		yaw -= Math.toRadians(yawOffset);
		pitch -= Math.toRadians(90);

		double x = -Math.sin(yaw) * Math.cos(pitch);
		double y = Math.sin(pitch);
		double z = Math.cos(yaw) * Math.cos(pitch);

		return new Direction(new Vector(x, y, z));
	}

	/**
	 * @param armorStand The armor stand to get the yaw offset from
	 * @return The yaw adjustment (in degrees) to correct for the armor stand rotation.
	 */
	private double getYawOffset(ArmorStand armorStand) {
		double yawOffset = Math.round(armorStand.getLocation().getYaw() / 45) * 45;
		boolean invert = yawOffset < 135;
		if(yawOffset > 135)
			yawOffset -= 90;
		else if(yawOffset < 135)
			yawOffset += 90;
		return invert ? -yawOffset : yawOffset;
	}

	private enum ArmorStandPose {
		LEFT_ARM(0), RIGHT_ARM(1), LEFT_LEG(2), RIGHT_LEG(3), BODY(4), HEAD(5);

		private final int id;

		ArmorStandPose(int id) {
			this.id = id;
		}

		public static @NotNull ArmorStandPose getById(int id) {
			for (ArmorStandPose pose : values()) {
				if (pose.id == id) {
					return pose;
				}
			}
			//Default to left arm if no valid id is found
			return ArmorStandPose.LEFT_ARM;
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "armor stand pose expression: " + pose + " of " + entity.toString(event, debug);
	}

}
