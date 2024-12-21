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
package ch.njol.skript.util;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.localization.GeneralWords;
import ch.njol.skript.localization.Language;
import ch.njol.skript.localization.Message;
import ch.njol.skript.localization.Noun;
import ch.njol.util.Kleenean;
import ch.njol.yggdrasil.Fields.FieldContext;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilRobustSerializable;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.StreamCorruptedException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Locale;

/**
 * @author Peter Güttinger
 */
public class Direction implements YggdrasilRobustSerializable {
	
	/**
	 * A direction that doesn't point anywhere, i.e. equal to 'at'.
	 */
	public final static Direction ZERO = new Direction(new double[] {0, 0, 0});
	/**
	 * A direction that points in the direction of the object(s) passed to the various <tt>getDirection</tt> methods.
	 */
	public final static Direction IDENTITY = new Direction(0, 0, 1);
	
	public final static BlockFace BF_X = findFace(1, 0, 0), BF_Y = findFace(0, 1, 0), BF_Z = findFace(0, 0, 1);

	private static BlockFace findFace(final int x, final int y, final int z) {
		for (final BlockFace f : BlockFace.values()) {
			if (f.getModX() == x && f.getModY() == y && f.getModZ() == z)
				return f;
		}
		assert false;
		return BlockFace.SELF;
	}
	
	public final static Noun m_meter = new Noun("directions.meter");
	
	// rotation or offset - These would be in a union if this were written in C
	private final double pitchOrX, yawOrY, lengthOrZ;
	
	// whether this direction is rotational (i.e. depends on some object) or translational/directional (i.e. depends on the coordinate system but nothing else)
	private final boolean relative;
	
	public Direction(final double[] mod) {
		if (mod.length != 3)
			throw new IllegalArgumentException();
		relative = false;
		pitchOrX = mod[0];
		yawOrY = mod[1];
		lengthOrZ = mod[2];
	}
	
	/**
	 * Use this as pitch to force a horizontal direction
	 */
	public final static double IGNORE_PITCH = 0xF1A7; // FLAT
	
	public Direction() {
		this(0, 0, 0);
	}
	
	public Direction(final double pitch, final double yaw, final double length) {
		relative = true;
		pitchOrX = pitch;
		yawOrY = yaw;
		lengthOrZ = length;
	}
	
	public Direction(final BlockFace f, final double length) {
		this(new Vector(f.getModX(), f.getModY(), f.getModZ()).normalize().multiply(length));
	}
	
	public Direction(final Vector v) {
		relative = false;
		pitchOrX = v.getX();
		yawOrY = v.getY();
		lengthOrZ = v.getZ();
	}
	
	public Location getRelative(final Location l) {
		return l.clone().add(getDirection(l));
	}
	
	public Location getRelative(final Entity e) {
		return e.getLocation().add(getDirection(e.getLocation()));
	}
	
	public Location getRelative(final Block b) {
		return b.getLocation().add(getDirection(b));
	}

	/*
	 * Used to get a vector from a direction without anything to be relative to.
	 * Any relative directions will be relative to 0 degrees pitch and yaw.
	 */
	public Vector getDirection() {
		if (!relative)
			return new Vector(pitchOrX, yawOrY, lengthOrZ);
		return getDirection(0, 0);
	}

	public Vector getDirection(final Location l) {
		if (!relative)
			return new Vector(pitchOrX, yawOrY, lengthOrZ);
		return getDirection(pitchOrX == IGNORE_PITCH ? 0 : pitchToRadians(l.getPitch()), yawToRadians(l.getYaw()));
	}
	
	public Vector getDirection(final Entity e) {
		return getDirection(e.getLocation());
	}
	
	public Vector getDirection(Block b) {
		if (!relative)
			return new Vector(pitchOrX, yawOrY, lengthOrZ);
		BlockFace blockFace = getFacing(b);
		return getDirection(pitchOrX == IGNORE_PITCH ? 0 : blockFace.getModZ() * Math.PI / 2 /* only up and down have a z mod */, Math.atan2(blockFace.getModZ(), blockFace.getModX()));
	}
	
