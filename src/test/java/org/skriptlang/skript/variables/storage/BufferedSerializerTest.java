package org.skriptlang.skript.variables.storage;

import ch.njol.skript.variables.SerializedVariable.Value;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.Assert.*;

public class BufferedSerializerTest {

	@Test
	public void hashOfPartOfAnArrayMatchesHashOfTheValue() {
		byte[] data = "some serialized value".getBytes(StandardCharsets.UTF_8);
		byte[] buffer = new byte[data.length + 10];
		Arrays.fill(buffer, (byte) 0x7f); // junk around the value
		System.arraycopy(data, 0, buffer, 4, data.length);

		long whole = SavedVariableHashes.hash(new Value("string", data));
		assertEquals(whole, SavedVariableHashes.hash("string", buffer, 4, data.length));
		assertNotEquals(whole, SavedVariableHashes.hash("string", buffer, 4, data.length - 1));
		assertNotEquals(whole, SavedVariableHashes.hash("number", buffer, 4, data.length));
	}

	@Test
	public void hashOfNothingMatches() {
		assertEquals(SavedVariableHashes.hash(new Value("string", new byte[0])),
			SavedVariableHashes.hash("string", new byte[5], 5, 0));
	}

	@Test
	public void wrappedFunction() {
		BufferedSerializer serializer = BufferedSerializer.of(value ->
			value instanceof String string ? new Value("string", string.getBytes(StandardCharsets.UTF_8)) : null);

		assertTrue(serializer.serialize("hello"));
		assertEquals("string", serializer.type());
		assertEquals(0, serializer.offset());
		assertEquals(5, serializer.length());
		Value value = serializer.toValue();
		assertEquals("string", value.type);
		assertArrayEquals("hello".getBytes(StandardCharsets.UTF_8), value.data);

		assertFalse(serializer.serialize(42));
		assertThrows(IllegalStateException.class, serializer::type);
	}

}
