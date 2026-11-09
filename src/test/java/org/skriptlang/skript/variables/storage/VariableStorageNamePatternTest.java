package org.skriptlang.skript.variables.storage;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests which variable names a {@link VariableStorage} accepts, going by its name pattern.
 */
public class VariableStorageNamePatternTest {

	private RecordingStorage storage;

	@Before
	public void setUp() {
		storage = new RecordingStorage();
	}

	@Test
	public void noPatternAcceptsEveryName() {
		storage.setPatternForTest(null);
		assertNull(storage.getNamePattern());
		assertTrue(storage.accept("x"));
		assertTrue(storage.accept("a::b::c"));
		assertTrue(storage.accept(""));
	}

	@Test
	public void patternMustMatchTheWholeName() {
		storage.setPatternForTest("kills::.*");
		assertTrue(storage.accept("kills::steve"));
		assertTrue(storage.accept("kills::"));
		assertFalse("only part of the name matches", storage.accept("player::kills::steve"));
		assertFalse(storage.accept("kills"));
	}

	@Test
	public void patternIsReturnedAsConfigured() {
		storage.setPatternForTest("(?i)temp::.+");
		assertEquals("(?i)temp::.+", storage.getNamePattern().pattern());
		assertTrue(storage.accept("TEMP::x"));
		assertFalse(storage.accept("temp::"));
	}

}