	private Vector getDirection(final double p, final double y) {
		if (pitchOrX == IGNORE_PITCH)
			return new Vector(Math.cos(y + yawOrY) * lengthOrZ, 0, Math.sin(y + yawOrY) * lengthOrZ);
		final double lxz = Math.cos(p + pitchOrX) * lengthOrZ;
		return new Vector(Math.cos(y + yawOrY) * lxz, Math.sin(p + pitchOrX) * Math.cos(yawOrY) * lengthOrZ, Math.sin(y + yawOrY) * lxz);
	}
	
	@Override
	public int hashCode() {
		return (relative ? 1 : -1) * Arrays.hashCode(new double[] {pitchOrX, yawOrY, lengthOrZ});
	}
	
	@Override
	public boolean equals(final @Nullable Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Direction))
			return false;
		final Direction other = (Direction) obj;
		return relative == other.relative && pitchOrX == other.pitchOrX && yawOrY == other.yawOrY && other.lengthOrZ == lengthOrZ;
	}
	
	/**
	 * @return Whether this Direction rotates the direction of a given object or only translates it.
	 */
	public boolean isRelative() {
		return relative;
	}
	
	/**
	 * @param pitch Notch-pitch
	 * @return Mathematical pitch oriented from x/z to y axis (with the origin in the x/z plane)
	 */
	public static double pitchToRadians(final float pitch) {
		return -Math.toRadians(pitch);
	}
	
	/**
	 * @param pitch Mathematical pitch oriented from x/z to y axis (with the origin in the x/z plane)
	 * @return Notch-pitch
	 */
	public static float getPitch(final double pitch) {
		return (float) Math.toDegrees(-pitch);
	}
	
	/**
	 * @param yaw Notch-yaw
	 * @return Mathematical yaw oriented from x to z axis (with the origin at the x axis)
	 */
	public static double yawToRadians(final float yaw) {
		return Math.toRadians(yaw) + Math.PI / 2;
	}
	
	/**
	 * @param yaw Mathematical yaw oriented from x to z axis (with the origin at the x axis)
	 * @return Notch-yaw
	 */
	public static float getYaw(final double yaw) {
		return (float) Math.toDegrees(yaw - Math.PI / 2);
	}
	
	/**
	 * @param b
	 * @return The facing of the block or {@link BlockFace#SELF} if the block doesn't have a facing.
	 */
	@SuppressWarnings("deprecation")
	public static BlockFace getFacing(Block b) {
		BlockData blockData = b.getBlockData();
		if (!(blockData instanceof org.bukkit.block.data.Directional))
			return BlockFace.SELF;
		return ((org.bukkit.block.data.Directional) blockData).getFacing();
	}
	
	public static BlockFace getFacing(final double yaw, final double pitch) {
		if (-Math.PI / 4 < pitch && pitch < Math.PI / 4) {
			if (yaw < Math.PI / 4 || yaw >= Math.PI * 7 / 4)
				return BF_X;
			if (yaw < Math.PI * 3 / 4)
				return BF_Z;
			if (yaw < Math.PI * 5 / 4)
				return BF_X.getOppositeFace();
			assert yaw < Math.PI * 7 / 4;
			return BF_Z.getOppositeFace();
		}
		if (pitch > 0)
			return BlockFace.UP;
		return BlockFace.DOWN;
	}
	
	public static BlockFace getFacing(final Location l, final boolean horizontal) {
		final double yaw = (yawToRadians(l.getYaw()) + 2 * Math.PI) % (2 * Math.PI);
		final double pitch = horizontal ? 0 : pitchToRadians(l.getPitch());
		return getFacing(yaw, pitch);
	}
	
	public static BlockFace getFacing(final Vector v, final boolean horizontal) {
		final double pitch = horizontal ? 0 : Math.atan2(v.getY(), Math.sqrt(Math.pow(v.getX(), 2) + Math.pow(v.getZ(), 2)));
		final double yaw = Math.atan2(v.getZ(), v.getX());
		return getFacing(yaw, pitch);
	}
	
	@SuppressWarnings("null")
	public static Location[] getRelatives(final Block[] blocks, final Direction[] directions) {
		final Location[] r = new Location[blocks.length * directions.length];
		if (r.length == 0)
			return r;
		for (int i = 0; i < blocks.length; i++) {
			r[i] = blocks[i].getLocation();
			for (int j = 0; j < directions.length; j++) {
				r[i].add(directions[j].getDirection(blocks[i]));
			}
		}
		return r;
	}
	
	@SuppressWarnings("null")
	public static Location[] getRelatives(final Location[] locations, final Direction[] directions) {
		final Location[] r = new Location[locations.length * directions.length];
		if (r.length == 0)
			return r;
		for (int i = 0; i < locations.length; i++) {
			r[i] = locations[i].clone();
			for (int j = 0; j < directions.length; j++) {
				r[i].add(directions[j].getDirection(locations[i]));
			}
		}
		return r;
	}
	
	@Override
	public String toString() {
		return relative ? toString(pitchOrX == IGNORE_PITCH ? 0 : pitchOrX, yawOrY, lengthOrZ) : toString(new double[] {pitchOrX, yawOrY, lengthOrZ});
	}
	
	public static String toString(final double pitch, final double yaw, final double length) {
		final double front = Math.cos(pitch) * Math.cos(yaw) * length;
		final double left = Math.cos(pitch) * Math.sin(yaw) * length;
		final double above = Math.sin(pitch) * length;
		return toString(new double[] {front, left, above}, relativeDirections);
	}
	
	private final static Message m_at = new Message("directions.at");
	private final static Message[] absoluteDirections = new Message[6];
	private final static Message[] relativeDirections = new Message[6];
	static {
		final String[] rd = {"front", "behind", "left", "right", "above", "below"};
		for (int i = 0; i < rd.length; i++) {
			relativeDirections[i] = new Message("directions." + rd[i]);
		}
		final String[] ad = {
				BF_X.name().toLowerCase(Locale.ENGLISH), BF_X.getOppositeFace().name().toLowerCase(Locale.ENGLISH),
				BF_Y.name().toLowerCase(Locale.ENGLISH), BF_Y.getOppositeFace().name().toLowerCase(Locale.ENGLISH),
				BF_Z.name().toLowerCase(Locale.ENGLISH), BF_Z.getOppositeFace().name().toLowerCase(Locale.ENGLISH)};
		for (int i = 0; i < ad.length; i++) {
			absoluteDirections[i] = new Message("directions." + ad[i]);
		}
	}
	
	public static String toString(final double[] mod) {
		if (mod[0] == 0 && mod[1] == 0 && mod[2] == 0)
			return m_at.toString();
		return toString(mod, absoluteDirections);
	}
	
	public static String toString(final Vector dir) {
		if (dir.getX() == 0 && dir.getY() == 0 && dir.getZ() == 0)
			return Language.get("directions.at");
		return toString(new double[] {dir.getX(), dir.getY(), dir.getZ()}, absoluteDirections);
	}
	
	@SuppressWarnings("null")
	private static String toString(final double[] mod, final Message[] names) {
		assert mod.length == 3 && names.length == 6;
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < 3; i++) {
			toString(b, mod[i], names[2 * i], names[2 * i + 1], b.length() != 0);
		}
		return b.toString();
	}
	
	private static void toString(final StringBuilder b, final double d, final Message direction, final Message oppositeDirection, final boolean prependAnd) {
		if (d == 0)
			return;
		if (prependAnd)
			b.append(" ").append(GeneralWords.and).append(" ");
		if (d != 1 && d != -1) {
			b.append(m_meter.withAmount(Math.abs(d)));
			b.append(" ");
		}
		b.append(d > 0 ? direction : oppositeDirection);
	}
	
