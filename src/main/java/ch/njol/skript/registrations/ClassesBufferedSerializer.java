package ch.njol.skript.registrations;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.variables.Variables;
import ch.njol.yggdrasil.DefaultYggdrasilOutputStream;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.variables.storage.BufferedSerializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Serializes values the same way as {@link Classes#serialize(Object)}, byte for byte, but into one buffer and through
 * one Yggdrasil stream that it reuses, so serializing a value allocates (almost) nothing itself.
 * Values whose serializer allocates, like items, still allocate that.
 * <p>
 * Only for the main thread, and not re-entrant.
 */
@ApiStatus.Internal
public final class ClassesBufferedSerializer implements BufferedSerializer {

	// a buffer grown bigger than this by one large value is dropped afterwards, rather than kept forever
	private static final int MAX_KEPT_CAPACITY = 64 * 1024;

	private Buffer buffer = new Buffer();
	private @Nullable DefaultYggdrasilOutputStream stream; // created when first needed, once Yggdrasil is set up
	// what Classes#serialize strips off the front of each value's bytes, per class info. Main thread only
	private final Map<ClassInfo<?>, byte[]> starts = new HashMap<>();

	private @Nullable String type;
	private int offset;
	private int length;

	@Override
	public boolean serialize(Object value) {
		type = null;
		ClassInfo<?> classInfo = Classes.getSavedClassInfo(value);
		if (classInfo == null)
			return false;
		Object saved = Classes.convertForSaving(value, classInfo);
		if (saved == null)
			return false;
		try {
			byte[] start = starts.get(classInfo);
			if (start == null) {
				start = Classes.getYggdrasilStart(classInfo);
				starts.put(classInfo, start);
			}
			DefaultYggdrasilOutputStream stream = stream();
			stream.writeObject(saved);
			stream.flush();
			assert startsWith(buffer, start) : value + " (" + classInfo.getC().getName() + ")";
			type = classInfo.getCodeName();
			offset = start.length;
			length = buffer.size() - start.length;
			return true;
		} catch (IOException e) { // shouldn't happen
			Skript.exception(e);
			return false;
		}
	}

	/**
	 * @return the stream, emptied and ready for a new value
	 */
	private DefaultYggdrasilOutputStream stream() throws IOException {
		if (buffer.capacity() > MAX_KEPT_CAPACITY) { // the stream writes to the buffer, so both are replaced
			buffer = new Buffer();
			stream = null;
		}
		buffer.reset();
		if (stream == null) {
			stream = new DefaultYggdrasilOutputStream(Variables.yggdrasil, buffer); // writes the header itself
		} else {
			stream.reset();
		}
		return stream;
	}

	private static boolean startsWith(Buffer buffer, byte[] start) {
		if (buffer.size() < start.length)
			return false;
		for (int i = 0; i < start.length; i++) {
			if (buffer.array()[i] != start[i])
				return false;
		}
		return true;
	}

	@Override
	public String type() {
		if (type == null)
			throw new IllegalStateException("nothing was serialized");
		return type;
	}

	@Override
	public byte[] data() {
		return buffer.array();
	}

	@Override
	public int offset() {
		return offset;
	}

	@Override
	public int length() {
		return length;
	}

	/**
	 * A byte array output stream whose array can be read without copying it.
	 */
	private static final class Buffer extends ByteArrayOutputStream {

		Buffer() {
			super(256);
		}

		byte[] array() {
			return buf;
		}

		int capacity() {
			return buf.length;
		}

	}

}
