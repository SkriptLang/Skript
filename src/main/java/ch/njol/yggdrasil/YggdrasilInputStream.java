package ch.njol.yggdrasil;

import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static ch.njol.yggdrasil.Tag.T_NULL;
import static ch.njol.yggdrasil.Tag.T_REFERENCE;
import static ch.njol.yggdrasil.Tag.getType;

public abstract class YggdrasilInputStream implements Closeable {
	
	protected final Yggdrasil yggdrasil;
	
	protected YggdrasilInputStream(Yggdrasil yggdrasil) {
		this.yggdrasil = yggdrasil;
	}
	
	protected abstract Tag readTag() throws IOException;
	
	protected abstract Object readPrimitive(Tag type) throws IOException;
	
	protected abstract Object readPrimitive_(Tag type) throws IOException;
	
	protected abstract String readString() throws IOException;
	
	protected abstract Class<?> readArrayComponentType() throws IOException;
	
	protected abstract int readArrayLength() throws IOException;
	
	private void readArrayContents(Object array) throws IOException {
		if (array.getClass().getComponentType().isPrimitive()) {
			int length = Array.getLength(array);
			Tag type = getType(array.getClass().getComponentType());
			for (int i = 0; i < length; i++)
				Array.set(array, i, readPrimitive_(type));
		} else {
			for (int i = 0; i < ((Object[]) array).length; i++)
				((Object[]) array)[i] = readObject();
		}
	}
	
	protected abstract Class<?> readEnumType() throws IOException;
	
	protected abstract String readEnumID() throws IOException;
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	private Object readEnum() throws IOException {
		Class<?> c = readEnumType();
		String id = readEnumID();
		if (Enum.class.isAssignableFrom(c)) {
			return Yggdrasil.getEnumConstant((Class) c, id);
		} else if (PseudoEnum.class.isAssignableFrom(c)) {
			Object o = PseudoEnum.valueOf((Class) c, id);
			if (o != null)
				return o;
//			if (YggdrasilRobustPseudoEnum.class.isAssignableFrom(c)) {
//				TODO create this and a handler (for Enums as well)
//			}
			throw new StreamCorruptedException("Enum constant " + id + " does not exist in " + c);
		} else {
			throw new StreamCorruptedException(c + " is not an enum type");
		}
	}
	
	protected abstract Class<?> readClass() throws IOException;
	
	protected abstract int readReference() throws IOException;
	
	protected abstract Class<?> readObjectType() throws IOException;
	
	protected abstract short readNumFields() throws IOException;
	
	protected abstract String readFieldID() throws IOException;
	
	private Fields readFields() throws IOException {
		Fields fields = new Fields(yggdrasil);
		short numFields = readNumFields();
		for (int i = 0; i < numFields; i++) {
			String id = readFieldID();
			Tag tag = readTag();
			if (tag.isPrimitive())
				fields.putPrimitive(id, readPrimitive(tag));
			else
				fields.putObject(id, readObject(tag));
		}
		return fields;
	}
	
	private final List<Object> readObjects = new ArrayList<>();
	
	@Nullable
	public final Object readObject() throws IOException {
		Tag tag = readTag();
		return readObject(tag);
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public final <T> T readObject(Class<T> expectedType) throws IOException {
		Tag tag = readTag();
		Object object = readObject(tag);
		if (object != null && !expectedType.isInstance(object))
			throw new StreamCorruptedException("Object " + object + " is of " + object.getClass() + " but expected " + expectedType);
		return (T) object;
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Nullable
	private Object readObject(Tag tag) throws IOException {
		if (tag == T_NULL)
			return null;
		if (tag == T_REFERENCE) {
			int ref = readReference();
			if (ref < 0 || ref >= readObjects.size())
				throw new StreamCorruptedException("Invalid reference " + ref + ", " + readObjects.size() + " object(s) read so far");
			Object object = readObjects.get(ref);
			if (object == null)
				throw new StreamCorruptedException("Reference to uninstantiable object: " + ref);
			return object;
		}
		Object object;
		switch (tag) {
			case T_ARRAY: {
				Class<?> type = readArrayComponentType();
				object = Array.newInstance(type, readArrayLength());
				readObjects.add(object);
				readArrayContents(object);
				return object;
			}
			case T_CLASS:
				object = readClass();
				break;
			case T_ENUM:
				object = readEnum();
				break;
			case T_STRING:
				object = readString();
				break;
			case T_OBJECT: {
				Class<?> c = readObjectType();
				YggdrasilSerializer s = yggdrasil.getSerializer(c);
				if (s != null && !s.canBeInstantiated(c)) {
					int ref = readObjects.size();
					readObjects.add(null);
					Fields fields = readFields();
					object = s.deserialize(c, fields);
					if (object == null)
						throw new YggdrasilException("YggdrasilSerializer " + s + " returned null from deserialize(" + c + "," + fields + ")");
					readObjects.set(ref, object);
				} else {
					object = yggdrasil.newInstance(c);
					if (object == null)
						throw new StreamCorruptedException();
					readObjects.add(object);
					Fields fields = readFields();
					if (s != null) {
						s.deserialize(object, fields);
					} else if (object instanceof YggdrasilExtendedSerializable) {
						((YggdrasilExtendedSerializable) object).deserialize(fields);
					} else {
						fields.setFields(object);
					}
				}
				return object;
			}
			case T_BOOLEAN_OBJ:
			case T_BYTE_OBJ:
			case T_CHAR_OBJ:
			case T_DOUBLE_OBJ:
			case T_FLOAT_OBJ:
			case T_INT_OBJ:
			case T_LONG_OBJ:
			case T_SHORT_OBJ:
				Tag primitive = tag.getPrimitive();
				assert primitive != null;
				object = readPrimitive(primitive);
				break;
			case T_BYTE:
			case T_BOOLEAN:
			case T_CHAR:
			case T_DOUBLE:
			case T_FLOAT:
			case T_INT:
			case T_LONG:
			case T_SHORT:
				throw new StreamCorruptedException();
			default:
				assert false;
				throw new StreamCorruptedException();
		}
		readObjects.add(object);
		return object;
	}
	
}
