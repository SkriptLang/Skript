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

import org.eclipse.jdt.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum Tag {
	/** the null reference */
	T_NULL(0x0, null, "null"),

	/** primitive types */
	T_BYTE(0x1, byte.class, "byte"), T_SHORT(0x2, short.class, "short"), T_INT(0x3, int.class, "int"), T_LONG(0x4, long.class, "long"),
	T_FLOAT(0x8, float.class, "float"), T_DOUBLE(0x9, double.class, "double"),
	T_CHAR(0xe, char.class, "char"), T_BOOLEAN(0xf, boolean.class, "boolean"),

	/** wrapper types */
	T_BYTE_OBJ(0x10 + T_BYTE.tag, Byte.class, "Byte"), T_SHORT_OBJ(0x10 + T_SHORT.tag, Short.class, "Short"), T_INT_OBJ(0x10 + T_INT.tag, Integer.class, "Integer"), T_LONG_OBJ(0x10 + T_LONG.tag, Long.class, "Long"),
	T_FLOAT_OBJ(0x10 + T_FLOAT.tag, Float.class, "Float"), T_DOUBLE_OBJ(0x10 + T_DOUBLE.tag, Double.class, "Double"),
	T_CHAR_OBJ(0x10 + T_CHAR.tag, Character.class, "Character"), T_BOOLEAN_OBJ(0x10 + T_BOOLEAN.tag, Boolean.class, "Boolean"),

	/** saved as UTF-8 */
	T_STRING(0x20, String.class, "string"),

	/** arrays */
	T_ARRAY(0x30, null, "array"),

	/** enum constants & class singletons */
	T_ENUM(0x40, null, "enum"), T_CLASS(0x41, Class.class, "class"),

	/** a generic object */
	T_OBJECT(0x80, Object.class, "object"),

	/** must always be 0xFF (check uses) */
	T_REFERENCE(0xFF, null, "reference");

	/** primitive tags are between these value */
	public final static int MIN_PRIMITIVE = T_BYTE.tag, MAX_PRIMITIVE = T_BOOLEAN.tag;

	/** primitive tags are between these value */
	public final static int MIN_WRAPPER = T_BYTE_OBJ.tag, MAX_WRAPPER = T_BOOLEAN_OBJ.tag;

	public final byte tag;
	@Nullable
	public final Class<?> c;
	public final String name;

	private Tag(final int tag, final @Nullable Class<?> c, final String name) {
		assert 0 <= tag && tag <= 0xFF : tag;
		this.tag = (byte) tag;
		this.c = c;
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public boolean isPrimitive() {
		return MIN_PRIMITIVE <= tag && tag <= MAX_PRIMITIVE;
	}

	@Nullable
	public Tag getPrimitive() {
		if (!isWrapper()) {
			assert false;
			return null;
		}
		return byID[tag - MIN_WRAPPER + MIN_PRIMITIVE];
	}

	public boolean isWrapper() {
		return MIN_WRAPPER <= tag && tag <= MAX_WRAPPER;
	}

	@SuppressWarnings("null")
	public Tag getWrapper() {
		if (!isPrimitive()) {
			assert false;
			return T_NULL;
		}
		return byID[tag - MIN_PRIMITIVE + MIN_WRAPPER];
	}

	private final static Map<Class<?>, Tag> types = new HashMap<>();
	private final static Tag[] byID = new Tag[256];
	private final static Map<String, Tag> byName = new HashMap<>();

	static {
		for (final Tag t : Tag.values()) {
			types.put(t.c, t);
			byID[t.tag & 0xFF] = t;
			byName.put(t.name, t);
		}
	}

	public static Tag getType(final @Nullable Class<?> c) {
		if (c == null)
			return T_NULL;
		final Tag t = types.get(c);
		if (t != null)
			return t;
		return c.isArray() ? T_ARRAY
			: Enum.class.isAssignableFrom(c) || PseudoEnum.class.isAssignableFrom(c) ? T_ENUM // isEnum() doesn't work for subclasses
			: T_OBJECT;
	}

	@Nullable
	public static Tag byID(final byte tag) {
		return byID[tag & 0xFF];
	}

	@Nullable
	public static Tag byID(final int tag) {
		return byID[tag];
	}

	@Nullable
	public static Tag byName(final String name) {
		return byName.get(name);
	}

	private final static HashMap<Class<?>, Tag> wrapperTypes = new HashMap<>();

	static {
		wrapperTypes.put(Byte.class, T_BYTE);
		wrapperTypes.put(Short.class, T_SHORT);
		wrapperTypes.put(Integer.class, T_INT);
		wrapperTypes.put(Long.class, T_LONG);
		wrapperTypes.put(Float.class, T_FLOAT);
		wrapperTypes.put(Double.class, T_DOUBLE);
		wrapperTypes.put(Character.class, T_CHAR);
		wrapperTypes.put(Boolean.class, T_BOOLEAN);
	}

	public static boolean isWrapper(final Class<?> c) {
		return wrapperTypes.containsKey(c);
	}

	public static Tag getPrimitiveFromWrapper(final Class<?> wrapper) {
		final Tag t = wrapperTypes.get(wrapper);
		if (t == null) {
			assert false : wrapper;
			return T_NULL;
		}
		return t;
	}

	public static Class<?> getWrapperClass(final Class<?> primitive) {
		assert primitive.isPrimitive();
		final Tag t = types.get(primitive);
		if (t == null) {
			assert false : primitive;
			return Object.class;
		}
		final Class<?> wrapper = t.getWrapper().c;
		assert wrapper != null : t;
		return wrapper;
	}

}
