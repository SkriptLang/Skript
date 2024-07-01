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
package ch.njol.yggdrasil;

import ch.njol.yggdrasil.Fields.FieldContext;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilRobustEnum;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilRobustSerializable;
import org.eclipse.jdt.annotation.Nullable;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Yggdrasil is a simple data format to store object graphs.
 * <p>
 * Yggdrasil uses String IDs to identify classes, thus all classes to be (de)serialized have to be registered to
 * Yggdrasil before doing anything (they can also be registered while Yggdrasil is working, but you must make sure
 * that all classes are registered in time when deserializing). A {@link ClassResolver} or
 * {@link YggdrasilSerializer} can also be used to find classes and IDs dynamically.
 * <p>
 * <b>Default behaviour</b>
 * <p>
 * A Java object can be serialized and deserialized if it is a primitive, a primitive wrapper, a String, an enum or
 * {@link PseudoEnum} (both require an ID), or its class meets all the following requirements:
 * <ul>
 * <li>It implements {@link YggdrasilSerializable}
 * <li>It has an ID assigned to it (using the methods described above)
 * <li>It provides a nullary constructor (any access modifier) (in particular anonymous and non-static inner classes can't be serialized)
 * <li>All its non-transient and non-static fields are serializable according to these requirements
 * </ul>
 * <p>
 * Yggdrasil will generate errors if an object loaded either has too many fields and/or is missing some in the stream.
 * <p>
 * <b>Customisation</b>
 * <p>
 * Any object that does not meet the above requirements for serialisation can still be (de)serialized using an
 * {@link YggdrasilSerializer} (useful for objects of an external API), or by implementing {@link YggdrasilExtendedSerializable}.
 * <p>
 * The behaviour in case of an invalid or outdated stream can be defined likewise, or one can
 * implement {@link YggdrasilRobustSerializable} or {@link YggdrasilRobustEnum} respectively.
 */
@NotThreadSafe
public final class Yggdrasil {
	
	/**
	 * Magic Number: "Ygg\0" (('Y' << 24) + ('g' << 16) + ('g' << 8) + '\0')
	 * <p>
	 * hex: 0x59676700
	 */
	public static final int MAGIC_NUMBER = 0x59676700;
	
	/** latest protocol version */
	public static final short LATEST_VERSION = 1; // version 2 is only one minor change currently
	
	public final short version;
	
	private final List<ClassResolver> classResolvers = new ArrayList<>();
	private final List<FieldHandler> fieldHandlers = new ArrayList<>();
	
	private final SimpleClassResolver simpleClassResolver = new SimpleClassResolver();
	
	public Yggdrasil() {
		this(LATEST_VERSION);
	}
	
	public Yggdrasil(short version) {
		if (version <= 0 || version > LATEST_VERSION)
			throw new YggdrasilException("Unsupported version number");
		this.version = version;
		classResolvers.add(new JRESerializer());
		classResolvers.add(simpleClassResolver);
	}
	
	public YggdrasilOutputStream newOutputStream(OutputStream out) throws IOException {
		return new DefaultYggdrasilOutputStream(this, out);
	}
	
	public YggdrasilInputStream newInputStream(InputStream in) throws IOException {
		return new DefaultYggdrasilInputStream(this, in);
	}
	
	public void registerClassResolver(ClassResolver resolver) {
		if (!classResolvers.contains(resolver))
			classResolvers.add(resolver);
	}
	
	public void registerSingleClass(Class<?> type, String id) {
		simpleClassResolver.registerClass(type, id);
	}
	
	/**
	 * Registers a class and uses its {@link YggdrasilID} as id.
	 */
	public void registerSingleClass(Class<?> type) {
		YggdrasilID id = type.getAnnotation(YggdrasilID.class);
		if (id == null)
			throw new IllegalArgumentException(type.toString());
		simpleClassResolver.registerClass(type, id.value());
	}
	
	public void registerFieldHandler(FieldHandler handler) {
		if (!fieldHandlers.contains(handler))
			fieldHandlers.add(handler);
	}
	
	public boolean isSerializable(Class<?> type) {
		try {
			return type.isPrimitive() || type == Object.class || (Enum.class.isAssignableFrom(type) ||
					PseudoEnum.class.isAssignableFrom(type)) && getIDNoError(type) != null ||
					((YggdrasilSerializable.class.isAssignableFrom(type) || getSerializer(type) != null) && newInstance(type) != type); // whatever, just make true out if it (null is a valid return value)
		} catch (StreamCorruptedException e) { // thrown by newInstance if the class does not provide a correct constructor or is abstract
			return false;
		} catch (NotSerializableException e) {
			return false;
		}
	}
	
	@Nullable
	YggdrasilSerializer<?> getSerializer(Class<?> type) {
		for (ClassResolver resolver : classResolvers) {
			if (resolver instanceof YggdrasilSerializer && resolver.getID(type) != null)
				return (YggdrasilSerializer<?>) resolver;
		}
		return null;
	}
	
	public Class<?> getClass(String id) throws StreamCorruptedException {
		if ("Object".equals(id))
			return Object.class;
		for (ClassResolver resolver : classResolvers) {
			Class<?> type = resolver.getClass(id);
			if (type != null) { // TODO error if not serializable?
				assert Tag.byName(id) == null && (Tag.getType(type) == Tag.T_OBJECT || Tag.getType(type) == Tag.T_ENUM) : "Tag IDs should not be matched: " + id + " (class resolver: " + resolver + ")";
				assert id.equals(resolver.getID(type)) : resolver + " returned " + type + " for id " + id + ", but returns id " + resolver.getID(type) + " for that class";
				return type;
			}
		}
		throw new StreamCorruptedException("No class found for ID " + id);
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Nullable
	private String getIDNoError(Class<?> type) {
		if (type == Object.class)
			return "Object";
		assert Tag.getType(type) == Tag.T_OBJECT || Tag.getType(type) == Tag.T_ENUM;
		if (Enum.class.isAssignableFrom(type) && type.getSuperclass() != Enum.class) {
			Class<?> s = type.getSuperclass();
			assert s != null; // type cannot be Object.class
			type = s;
		}
		if (PseudoEnum.class.isAssignableFrom(type))
			type = PseudoEnum.getDeclaringClass((Class) type);
		for (ClassResolver resolver : classResolvers) {
			String id = resolver.getID(type);
			if (id != null) {
				assert Tag.byName(id) == null : "Class IDs should not match Tag IDs: " + id + " (class resolver: " + resolver + ")";
				Class<?> c2 = resolver.getClass(id);
				assert c2 != null && (resolver instanceof YggdrasilSerializer ? id.equals(resolver.getID(c2)) : resolver.getClass(id) == type) : resolver + " returned id " + id + " for " + type + ", but returns " + c2 + " for that id";
				return id;
			}
		}
		return null;
	}
	
	public String getID(Class<?> type) throws NotSerializableException {
		String id = getIDNoError(type);
		if (id == null)
			throw new NotSerializableException("No ID found for " + type);
		if (!isSerializable(type))
			throw new NotSerializableException(type.getCanonicalName());
		return id;
	}
	
	/**
	 * Gets the ID of a field.
	 * <p>
	 * This method performs no checks on the given field.
	 *
	 * @return The field's id as given by its {@link YggdrasilID} annotation, or its name if it's not annotated.
	 */
	public static String getID(Field field) {
		YggdrasilID yid = field.getAnnotation(YggdrasilID.class);
		if (yid != null) {
			return yid.value();
		}
		return "" + field.getName();
	}
	
	public static String getID(Enum<?> e) {
		try {
			return getID(e.getDeclaringClass().getDeclaredField(e.name()));
		} catch (NoSuchFieldException ex) {
			assert false : e;
			return "" + e.name();
		}
	}
	
	@SuppressWarnings({"unchecked", "unused"})
	public static <T extends Enum<T>> Enum<T> getEnumConstant(Class<T> type, String id) throws StreamCorruptedException {
		Field[] fields = type.getDeclaredFields();
		for (Field field : fields) {
			assert field != null;
			if (getID(field).equals(id))
				return Enum.valueOf(type, field.getName());
		}
		if (YggdrasilRobustEnum.class.isAssignableFrom(type)) {
			Object[] constants = type.getEnumConstants();
			if (constants.length == 0)
				throw new StreamCorruptedException(type + " does not have any enum constants");
			Enum<?> e = ((YggdrasilRobustEnum) constants[0]).excessiveConstant(id);
			if (!type.isInstance(e))
				throw new YggdrasilException(type + " returned a foreign enum constant: " + e.getClass() + "." + e);
			return (Enum<T>) e;
		}
		// TODO use field handlers/new enum handlers
		throw new StreamCorruptedException("Enum constant " + id + " does not exist in " + type);
	}
	
	public void excessiveField(Object object, FieldContext field) throws StreamCorruptedException {
		for (FieldHandler handler : fieldHandlers) {
			if (handler.excessiveField(object, field))
				return;
		}
		throw new StreamCorruptedException("Excessive field " + field.id + " in class " + object.getClass().getCanonicalName() + " was not handled");
	}
	
	public void missingField(Object object, Field field) throws StreamCorruptedException {
		for (FieldHandler handler : fieldHandlers) {
			if (handler.missingField(object, field))
				return;
		}
		throw new StreamCorruptedException("Missing field " + getID(field) + " in class " + object.getClass().getCanonicalName() + " was not handled");
	}
	
	public void incompatibleField(Object object, Field field, FieldContext context) throws StreamCorruptedException {
		for (FieldHandler handler : fieldHandlers) {
			if (handler.incompatibleField(object, field, context))
				return;
		}
		throw new StreamCorruptedException("Incompatible field " + getID(field) + " in class " + object.getClass().getCanonicalName() + " of incompatible " + context.getType() + " was not handled");
	}
	
	public void saveToFile(Object object, File file) throws IOException {
		try (
			FileOutputStream fout = new FileOutputStream(file);
			YggdrasilOutputStream yout = newOutputStream(fout)
		) {
			yout.writeObject(object);
			yout.flush();
		}
	}
	
	@Nullable
	public <T> T loadFromFile(File file, Class<T> expectedType) throws IOException {
		try (
			FileInputStream fin = new FileInputStream(file);
			YggdrasilInputStream yin = newInputStream(fin)
		) {
			return yin.readObject(expectedType);
		}
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Nullable
	Object newInstance(Class<?> type) throws StreamCorruptedException, NotSerializableException {
		YggdrasilSerializer serializer = getSerializer(type);
		if (serializer != null) {
			if (!serializer.canBeInstantiated(type)) { // only used by isSerializable - return null if OK, throw an YggdrasilException if not
				try {
					serializer.deserialize(type, new Fields(this));
				} catch (StreamCorruptedException ignored) {}
				return null;
			}
			Object o = serializer.newInstance(type);
			if (o == null)
				throw new YggdrasilException("YggdrasilSerializer " + serializer + " returned null from newInstance(" + type + ")");
			return o;
		}
		// try whether a nullary constructor exists
		try {
			Constructor<?> constr = type.getDeclaredConstructor();
			constr.setAccessible(true);
			return constr.newInstance();
		} catch (NoSuchMethodException e) {
			throw new StreamCorruptedException("Cannot create an instance of " + type + " because it has no nullary constructor");
		} catch (SecurityException e) {
			throw new StreamCorruptedException("Cannot create an instance of " + type + " because the security manager didn't allow it");
		} catch (InstantiationException e) {
			throw new StreamCorruptedException("Cannot create an instance of " + type + " because it is abstract");
		} catch (IllegalAccessException | IllegalArgumentException e) {
			e.printStackTrace();
			assert false;
			return null;
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
}
