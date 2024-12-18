package ch.njol.skript.expressions;

import ch.njol.skript.ServerPlatform;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

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

	private static final double DEG_TO_RAD = Math.PI / 180;
	private static final double RAD_TO_DEG =  180 / Math.PI;

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
		} else if (object instanceof Location location) {
			return usesYaw
				? normalizeYaw(location.getYaw())
				: location.getPitch();
		} else if (object instanceof Vector vector) {
			return usesYaw
				? skriptYaw((getYaw(vector)))
				: skriptPitch(getPitch(vector));
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
		float yaw = getYaw(vector);
		float pitch = getPitch(vector);
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
					yaw = fromSkriptYaw(value);
				else
					pitch = fromSkriptPitch(value);
		}
		Vector newVector = fromYawAndPitch(yaw, pitch).multiply(vector.length());
		vector.copy(newVector);
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

	// TODO Mark as private next version after VectorMath deletion
	@ApiStatus.Internal
	public static Vector fromYawAndPitch(float yaw, float pitch) {
		double y = Math.sin(pitch * DEG_TO_RAD);
		double div = Math.cos(pitch * DEG_TO_RAD);
		double x = Math.cos(yaw * DEG_TO_RAD);
		double z = Math.sin(yaw * DEG_TO_RAD);
		x *= div;
		z *= div;
		return new Vector(x,y,z);
	}

	// TODO Mark as private next version after VectorMath deletion
	@ApiStatus.Internal
	public static float getYaw(Vector vector) {
		if (((Double) vector.getX()).equals((double) 0) && ((Double) vector.getZ()).equals((double) 0)){
			return 0;
		}
		return (float) (Math.atan2(vector.getZ(), vector.getX()) * RAD_TO_DEG);
	}

	// TODO Mark as private next version after VectorMath deletion
	@ApiStatus.Internal
	public static float getPitch(Vector vector) {
		double xy = Math.sqrt(vector.getX() * vector.getX() + vector.getZ() * vector.getZ());
		return (float) (Math.atan(vector.getY() / xy) * RAD_TO_DEG);
	}

	// TODO Mark as private next version after VectorMath deletion
	@ApiStatus.Internal
	public static float skriptYaw(float yaw) {
		return yaw < 90
			? yaw + 270
			: yaw - 90;
	}

	// TODO Mark as private next version after VectorMath deletion
	@ApiStatus.Internal
	public static float skriptPitch(float pitch) {
		return -pitch;
	}

	public static float fromSkriptYaw(float yaw) {
		return yaw > 270
			? yaw - 270
			: yaw + 90;
	}

	public static float fromSkriptPitch(float pitch) {
		return -pitch;
	}
}
