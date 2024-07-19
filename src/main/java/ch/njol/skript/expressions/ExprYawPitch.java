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

import ch.njol.skript.ServerPlatform;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.util.VectorMath;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

@Name("Yaw / Pitch")
@Description({
		"The yaw or pitch of a location or vector.",
		"A yaw of 0 or 360 represents the positive z direction. Adding a positive number to the yaw of a player will rotate it clockwise.",
		"A pitch of 90 represents the negative y direction, or downward facing. A pitch of -90 represents upward facing. Adding a positive number to the pitch will rotate the direction downwards.",
		"Only Paper 1.19+ users may directly change the yaw/pitch of players."
})
@Examples({
		"log \"%player%: %location of player%, %player's yaw%, %player's pitch%\" to \"playerlocs.log\"",
		"set {_yaw} to yaw of player",
		"set {_p} to pitch of target entity",
		"set pitch of player to -90 # Makes the player look upwards, Paper 1.19+ only",
		"add 180 to yaw of target of player # Makes the target look behind themselves"
})
@Since("2.0, 2.2-dev28 (vector yaw/pitch), 2.9.0 (entity changers)")
@RequiredPlugins("Paper 1.19+ (player changers)")
public class ExprYawPitch extends SimplePropertyExpression<Object, Float> {

	static {
		register(ExprYawPitch.class, Float.class, "(:yaw|pitch)", "entities/locations/vectors");
	}

	// For non-Paper versions lower than 1.19, changing the rotation of an entity is not supported for players.
	private static final boolean SUPPORTS_PLAYERS = Skript.isRunningMinecraft(1, 19) && Skript.getServerPlatform() == ServerPlatform.BUKKIT_PAPER;

	private boolean usesYaw;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		usesYaw = parseResult.hasTag("yaw");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public Float convert(Object object) {
		if (object instanceof Entity) {
			Location location = ((Entity) object).getLocation();
			return usesYaw
					? normalizeYaw(location.getYaw())
					: location.getPitch();
		} else if (object instanceof Location) {
			Location location = (Location) object;
			return usesYaw
					? normalizeYaw(location.getYaw())
					: location.getPitch();
		} else if (object instanceof Vector) {
			Vector vector = (Vector) object;
			return usesYaw
					? VectorMath.skriptYaw((VectorMath.getYaw(vector)))
					: VectorMath.skriptPitch(VectorMath.getPitch(vector));
		}
		return null;
	}

	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (Player.class.isAssignableFrom(getExpr().getReturnType()) && !SUPPORTS_PLAYERS)
			return null;

		switch (mode) {
			case SET:
			case ADD:
			case REMOVE:
				return CollectionUtils.array(Number.class);
			case RESET:
				return new Class[0];
			default:
				return null;
		}
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (delta == null && mode != ChangeMode.RESET)
			return;
		float value = ((Number) delta[0]).floatValue();
		for (Object object : getExpr().getArray(event)) {
			if (object instanceof Player && !SUPPORTS_PLAYERS)
				continue;
				
			if (object instanceof Entity) {
				changeForEntity((Entity) object, value, mode);
			} else if (object instanceof Location) {
				changeForLocation(((Location) object), value, mode);
			} else if (object instanceof Vector) {
				changeForVector(((Vector) object), value, mode);
			}
		}
	}

	private void changeForEntity(Entity entity, float value, ChangeMode mode) {
		Location location = entity.getLocation();
		switch (mode) {
			case SET:
				if (usesYaw) {
					entity.setRotation(value, location.getPitch());
				} else {
					entity.setRotation(location.getYaw(), value);
				}
				break;
			case REMOVE:
				value = -value;
			case ADD:
				if (usesYaw) {
					entity.setRotation(location.getYaw() + value, location.getPitch());
				} else {
					// Subtracting because of Minecraft's upside-down pitch.
					entity.setRotation(location.getYaw(), location.getPitch() - value);
				}
				break;
			case RESET:
				if (usesYaw) {
					entity.setRotation(0, location.getPitch());
				} else {
					entity.setRotation(location.getYaw(), 0);
				}
				break;
			default:
				break;
		}
	}

	private void changeForLocation(Location location, float value, ChangeMode mode) {
		switch (mode) {
			case SET:
				if (usesYaw) {
					location.setYaw(value);
				} else {
					location.setPitch(value);
				}
				break;
			case REMOVE:
				value = -value;
			case ADD:
				if (usesYaw) {
					location.setYaw(location.getYaw() + value);
				} else {
					// Subtracting because of Minecraft's upside-down pitch.
					location.setPitch(location.getPitch() - value);
				}
				break;
			case RESET:
				if (usesYaw) {
					location.setYaw(0);
				} else {
					location.setPitch(0);
				}
			default:
				break;
		}
	}

	private void changeForVector(Vector vector, float value, ChangeMode mode) {
		float yaw = VectorMath.getYaw(vector);
		float pitch = VectorMath.getPitch(vector);
		switch (mode) {
			case REMOVE:
				value = -value;
				// $FALL-THROUGH$
			case ADD:
				if (usesYaw) {
					yaw += value;
				} else {
					// Subtracting because of Minecraft's upside-down pitch.
					pitch -= value;
				}
				break;
			case SET:
				if (usesYaw)
					yaw = VectorMath.fromSkriptYaw(value);
				else
					pitch = VectorMath.fromSkriptPitch(value);
		}
		Vector newVector = VectorMath.fromYawAndPitch(yaw, pitch).multiply(vector.length());
		VectorMath.copyVector(vector, newVector);
	}

	private static float normalizeYaw(float yaw) {
		yaw = Location.normalizeYaw(yaw);
		return yaw < 0 ? yaw + 360 : yaw;
	}

	@Override
	public Class<? extends Float> getReturnType() {
		return Float.class;
	}

	@Override
	protected String getPropertyName() {
		return usesYaw ? "yaw" : "pitch";
	}

}
