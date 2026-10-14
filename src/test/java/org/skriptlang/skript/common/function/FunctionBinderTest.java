package org.skriptlang.skript.common.function;

import org.junit.Test;
import org.skriptlang.skript.common.function.Parameter.Modifier;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the allocation of passed arguments across a signature's parameters.
 *
 * @see FunctionBinder#allocations(Parameter[], int)
 */
public class FunctionBinderTest {

	private static Parameter<?> single(String name) {
		return new ScriptParameter<>(name, Number.class);
	}

	private static Parameter<?> list(String name) {
		return new ScriptParameter<>(name, Number[].class);
	}

	private static Parameter<?> optionalSingle(String name) {
		return new ScriptParameter<>(name, Number.class, Modifier.OPTIONAL);
	}

	private static Parameter<?> optionalList(String name) {
		return new ScriptParameter<>(name, Number[].class, Modifier.OPTIONAL);
	}

	private static Parameter<?>[] parameters(Parameter<?>... parameters) {
		return parameters;
	}

	/**
	 * Asserts that the first division offered for {@code slots} arguments is {@code expected}. The
	 * binder tries the divisions in order and keeps the first which binds, so the first one is the
	 * division that decides how a call binds when every division would fit.
	 */
	private static void assertGreediest(int[] expected, Parameter<?>[] parameters, int slots) {
		List<int[]> divisions = FunctionBinder.allocations(parameters, slots);
		assertFalse("no division for " + slots + " arguments", divisions.isEmpty());
		assertArrayEquals("first division for " + slots + " arguments", expected, divisions.getFirst());
	}

	/**
	 * Asserts that {@code slots} arguments cannot be divided across {@code parameters} at all.
	 */
	private static void assertNoDivision(Parameter<?>[] parameters, int slots) {
		assertTrue("a division was offered for " + slots + " arguments",
			FunctionBinder.allocations(parameters, slots).isEmpty());
	}

	@Test
	public void testNoParameters() {
		assertGreediest(new int[0], parameters(), 0);
		assertNoDivision(parameters(), 1);
	}

	@Test
	public void testOnlySingleParameters() {
		Parameter<?>[] parameters = parameters(single("a"), single("b"));

		assertGreediest(new int[]{1, 1}, parameters, 2);

		// without a list parameter there is nothing to absorb a surplus, and nothing may be omitted
		assertNoDivision(parameters, 3);
		assertNoDivision(parameters, 1);
		assertNoDivision(parameters, 0);
	}

	@Test
	public void testListParameterAbsorbsSurplus() {
		// f(a: number, b: numbers) given 1, 2, 3 binds a=1 and b=(2, 3)
		assertGreediest(new int[]{1, 2}, parameters(single("a"), list("b")), 3);
		assertGreediest(new int[]{1, 1}, parameters(single("a"), list("b")), 2);

		// a required list parameter must still be given something
		assertNoDivision(parameters(single("a"), list("b")), 1);
	}

	@Test
	public void testAllocatesLeftToRight() {
		// the leftmost list parameter takes as much as it can while leaving enough for the rest
		assertGreediest(new int[]{2, 1}, parameters(list("a"), single("b")), 3);
		assertGreediest(new int[]{2, 1}, parameters(list("a"), list("b")), 3);
		assertGreediest(new int[]{1, 2, 1}, parameters(single("a"), list("b"), single("c")), 4);
	}

	@Test
	public void testSingleListParameter() {
		assertGreediest(new int[]{3}, parameters(list("a")), 3);
		assertGreediest(new int[]{1}, parameters(list("a")), 1);

		// a required list parameter cannot be given nothing
		assertNoDivision(parameters(list("a")), 0);
		assertGreediest(new int[]{0}, parameters(optionalList("a")), 0);
	}

	@Test
	public void testOptionalParametersMayBeOmitted() {
		Parameter<?>[] parameters = parameters(single("a"), optionalList("b"));

		assertGreediest(new int[]{1, 0}, parameters, 1);
		assertGreediest(new int[]{1, 1}, parameters, 2);
		assertGreediest(new int[]{1, 2}, parameters, 3);

		assertNoDivision(parameters, 0);
	}

	@Test
	public void testOptionalBeforeRequiredIsStillRequired() {
		// matching Signature#getMinParameters, a parameter may only be omitted when it and every
		// parameter after it is optional
		Parameter<?>[] parameters = parameters(optionalSingle("a"), list("b"));

		assertGreediest(new int[]{1, 1}, parameters, 2);
		assertGreediest(new int[]{1, 2}, parameters, 3);

		assertNoDivision(parameters, 1);
	}

	@Test
	public void testTrailingOptionalSingleParameters() {
		Parameter<?>[] parameters = parameters(single("a"), optionalSingle("b"), optionalSingle("c"));

		assertGreediest(new int[]{1, 0, 0}, parameters, 1);
		assertGreediest(new int[]{1, 1, 0}, parameters, 2);
		assertGreediest(new int[]{1, 1, 1}, parameters, 3);

		// no list parameter, so a surplus cannot be absorbed
		assertNoDivision(parameters, 4);
	}


	@Test
	public void testAllocationsBeginWithTheGreediestDivision() {
		// the binder tries the divisions in order and keeps the first which binds, so the first has
		// to be the greediest on the left: the leftmost list parameter takes everything the ones
		// after it do not require. That is what keeps a call which already bound binding the same
		// way
		Parameter<?>[] parameters = parameters(list("a"), list("b"), list("c"));

		for (int slots = 3; slots <= 7; slots++) {
			assertGreediest(new int[]{slots - 2, 1, 1}, parameters, slots);
		}
	}

	@Test
	public void testAllocationsOfferEveryDivision() {
		// three list parameters and five arguments: the greediest division is 3/1/1, and 2/1/2 has
		// to be offered too, since that is the one f(texts, numbers, texts) needs for
		// "1", "2", -1, "3", "4"
		List<int[]> divisions =
			FunctionBinder.allocations(parameters(list("a"), list("b"), list("c")), 5);

		assertArrayEquals(new int[]{3, 1, 1}, divisions.getFirst());
		assertTrue("2/1/2 was not offered",
			divisions.stream().anyMatch(division -> java.util.Arrays.equals(division, new int[]{2, 1, 2})));

		// every division must use all of the arguments
		for (int[] division : divisions) {
			assertEquals("a division did not use every argument", 5,
				java.util.Arrays.stream(division).sum());
		}
	}

	@Test
	public void testAllocationsAreEmptyWhenNothingFits() {
		// two single parameters cannot take three arguments however they are divided
		assertTrue(FunctionBinder.allocations(parameters(single("a"), single("b")), 3).isEmpty());
		assertTrue(FunctionBinder.allocations(parameters(), 1).isEmpty());
	}

	@Test
	public void testAllocationsRespectSingleParameters() {
		// a single parameter never takes more than one argument in any division
		List<int[]> divisions =
			FunctionBinder.allocations(parameters(single("a"), list("b"), single("c")), 5);

		assertTrue("no division was offered", !divisions.isEmpty());
		for (int[] division : divisions) {
			assertEquals("a single parameter took more than one argument", 1, division[0]);
			assertEquals("a single parameter took more than one argument", 1, division[2]);
		}
	}

}
