package ch.njol.yggdrasil;

import org.jetbrains.annotations.Nullable;

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
	public static final int MIN_PRIMITIVE = T_BYTE.tag;
	public static final int MAX_PRIMITIVE = T_BOOLEAN.tag;
	
	/** primitive tags are between these value */
	public static final int MIN_WRAPPER = T_BYTE_OBJ.tag;
	public static final int MAX_WRAPPER = T_BOOLEAN_OBJ.tag;
	
	public final byte tag;
	@Nullable
	public final Class<?> type;
	public final String name;
	
	Tag(int tag, @Nullable Class<?> type, String name) {
		assert 0 <= tag && tag <= 0xFF : tag;
		this.tag = (byte) tag;
		this.type = type;
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
	
	public Tag getWrapper() {
		if (!isPrimitive()) {
			assert false;
			return T_NULL;
		}
		return byID[tag - MIN_PRIMITIVE + MIN_WRAPPER];
	}
	
	private static final Map<Class<?>, Tag> types = new HashMap<>();
	private static final Tag[] byID = new Tag[256];
	private static final Map<String, Tag> byName = new HashMap<>();
	static {
		for (Tag tag : Tag.values()) {
			types.put(tag.type, tag);
			byID[tag.tag & 0xFF] = tag;
			byName.put(tag.name, tag);
		}
	}
	
	public static Tag getType(@Nullable Class<?> type) {
		if (type == null)
			return T_NULL;
		Tag t = types.get(type);
		if (t != null)
			return t;
		return type.isArray() ? T_ARRAY
				: Enum.class.isAssignableFrom(type) || PseudoEnum.class.isAssignableFrom(type) ? T_ENUM // isEnum() doesn't work for subclasses
				: T_OBJECT;
	}
	
	@Nullable
	public static Tag byID(byte tag) {
		return byID[tag & 0xFF];
	}
	
	@Nullable
	public static Tag byID(int tag) {
		return byID[tag];
	}
	
	@Nullable
	public static Tag byName(String name) {
		return byName.get(name);
	}
	
	private static final HashMap<Class<?>, Tag> wrapperTypes = new HashMap<>();
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
	
	public static boolean isWrapper(Class<?> type) {
		return wrapperTypes.containsKey(type);
	}
	
	public static Tag getPrimitiveFromWrapper(Class<?> wrapper) {
		Tag tag = wrapperTypes.get(wrapper);
		if (tag == null) {
			assert false : wrapper;
			return T_NULL;
		}
		return tag;
	}
	
	public static Class<?> getWrapperClass(Class<?> primitive) {
		assert primitive.isPrimitive();
		Tag tag = types.get(primitive);
		if (tag == null) {
			assert false : primitive;
			return Object.class;
		}
		Class<?> wrapper = tag.getWrapper().type;
		assert wrapper != null : tag;
		return wrapper;
	}
	
}