//		return "" + relative + ":" + (relative ? pitch + "," + yaw + "," + length : mod[0] + "," + mod[1] + "," + mod[2]);
	@Deprecated
	@Nullable
	public static Direction deserialize(final String s) {
		final String[] split = s.split(":");
		if (split.length != 2)
			return null;
		final boolean relative = Boolean.parseBoolean(split[0]);
		if (relative) {
			final String[] split2 = split[1].split(",");
			if (split2.length != 3)
				return null;
			try {
				return new Direction(Double.parseDouble(split2[0]), Double.parseDouble(split2[1]), Double.parseDouble(split2[2]));
			} catch (final NumberFormatException e) {
				return null;
			}
		} else {
			final String[] split2 = split[1].split(",");
			if (split2.length != 3)
				return null;
			try {
				return new Direction(new double[] {Double.parseDouble(split2[0]), Double.parseDouble(split2[1]), Double.parseDouble(split2[2])});
			} catch (final NumberFormatException e) {
				return null;
			}
		}
	}
	
	public static Expression<Location> combine(final Expression<? extends Direction> dirs, final Expression<? extends Location> locs) {
		return new SimpleExpression<Location>() {
			@SuppressWarnings("null")
			@Override
			protected Location[] get(final Event e) {
				final Direction[] ds = dirs.getArray(e);
				final Location[] ls = locs.getArray(e);
				final Location[] r = ls; //ds.length == 1 ? ls : new Location[ds.length * ls.length];
				for (int i = 0; i < ds.length; i++) {
					for (int j = 0; j < ls.length; j++) {
//						r[i + j * ds.length] = ds[i].getRelative(ls[j]);
						r[j] = ds[i].getRelative(r[j]);
					}
				}
				return r;
			}
			
			@SuppressWarnings("null")
			@Override
			public Location[] getAll(final Event e) {
				final Direction[] ds = dirs.getAll(e);
				final Location[] ls = locs.getAll(e);
				final Location[] r = ls; //ds.length == 1 ? ls : new Location[ds.length * ls.length];
				for (int i = 0; i < ds.length; i++) {
					for (int j = 0; j < ls.length; j++) {
//						r[i + j * ds.length] = ds[i].getRelative(ls[j]);
						r[j] = ds[i].getRelative(r[j]);
					}
				}
				return r;
			}
			
			@Override
			public boolean getAnd() {
//				return (dirs.isSingle() || dirs.getAnd()) && (locs.isSingle() || locs.getAnd());
				return locs.getAnd();
			}
			
			@Override
			public boolean isSingle() {
//				return dirs.isSingle() && locs.isSingle();
				return locs.isSingle();
			}
			
			@Override
			public Class<? extends Location> getReturnType() {
				return Location.class;
			}
			
			@Override
			public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public String toString(final @Nullable Event e, final boolean debug) {
				return dirs.toString(e, debug) + " " + locs.toString(e, debug);
			}
			
			@Override
			public Expression<? extends Location> simplify() {
				if (dirs instanceof Literal && dirs.isSingle() && Direction.ZERO.equals(((Literal<?>) dirs).getSingle())) {
					return locs;
				}
				return this;
			}
		};
	}
	
	@Override
	public boolean incompatibleField(@NotNull final Field f, @NotNull final FieldContext value) throws StreamCorruptedException {
		return false;
	}
	
	private void set(final String field, final @Nullable Double value) throws StreamCorruptedException {
		if (value == null)
			throw new StreamCorruptedException();
		try {
			final Field f = Direction.class.getDeclaredField(field);
			f.setAccessible(true); // required for final fields
			f.set(this, value);
		} catch (final IllegalArgumentException e) {
			assert false : e;
		} catch (final IllegalAccessException e) {
			assert false : e;
		} catch (final NoSuchFieldException e) {
			assert false : e;
		}
	}
	
	@Override
	public boolean excessiveField(@NotNull final FieldContext field) throws StreamCorruptedException {
		if (field.getID().equals("mod")) {
			final double[] mod = field.getObject(double[].class);
			if (mod == null)
				return true;
			if (mod.length != 3)
				throw new StreamCorruptedException();
			set("pitchOrX", mod[0]);
			set("yawOrY", mod[1]);
			set("lengthOrZ", mod[1]);
			return true;
		} else if (field.getID().equals("pitch")) {
			set("pitchOrX", field.getPrimitive(double.class));
			return true;
		} else if (field.getID().equals("yaw")) {
			set("yawOrY", field.getPrimitive(double.class));
			return true;
		} else if (field.getID().equals("length")) {
			set("lengthOrZ", field.getPrimitive(double.class));
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean missingField(@NotNull final Field field) throws StreamCorruptedException {
		if (!field.getName().equals("relative"))
			return true;
		return false;
	}
	
}
