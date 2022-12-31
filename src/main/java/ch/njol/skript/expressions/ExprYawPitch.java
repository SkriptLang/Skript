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
		"A yaw of 0 or 360 represents the positive x direction. Adding a positive number to the yaw of a player will rotate it counter-clockwise.",
		"A pitch of 90 represents the positive y direction, or upward facing. A pitch of -90 represents downward facing. Adding a positive number to the pitch will rotate the player upwards.",
		"You can only change the yaw/pitch of entities, not players. Only Paper 1.19+ users may directly change the yaw/pitch of players."
})
@Examples({
		"log \"%player%: %location of player%, %player's yaw%, %player's pitch%\" to \"playerlocs.log\"",
		"set {_yaw} to yaw of player",
		"set {_p} to pitch of target entity",
		"set pitch of player to 90 # Makes the player look upwards, Paper 1.19+ only",
		"add 180 to yaw of target of player # Makes the target look behind him"
})
@Since("2.0, 2.2-dev28 (vector yaw/pitch), INSERT VERSION (changers)")
public class ExprYawPitch extends SimplePropertyExpression<Object, Number> {

	static {
		register(ExprYawPitch.class, Number.class, "(:yaw|pitch)", "entities/locations/vectors");
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
	public Number convert(Object object) {
		if (object instanceof Entity) {
			Location location = ((Entity) object).getLocation();
			return usesYaw
					? toSkriptYaw(location.getYaw())
					: toSkriptPitch(location.getPitch());
		} else if (object instanceof Location) {
			Location location = (Location) object;
			return usesYaw
					? toSkriptYaw(location.getYaw())
					: toSkriptPitch(location.getPitch());
		} else if (object instanceof Vector) {
			Vector vector = (Vector) object;
			return usesYaw
					? toSkriptYaw(VectorMath.getYaw(vector))
					: toSkriptPitch(VectorMath.getPitch(vector));
		}
		return null;
	}

	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (getExpr().getReturnType().isAssignableFrom(Player.class) && !SUPPORTS_PLAYERS)
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
					entity.setRotation(fromSkriptYaw(value), location.getPitch());
				} else {
					entity.setRotation(location.getYaw(), fromSkriptPitch(value));
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
					entity.setRotation(fromSkriptYaw(0), location.getPitch());
				} else {
					entity.setRotation(location.getYaw(), fromSkriptPitch(0));
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
					location.setYaw(fromSkriptYaw(value));
				} else {
					location.setPitch(fromSkriptPitch(value));
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
					location.setYaw(fromSkriptYaw(0));
				} else {
					location.setPitch(fromSkriptPitch(0));
				}
			default:
				break;
		}
	}

	private void changeForVector(Vector vector, float value, ChangeMode mode) {
		float yaw = VectorMath.getYaw(vector);
		float pitch = VectorMath.getPitch(vector);
		switch (mode) {
			case SET:
				if (usesYaw) {
					yaw = fromSkriptYaw(value);
				} else {
					pitch = fromSkriptPitch(value);
				}
				break;
			case REMOVE:
				value = -value;
			case ADD:
				if (usesYaw) {
					yaw += value;
				} else {
					// Subtracting because of Minecraft's upside-down pitch.
					pitch -= value;
				}
				break;
		}
		VectorMath.copyVector(vector, VectorMath.fromYawAndPitch(yaw, pitch).multiply(vector.length()));
	}

	private static float fromSkriptYaw(float yaw) {
		return Location.normalizeYaw(yaw + 90);
	}

	private static float toSkriptYaw(float yaw) {
		yaw = Location.normalizeYaw(yaw);
		return yaw - 90 < 0 ? yaw + 270 : yaw - 90;
	}

	private static float fromSkriptPitch(float pitch) {
		return Location.normalizePitch(-pitch);
	}

	private static float toSkriptPitch(float pitch) {
		return -Location.normalizePitch(pitch);
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	protected String getPropertyName() {
		return usesYaw ? "yaw" : "pitch";
	}

}
