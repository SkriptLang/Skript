/**
 * This file is part of Skript.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.yggdrasil;

import ch.njol.yggdrasil.Fields.FieldContext;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilRobustEnum;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilRobustSerializable;
import ch.njol.yggdrasil.xml.YggXMLInputStream;
import ch.njol.yggdrasil.xml.YggXMLOutputStream;
import org.eclipse.jdt.annotation.Nullable;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Yggdrasil is a simple data format to store object graphs.
 * <p>
 * Yggdrasil uses String IDs to identify classes, thus all classes to be (de)serialised have to be registered to Yggdrasil before doing anything (they can also be registered while
 * Yggdrasil is working, but you must make sure that all classes are registered in time when deserialising). A {@link ClassResolver} or {@link YggdrasilSerializer} can also be used
 * to find classes and IDs dynamically.
 * <p>
 * <b>Default behaviour</b>
 * <p>
 * A Java object can be serialised and deserialised if it is a primitive, a primitive wrapper, a String, an enum or {@link PseudoEnum} (both require an ID), or its class meets all
 * of the following requirements:
 * <ul>
 * <li>It implements {@link YggdrasilSerializable}
 * <li>It has an ID assigned to it (using the methods described above)
 * <li>It provides a nullary constructor (any access modifier) (in particular anonymous and non-static inner classes can't be serialised)
 * <li>All its non-transient and non-static fields are serialisable according to these requirements
 * </ul>
 * <p>
 * Yggdrasil will generate errors if an object loaded either has too many fields and/or is missing some in the stream.
 * <p>
 * <b>Customisation</b>
 * <p>
 * Any object that does not meet the above requirements for serialisation can still be (de)serialised using an {@link YggdrasilSerializer} (useful for objects of an external API),
 * or by implementing {@link YggdrasilExtendedSerializable}.
 * <p>
 * The behaviour in case of an invalid or outdated stream can be defined likewise, or one can implement {@link YggdrasilRobustSerializable} or {@link YggdrasilRobustEnum}
 * respectively.
 *
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation")
@NotThreadSafe
public final class Yggdrasil {

	/**
	 * Magic Number: "Ygg\0"
	 * <p>
	 * hex: 0x59676700
	 */
	public final static int MAGIC_NUMBER = ('Y' << 24) + ('g' << 16) + ('g' << 8) + '\0';

	/** latest protocol version */
	public final static short LATEST_VERSION = 1; // version 2 is only one minor change currently

	public final short version;

	private final List<ClassResolver> classResolvers = new ArrayList<>();
	private final List<FieldHandler> fieldHandlers = new ArrayList<>();

	private final SimpleClassResolver simpleClassResolver = new SimpleClassResolver();

	public Yggdrasil() {
		this(LATEST_VERSION);
	}

	public Yggdrasil(final short version) {
		if (version <= 0 || version > LATEST_VERSION)
			throw new YggdrasilException("Unsupported version number");
		this.version = version;
		classResolvers.add(new JRESerializer());
		classResolvers.add(simpleClassResolver);
	}

	public YggdrasilOutputStream newOutputStream(final OutputStream out) throws IOException {
		return new DefaultYggdrasilOutputStream(this, out);
	}

	public YggdrasilInputStream newInputStream(final InputStream in) throws IOException {
		return new DefaultYggdrasilInputStream(this, in);
	}

	@Deprecated
	public YggXMLOutputStream newXMLOutputStream(final OutputStream out) throws IOException {
		return new YggXMLOutputStream(this, out);
	}

	@Deprecated
	public YggdrasilInputStream newXMLInputStream(final InputStream in) throws IOException {
		return new YggXMLInputStream(this, in);
	}

	public void registerClassResolver(final ClassResolver r) {
		if (!classResolvers.contains(r))
			classResolvers.add(r);
	}

	public void registerSingleClass(final Class<?> c, final String id) {
		simpleClassResolver.registerClass(c, id);
	}

	/**
	 * Registers a class and uses its {@link YggdrasilID} as id.
	 */
	public void registerSingleClass(final Class<?> c) {
		final YggdrasilID id = c.getAnnotation(YggdrasilID.class);
		if (id == null)
			throw new IllegalArgumentException(c.toString());
		simpleClassResolver.registerClass(c, id.value());
	}

	public void registerFieldHandler(final FieldHandler h) {
		if (!fieldHandlers.contains(h))
			fieldHandlers.add(h);
	}

	public final boolean isSerializable(final Class<?> c) {
		try {
			return c.isPrimitive() || c == Object.class || (Enum.class.isAssignableFrom(c) || PseudoEnum.class.isAssignableFrom(c)) && getIDNoError(c) != null ||
				((YggdrasilSerializable.class.isAssignableFrom(c) || getSerializer(c) != null) && newInstance(c) != c);// whatever, just make true out if it (null is a valid return value)
		} catch (final
		StreamCorruptedException e) { // thrown by newInstance if the class does not provide a correct constructor or is abstract
			return false;
		} catch (final NotSerializableException e) {
			return false;
		}
	}

	@Nullable
	YggdrasilSerializer<?> getSerializer(final Class<?> c) {
		for (final ClassResolver r : classResolvers) {
			if (r instanceof YggdrasilSerializer && r.getID(c) != null)
				return (YggdrasilSerializer<?>) r;
		}
		return null;
	}

	public Class<?> getClass(final String id) throws StreamCorruptedException {
		if ("Object".equals(id))
			return Object.class;
		for (final ClassResolver r : classResolvers) {
			final Class<?> c = r.getClass(id);
			if (c != null) { // TODO error if not serialisable?
				assert Tag.byName(id) == null && (Tag.getType(c) == Tag.T_OBJECT || Tag.getType(c) == Tag.T_ENUM) : "Tag IDs should not be matched: " + id + " (class resolver: " + r + ")";
				assert id.equals(r.getID(c)) : r + " returned " + c + " for id " + id + ", but returns id " + r.getID(c) + " for that class";
				return c;
			}
		}
		throw new StreamCorruptedException("No class found for ID " + id);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Nullable
	private String getIDNoError(Class<?> c) {
		if (c == Object.class)
			return "Object";
		assert Tag.getType(c) == Tag.T_OBJECT || Tag.getType(c) == Tag.T_ENUM;
		if (Enum.class.isAssignableFrom(c) && c.getSuperclass() != Enum.class) {
			final Class<?> s = c.getSuperclass();
			assert s != null; // c cannot be Object.class
			c = s;
		}
		if (PseudoEnum.class.isAssignableFrom(c))
			c = PseudoEnum.getDeclaringClass((Class) c);
		for (final ClassResolver r : classResolvers) {
			final String id = r.getID(c);
			if (id != null) {
				assert Tag.byName(id) == null : "Class IDs should not match Tag IDs: " + id + " (class resolver: " + r + ")";
				final Class<?> c2 = r.getClass(id);
				assert c2 != null && (r instanceof YggdrasilSerializer ? id.equals(r.getID(c2)) : r.getClass(id) == c) : r + " returned id " + id + " for " + c + ", but returns " + c2 + " for that id";
				return id;
			}
		}
		return null;
	}

	public String getID(final Class<?> c) throws NotSerializableException {
		final String id = getIDNoError(c);
		if (id == null)
			throw new NotSerializableException("No ID found for " + c);
		if (!isSerializable(c))
			throw new NotSerializableException(c.getCanonicalName());
		return id;
	}

	/**
	 * Gets the ID of a field.
	 * <p>
	 * This method performs no checks on the given field.
	 *
	 * @param f
	 * @return The field's id as given by its {@link YggdrasilID} annotation, or its name if it's not annotated.
	 */
	public static String getID(final Field f) {
		final YggdrasilID yid = f.getAnnotation(YggdrasilID.class);
		if (yid != null) {
			return yid.value();
		}
		return "" + f.getName();
	}

	@SuppressWarnings("null")
	public static String getID(final Enum<?> e) {
		try {
			return getID(e.getDeclaringClass().getDeclaredField(e.name()));
		} catch (final NoSuchFieldException ex) {
			assert false : e;
			return "" + e.name();
		}
	}

	@SuppressWarnings({"unchecked", "null", "unused"})
	public static <T extends Enum<T>> Enum<T> getEnumConstant(final Class<T> c, final String id) throws StreamCorruptedException {
		final Field[] fields = c.getDeclaredFields();
		for (final Field f : fields) {
			assert f != null;
			if (getID(f).equals(id))
				return Enum.valueOf(c, f.getName());
		}
		if (YggdrasilRobustEnum.class.isAssignableFrom(c)) {
			final Object[] cs = c.getEnumConstants();
			if (cs.length == 0)
				throw new StreamCorruptedException(c + " does not have any enum constants");
			final Enum<?> e = ((YggdrasilRobustEnum) cs[0]).excessiveConstant(id);
			if (e == null)
				throw new YggdrasilException("YggdrasilRobustEnum " + c + " returned null from excessiveConstant(" + id + ")");
			if (!c.isInstance(e))
				throw new YggdrasilException(c + " returned a foreign enum constant: " + e.getClass() + "." + e);
			return (Enum<T>) e;
		}
		// TODO use field handlers/new enum handlers
		throw new StreamCorruptedException("Enum constant " + id + " does not exist in " + c);
	}

	public void excessiveField(final Object o, final FieldContext field) throws StreamCorruptedException {
		for (final FieldHandler h : fieldHandlers) {
			if (h.excessiveField(o, field))
				return;
		}
		throw new StreamCorruptedException("Excessive field " + field.id + " in class " + o.getClass().getCanonicalName() + " was not handled");
	}

	public void missingField(final Object o, final Field f) throws StreamCorruptedException {
		for (final FieldHandler h : fieldHandlers) {
			if (h.missingField(o, f))
				return;
		}
		throw new StreamCorruptedException("Missing field " + getID(f) + " in class " + o.getClass().getCanonicalName() + " was not handled");
	}

	public void incompatibleField(final Object o, final Field f, final FieldContext field) throws StreamCorruptedException {
		for (final FieldHandler h : fieldHandlers) {
			if (h.incompatibleField(o, f, field))
				return;
		}
		throw new StreamCorruptedException("Incompatible field " + getID(f) + " in class " + o.getClass().getCanonicalName() + " of incompatible " + field.getType() + " was not handled");
	}

	public void saveToFile(final Object o, final File f) throws IOException {
		FileOutputStream fout = null;
		YggdrasilOutputStream yout = null;
		try {
			fout = new FileOutputStream(f);
			yout = newOutputStream(fout);
			yout.writeObject(o);
			yout.flush();
		} finally {
			if (yout != null)
				yout.close();
			if (fout != null)
				fout.close();
		}
	}

	@Nullable
	public <T> T loadFromFile(final File f, final Class<T> expectedType) throws IOException {
		FileInputStream fin = null;
		YggdrasilInputStream yin = null;
		try {
			fin = new FileInputStream(f);
			yin = newInputStream(fin);
			return yin.readObject(expectedType);
		} finally {
			if (yin != null)
				yin.close();
			if (fin != null)
				fin.close();
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Nullable
	final Object newInstance(final Class<?> c) throws StreamCorruptedException, NotSerializableException {
		final YggdrasilSerializer s = getSerializer(c);
		if (s != null) {
			if (!s.canBeInstantiated(c)) { // only used by isSerializable - return null if OK, throw an YggdrasilException if not
				try {
					s.deserialize(c, new Fields(this));
				} catch (final StreamCorruptedException e) {
				}
				return null;
			}
			final Object o = s.newInstance(c);
			if (o == null)
				throw new YggdrasilException("YggdrasilSerializer " + s + " returned null from newInstance(" + c + ")");
			return o;
		}
		// try whether a nullary constructor exists
		try {
			final Constructor<?> constr = c.getDeclaredConstructor();
			constr.setAccessible(true);
			return constr.newInstance();
		} catch (final NoSuchMethodException e) {
			throw new StreamCorruptedException("Cannot create an instance of " + c + " because it has no nullary constructor");
		} catch (final SecurityException e) {
			throw new StreamCorruptedException("Cannot create an instance of " + c + " because the security manager didn't allow it");
		} catch (final InstantiationException e) {
			throw new StreamCorruptedException("Cannot create an instance of " + c + " because it is abstract");
		} catch (final IllegalAccessException e) {
			e.printStackTrace();
			assert false;
			return null;
		} catch (final IllegalArgumentException e) {
			e.printStackTrace();
			assert false;
			return null;
		} catch (final InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	// TODO command line, e.g. convert to XML
	public static void main(final String[] args) {
		System.err.println("Command line not supported yet");
		System.exit(1);
	}

}
