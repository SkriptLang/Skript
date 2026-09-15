package ch.njol.yggdrasil;

import ch.njol.yggdrasil.Fields.FieldContext;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.NotSerializableException;
import java.lang.reflect.Array;
import java.util.IdentityHashMap;

import static ch.njol.yggdrasil.Tag.T_ARRAY;
import static ch.njol.yggdrasil.Tag.T_CLASS;
import static ch.njol.yggdrasil.Tag.T_ENUM;
import static ch.njol.yggdrasil.Tag.T_NULL;
import static ch.njol.yggdrasil.Tag.T_OBJECT;
import static ch.njol.yggdrasil.Tag.T_REFERENCE;
import static ch.njol.yggdrasil.Tag.T_STRING;
import static ch.njol.yggdrasil.Tag.getType;

public abstract class YggdrasilOutputStream implements Flushable, Closeable {
	
	protected final Yggdrasil yggdrasil;
	
	protected YggdrasilOutputStream(Yggdrasil yggdrasil) {
		this.yggdrasil = yggdrasil;
	}
	
	protected abstract void writeTag(Tag tag) throws IOException;
	
	private void writeNull() throws IOException {
		writeTag(T_NULL);
	}
	
	protected abstract void writePrimitiveValue(Object object) throws IOException;
	
	protected abstract void writePrimitive_(Object object) throws IOException;
	
	private void writePrimitive(Object object) throws IOException {
		Tag tag = Tag.getType(object.getClass());
		assert tag.isWrapper();
		Tag primitive = tag.getPrimitive();
		assert primitive != null;
		writeTag(primitive);
		writePrimitiveValue(object);
	}
	
	private void writeWrappedPrimitive(Object object) throws IOException {
		Tag tag = Tag.getType(object.getClass());
		assert tag.isWrapper();
		writeTag(tag);
		writePrimitiveValue(object);
	}
	
	protected abstract void writeStringValue(String string) throws IOException;
	
	private void writeString(String string) throws IOException {
		writeTag(T_STRING);
		writeStringValue(string);
	}
	
	protected abstract void writeArrayComponentType(Class<?> componentType) throws IOException;
	
	protected abstract void writeArrayLength(int length) throws IOException;
	
	protected abstract void writeArrayEnd() throws IOException;
	
	private void writeArray(Object array) throws IOException {
		int length = Array.getLength(array);
		Class<?> type = array.getClass().getComponentType();
		assert type != null;
		writeTag(T_ARRAY);
		writeArrayComponentType(type);
		writeArrayLength(length);
		if (type.isPrimitive()) {
			for (int i = 0; i < length; i++) {
				Object object = Array.get(array, i);
				assert object != null;
				writePrimitive_(object);
			}
			writeArrayEnd();
		} else {
			for (Object object : (Object[]) array)
				writeObject(object);
			writeArrayEnd();
		}
	}
	
	protected abstract void writeEnumType(String type) throws IOException;
	
	protected abstract void writeEnumID(String id) throws IOException;
	
	private void writeEnum(Enum<?> object) throws IOException {
		writeTag(T_ENUM);
		Class<?> type = object.getDeclaringClass();
		writeEnumType(yggdrasil.getID(type));
		writeEnumID(Yggdrasil.getID(object));
	}
	
	private void writeEnum(PseudoEnum<?> object) throws IOException {
		writeTag(T_ENUM);
		writeEnumType(yggdrasil.getID(object.getDeclaringClass()));
		writeEnumID(object.name());
	}
	
	protected abstract void writeClassType(Class<?> type) throws IOException;
	
	private void writeClass(Class<?> type) throws IOException {
		writeTag(T_CLASS);
		writeClassType(type);
	}
	
	protected abstract void writeReferenceID(int ref) throws IOException;
	
	protected final void writeReference(int ref) throws IOException {
		assert ref >= 0;
		writeTag(T_REFERENCE);
		writeReferenceID(ref);
	}
	
	protected abstract void writeObjectType(String type) throws IOException;
	
	protected abstract void writeNumFields(short numFields) throws IOException;
	
	protected abstract void writeFieldID(String id) throws IOException;
	
	protected abstract void writeObjectEnd() throws IOException;
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	private void writeGenericObject(Object object, int ref) throws IOException {
		Class<?> type = object.getClass();
		assert type != null;
		if (!yggdrasil.isSerializable(type))
			throw new NotSerializableException(type.getName());
		Fields fields;
		YggdrasilSerializer serializer = yggdrasil.getSerializer(type);
		if (serializer != null) {
			fields = serializer.serialize(object);
			if (!serializer.canBeInstantiated(type)) {
				ref = ~ref; // ~ instead of - to also get a negative value if ref is 0
				writtenObjects.put(object, ref);
			}
		} else if (object instanceof YggdrasilExtendedSerializable) {
			fields = ((YggdrasilExtendedSerializable) object).serialize();
		} else {
			fields = new Fields(object, yggdrasil);
		}
		if (fields.size() > Short.MAX_VALUE)
			throw new YggdrasilException("Class " + type.getCanonicalName() + " has too many fields (" + fields.size() + ")");
		
		writeTag(T_OBJECT);
		writeObjectType(yggdrasil.getID(type));
		writeNumFields((short) fields.size());
		for (FieldContext field : fields) {
			writeFieldID(field.id);
			if (field.isPrimitive())
				writePrimitive(field.getPrimitive());
			else
				writeObject(field.getObject());
		}
		writeObjectEnd();
		
		if (ref < 0)
			writtenObjects.put(object, ~ref);
	}
	
	private int nextObjectID = 0;
	private final IdentityHashMap<Object, Integer> writtenObjects = new IdentityHashMap<>();
	
	public final void writeObject(@Nullable Object object) throws IOException {
		if (object == null) {
			writeNull();
			return;
		}
		if (writtenObjects.containsKey(object)) {
			int ref = writtenObjects.get(object);
			if (ref < 0)
				throw new YggdrasilException("Uninstantiable object " + object + " is referenced in its fields' graph");
			writeReference(ref);
			return;
		}
		int ref = nextObjectID;
		nextObjectID++;
		writtenObjects.put(object, ref);
		Tag type = getType(object.getClass());
		if (type.isWrapper()) {
			writeWrappedPrimitive(object);
			return;
		}
		switch (type) {
			case T_ARRAY:
				writeArray(object);
				return;
			case T_STRING:
				writeString((String) object);
				return;
			case T_ENUM:
				if (object instanceof Enum)
					writeEnum((Enum<?>) object);
				else
					writeEnum((PseudoEnum<?>) object);
				return;
			case T_CLASS:
				writeClass((Class<?>) object);
				return;
			case T_OBJECT:
				writeGenericObject(object, ref);
				return;
			default:
				throw new YggdrasilException("unhandled type " + type);
		}
	}
	
}
