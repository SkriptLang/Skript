package org.skriptlang.skript.variables;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * Checks the values and branches of {@link VariableMap}, with every test running against both modes.
 * List views aren't covered here, so the tree is checked through {@link VariableMap#branchCount()}.
 */
@RunWith(Parameterized.class)
public class VariableMapTest {

	@Parameters(name = "{0}")
	public static List<Object[]> modes() {
		return List.of(
			new Object[]{"concurrent", (Supplier<VariableMap>) VariableMap::concurrent},
			new Object[]{"local", (Supplier<VariableMap>) VariableMap::local}
		);
	}

	@Parameter
	public String mode;

	@Parameter(1)
	public Supplier<VariableMap> factory;

	private VariableMap map;

	@Before
	public void setUp() {
		map = factory.get();
	}

	@Test
	public void testEmptyMap() {
		assertTrue(map.isEmpty());
		assertEquals(0, map.size());
		assertEquals(0, map.branchCount());
		assertNull(map.getVariable("a"));
		assertNull(map.getVariable("a::b"));
	}

	@Test
	public void testSetOverwriteDelete() {
		assertNull(map.setVariable("a", 1));
		assertEquals(1, map.getVariable("a"));
		assertEquals(1, map.size());
		assertFalse(map.isEmpty());

		assertEquals(1, map.setVariable("a", 2));
		assertEquals(2, map.getVariable("a"));
		assertEquals(1, map.size());

		assertEquals(2, map.setVariable("a", null));
		assertNull(map.getVariable("a"));
		assertEquals(0, map.size());
		assertTrue(map.isEmpty());
	}

	@Test
	public void testDeleteMissing() {
		assertNull(map.setVariable("a", null));
		assertNull(map.setVariable("a::b", null));
		assertEquals(0, map.size());
		assertEquals(0, map.branchCount());

		// the parent exists here, but has no entry for this key
		map.setVariable("a::b", 1);
		assertNull(map.setVariable("a::c", null));
		assertEquals(1, map.getVariable("a::b"));
		assertEquals(1, map.branchCount());
	}

	@Test
	public void testListNameRejectsValue() {
		assertThrows(IllegalArgumentException.class, () -> map.setVariable("a::*", 1));
		assertEquals(0, map.size());
		assertEquals(0, map.branchCount());
	}

	@Test
	public void testRootNamesStayOutOfTree() {
		map.setVariable("a", 1);
		map.setVariable("b", 2);
		assertEquals(0, map.branchCount());
	}

	@Test
	public void testNestedSetCreatesAncestors() {
		map.setVariable("a::b::c", 1);
		assertEquals(1, map.getVariable("a::b::c"));
		assertEquals(1, map.size());
		assertEquals(2, map.branchCount()); // a and a::b
		assertNull(map.getVariable("a"));
		assertNull(map.getVariable("a::b"));
	}

	@Test
	public void testOverwriteLeavesTreeAlone() {
		map.setVariable("a::b", 1);
		map.setVariable("a::b", 2);
		assertEquals(2, map.getVariable("a::b"));
		assertEquals(1, map.size());
		assertEquals(1, map.branchCount());
	}

	@Test
	public void testDeleteNestedLeavesNothing() {
		map.setVariable("a::b::c", 1);
		map.setVariable("a::b::c", null);
		assertEquals(0, map.size());
		assertEquals(0, map.branchCount());
	}

	@Test
	public void testDeleteStopsAtSharedAncestor() {
		map.setVariable("a::b::c", 1);
		map.setVariable("a::d", 2);
		map.setVariable("a::b::c", null);
		assertEquals(1, map.branchCount()); // a::b is gone, but a still has d
		assertEquals(2, map.getVariable("a::d"));

		map.setVariable("a::d", null);
		assertEquals(0, map.branchCount());
	}

	@Test
	public void testBranchWithValueBecomesLeaf() {
		map.setVariable("x::a", 1);
		map.setVariable("x::a::b", 2);
		assertEquals(2, map.branchCount()); // x and x::a

		map.setVariable("x::a::b", null);
		assertEquals(1, map.branchCount()); // x::a is a leaf again, so only x is left
		assertEquals(1, map.getVariable("x::a"));

		map.setVariable("x::a", null);
		assertEquals(0, map.branchCount());
	}

	@Test
	public void testDeleteBranchValueKeepsChildren() {
		map.setVariable("x::a::b", 1);
		map.setVariable("x::a", 2);
		map.setVariable("x::a", null);
		assertNull(map.getVariable("x::a"));
		assertEquals(1, map.getVariable("x::a::b"));
		assertEquals(2, map.branchCount());

		map.setVariable("x::a::b", null);
		assertEquals(0, map.branchCount());
	}

	@Test
	public void testRootValueSurvivesPrune() {
		map.setVariable("x", 1);
		map.setVariable("x::a", 2);
		assertEquals(1, map.branchCount());

		map.setVariable("x::a", null);
		assertEquals(0, map.branchCount());
		assertEquals(1, map.getVariable("x"));
		assertEquals(1, map.size());
	}

	@Test
	public void testSeparatorEdgeCases() {
		map.setVariable("a:::b", 1); // parent a, key :b
		assertEquals(1, map.getVariable("a:::b"));
		assertEquals(1, map.branchCount());
		map.setVariable("a:::b", null);
		assertEquals(0, map.branchCount());

		map.setVariable("a::::b", 1); // parent a::, key b
		assertEquals(1, map.getVariable("a::::b"));
		assertEquals(2, map.branchCount());
		map.setVariable("a::::b", null);
		assertEquals(0, map.branchCount());
	}

	@Test
	public void testChurnDoesNotLeak() {
		for (int i = 0; i < 10_000; i++) {
			String name = "cooldown::" + i + "::end";
			map.setVariable(name, i);
			map.setVariable(name, null);
		}
		assertEquals(0, map.size());
		assertEquals(0, map.branchCount());
	}

	@Test
	public void testLastSeparator() {
		assertEquals(-1, VariableMap.lastSeparator("abc"));
		assertEquals(1, VariableMap.lastSeparator("a::b"));
		assertEquals(4, VariableMap.lastSeparator("a::b::c"));
		assertEquals(1, VariableMap.lastSeparator("a:::b"));
		assertEquals(3, VariableMap.lastSeparator("a::::b"));
	}

}
