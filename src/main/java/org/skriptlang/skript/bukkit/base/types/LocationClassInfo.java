package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.yggdrasil.Fields;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.WXYZHandler;
import org.skriptlang.skript.lang.properties.handlers.base.PropertyHandler;

import java.io.StreamCorruptedException;

@ApiStatus.Internal
public class LocationClassInfo extends ClassInfo<Location> {

	public LocationClassInfo() {
		super(Location.class, "location");
		this.user("locations?")
			.name("Location")
			.description("A location in a <a href='#world'>world</a>. Locations are world-specific and even store a <a href='#direction'>direction</a>, " +
				"e.g. if you save a location and later teleport to it you will face the exact same direction you did when you saved the location.")
			.usage("")
			.examples("teleport player to location at 0, 69, 0",
				"set {home::%uuid of player%} to location of the player")
			.since("1.0")
			.defaultExpression(new EventValueExpression<>(Location.class))
			.parser(new LocationParser())
			.serializer(new LocationSerializer())
			.cloner(Location::clone)
			.property(Property.WXYZ,
				"The X, Y, or Z coordinate of the location.",
				Skript.instance(),
				new LocationWXYZHandler());

	}

	private static class LocationWXYZHandler extends WXYZHandler<Location, Double> {
		//<editor-fold desc="location wxyz handler" defaultstate="collapsed">
		@Override
		public PropertyHandler<Location> newInstance() {
			var instance = new LocationWXYZHandler();
			instance.axis(axis);
			return instance;
		}

		@Override
		public @Nullable Double convert(Location propertyHolder) {
			return switch (axis) {
				case X -> propertyHolder.getX();
				case Y -> propertyHolder.getY();
				case Z -> propertyHolder.getZ();
				default -> null;
			};
		}

		@Override
		public boolean supportsAxis(Axis axis) {
			return axis != Axis.W;
		}

		@Override
		public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
			return switch (mode) {
				case ADD, SET, REMOVE -> new Class[]{Float.class};
				default -> null;
			};
		}

		@Override
		public void change(Location location, Object @Nullable [] delta, ChangeMode mode) {
			assert delta != null;
			float value = ((Float) delta[0]);
			if (axis == Axis.W)
				return;
			switch (mode) {
				case REMOVE:
					value = -value;
					//$FALL-THROUGH$
				case ADD:
					switch (axis) {
						case X -> location.setX(location.getX() + value);
						case Y -> location.setY(location.getY() + value);
						case Z -> location.setZ(location.getZ() + value);
					}
					break;
				case SET:
					switch (axis) {
						case X -> location.setX(value);
						case Y -> location.setY(value);
						case Z -> location.setZ(value);
					}
					break;
				default:
					assert false;
			}
		}

		@Override
		public @NotNull Class<Double> returnType() {
			return Double.class;
		}

		@Override
		public boolean requiresSourceExprChange() {
			return true;
		}
		//</editor-fold>
	}

	private static class LocationParser extends Parser<Location> {
		//<editor-fold desc="location parser" defaultstate="collapsed">
		@Override
		public boolean canParse(ParseContext context) {
			return false;
		}

		@Override
		public String toString(Location loc, int flags) {
			String worldPart = loc.getWorld() == null ? "" : " in '" + loc.getWorld().getName() + "'"; // Safety: getWorld is marked as Nullable by spigot
			return "x: " + Skript.toString(loc.getX()) + ", y: " + Skript.toString(loc.getY()) + ", z: " + Skript.toString(loc.getZ()) + ", yaw: " + Skript.toString(loc.getYaw()) + ", pitch: " + Skript.toString(loc.getPitch()) + worldPart;
		}

		@Override
		public String toVariableNameString(Location loc) {
			return loc.getWorld().getName() + ":" + loc.getX() + "," + loc.getY() + "," + loc.getZ();
		}

		@Override
		public String getDebugMessage(Location loc) {
			return "(" + loc.getWorld().getName() + ":" + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "|yaw=" + loc.getYaw() + "/pitch=" + loc.getPitch() + ")";
		}
		//</editor-fold>
	}

	private static class LocationSerializer extends Serializer<Location> {
		//<editor-fold desc="location serializer" defaultstate="collapsed">
		@Override
		public Fields serialize(Location location) {
			Fields fields = new Fields();
			World world = null;
			try {
				world = location.getWorld();
			} catch (IllegalArgumentException exception) {
				Skript.warning("A location failed to serialize with its defined world, as the world was unloaded.");
			}
			fields.putObject("world", world);
			fields.putPrimitive("x", location.getX());
			fields.putPrimitive("y", location.getY());
			fields.putPrimitive("z", location.getZ());
			fields.putPrimitive("yaw", location.getYaw());
			fields.putPrimitive("pitch", location.getPitch());
			return fields;
		}

		@Override
		public void deserialize(Location o, Fields f) {
			assert false;
		}

		@Override
		public Location deserialize(Fields f) throws StreamCorruptedException {
			return new Location(f.getObject("world", World.class),
				f.getPrimitive("x", double.class), f.getPrimitive("y", double.class), f.getPrimitive("z", double.class),
				f.getPrimitive("yaw", float.class), f.getPrimitive("pitch", float.class));
		}

		@Override
		public boolean canBeInstantiated() {
			return false; // no nullary constructor - also, saving the location manually prevents errors should Location ever be changed
		}

		@Override
		public boolean mustSyncDeserialization() {
			return true;
		}

		// return l.getWorld().getName() + ":" + l.getX() + "," + l.getY() + "," + l.getZ() + "|" + l.getYaw() + "/" + l.getPitch();
		@Override
		@Nullable
		public Location deserialize(String input) {
			final String[] split = input.split("[:,|/]");
			if (split.length != 6)
				return null;
			final World w = Bukkit.getWorld(split[0]);
			if (w == null)
				return null;
			try {
				final double[] l = new double[5];
				for (int i = 0; i < 5; i++)
					l[i] = Double.parseDouble(split[i + 1]);
				return new Location(w, l[0], l[1], l[2], (float) l[3], (float) l[4]);
			} catch (final NumberFormatException e) {
				return null;
			}
		}
		//</editor-fold>
	}

}
