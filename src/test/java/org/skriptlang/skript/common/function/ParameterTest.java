package org.skriptlang.skript.common.function;

import org.junit.Test;
import org.skriptlang.skript.common.function.DefaultFunctionImpl.DefaultParameter;
import org.skriptlang.skript.common.function.Parameter.Modifier;
import org.skriptlang.skript.common.function.Parameter.Modifier.Ranged;

import static org.junit.Assert.*;

public class ParameterTest {

	@Test
	public void testToFormattedString() {
		DefaultParameter<Number> param = new DefaultParameter<>("x", Number.class,
				new Modifier.Optional(), new Modifier.Keyed(), new Modifier.Ranged<>(0, 10));

		assertEquals("x: optional keyed number between 0 and 10", param.toFormattedString());
	}

	@Test
	public void testInequality() {
		DefaultParameter<Number> a = new DefaultParameter<>("x", Number.class,
				new Modifier.Optional(), new Modifier.Keyed(), new Modifier.Ranged<>(0, 10));

		DefaultParameter<Number> b = new DefaultParameter<>("x", Number.class,
				new Modifier.Optional(), new Modifier.Keyed(), new Modifier.Ranged<>(1, 2));

		assertNotEquals(a, b);
	}

	@Test
	public void testDeprecatedGetModifier() {
		DefaultParameter<Number> a = new DefaultParameter<>("x", Number.class, Modifier.ranged(0, 10));

		assertTrue(a.hasModifier(Modifier.Ranged.class));
		assertEquals(0, (int) a.getModifier(Ranged.class).min());
		assertEquals(10, (int) a.getModifier(Ranged.class).max());

		DefaultParameter<Number> b = new DefaultParameter<>("x", Number.class, new Modifier.Ranged<>(0, 10));

		assertTrue(b.hasModifier(Modifier.RANGED));
		assertEquals(0, (int) a.getModifier(Modifier.RangedModifier.class).getMin());
		assertEquals(10, (int) a.getModifier(Modifier.RangedModifier.class).getMax());

		assertTrue(b.hasModifier(Modifier.Ranged.class));
		assertEquals(0, (int) a.getModifier(Modifier.RangedModifier.class).getMin());
		assertEquals(10, (int) a.getModifier(Modifier.RangedModifier.class).getMax());
	}

}
