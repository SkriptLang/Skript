package org.skriptlang.skript.variables.storage;


import ch.njol.skript.variables.SerializedVariable;

import java.util.zip.CRC32C;

/**
 * The hash of what was last saved for each variable, so the refresh can tell which values changed.
 * Only used on the main thread.
 */
final class SavedVariableHashes {

	/**
	 * Stored for a variable whose saved value is unknown, so it matches nothing.
	 * A value that really hashes to this is just saved again by every pass, which is harmless.
	 */
	static final long UNKNOWN = Long.MIN_VALUE;

	private String[] keys;   // null = empty slot.
	private long[] hashes;
	private int size;

	SavedVariableHashes(int expected) {
		int capacity = Integer.highestOneBit(Math.max(16, expected * 4 / 3) - 1) << 1; // give a bit of room over expected
		keys = new String[capacity];
		hashes = new long[capacity];
	}

	/**
	 * Hashes a serialized value, to compare with what was saved for it last.
	 * Two independent 32-bit hashes make up the 64 bits, so two different values getting the same hash
	 * is practically impossible. If it does happen, the change is only saved once the variable changes again.
	 * The hashes are only kept in memory, so they don't have to stay the same between versions or restarts.
	 *
	 * @param value The serialized value
	 * @return Its hash
	 */
	static long hash(SerializedVariable.Value value) {
		return hash(value.type, value.data, 0, value.data.length);
	}

	/**
	 * Hashes a serialized value held in part of an array, like {@link BufferedSerializer}'s buffer,
	 * without copying it out. Gives the same hash as {@link #hash(SerializedVariable.Value)} for the same bytes.
	 *
	 * @param type   The value's type
	 * @param data   The array holding the value's data
	 * @param offset Where the data starts
	 * @param length How long the data is
	 * @return Its hash
	 */
	static long hash(String type, byte[] data, int offset, int length) {
		CRC32C crc = new CRC32C();
		crc.update(data, offset, length); // intrinsic CRC instruction
		int dataHash = 1; // what Arrays.hashCode(byte[]) gives, which has no version for part of an array
		for (int i = offset, end = offset + length; i < end; i++)
			dataHash = 31 * dataHash + data[i];
		int other = 31 * type.hashCode() + dataHash;
		return (long) other << 32 | crc.getValue();
	}

	/**
	 * Does this variable hash match the stored hash?
	 *
	 * @param name The variable name
	 * @param hash The hashed value
	 * @return Whether the hash matches the stored hash (false if no hash stored)
	 */
	boolean matches(String name, long hash) {
		int i = slot(name);
		return keys[i] != null && hashes[i] == hash;
	}

	void put(String name, long hash) {
		int i = slot(name);
		if (keys[i] == null) {
			size++;
			if (size > keys.length * 0.75) {
				// grow
				String[] oldKeys = keys;
				long[] oldHashes = hashes;
				keys = new String[keys.length * 2];
				hashes = new long[keys.length];
				for (int j = 0; j < oldKeys.length; j++) {
					if (oldKeys[j] == null)
						continue;
					int k = slot(oldKeys[j]);
					keys[k] = oldKeys[j];
					hashes[k] = oldHashes[j];
				}
				i = slot(name); // re-acquire slot since size changed
			}
			keys[i] = name;
		}
		hashes[i] = hash;
	}

	/**
	 * Forgets the hash stored for a variable, so it matches nothing.
	 * Only changes a variable that's already here, since one that isn't matches nothing already,
	 * so this never grows the table.
	 *
	 * @param name The variable name
	 */
	void invalidate(String name) {
		int i = slot(name);
		if (keys[i] != null)
			hashes[i] = UNKNOWN;
	}

	/**
	 * Hashes the name to get an open slot in the keys array.
	 *
	 * @param name variable name
	 * @return index into keys/hashes to use
	 */
	@SuppressWarnings("StringEquality")
	private int slot(String name) {
		int mask = keys.length - 1;
		int i = mix(name.hashCode()) & mask;   // String caches its hashCode, so this is cheap
		while (true) {
			String key = keys[i];
			if (key == null || key == name || key.equals(name)) // usually the same instance, so == is the fast path
				return i;
			i = (i + 1) & mask; // linear probing until we find an open slot
		}
	}

	/**
	 * some nonsense to help avoid hash conflicts for strings
	 * HashMap does it so it must be cool.
	 *
	 * @param h the number to mix
	 * @return h, but with its high bits xored with its low bits.
	 */
	private static int mix(int h) {
		return h ^ (h >>> 16);
	}


}
