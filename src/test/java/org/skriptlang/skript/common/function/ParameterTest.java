package org.skriptlang.skript.common.function;

import org.junit.Test;
import org.skriptlang.skript.common.function.DefaultFunctionImpl.DefaultParameter;
import org.skriptlang.skript.common.function.Parameter.Modifier;

import static org.junit.Assert.assertEquals;

public class ParameterTest {

	@Test
	public void testToFormattedString() {
		DefaultParameter<Number> param = new DefaultParameter<>("x", Number.class,
				new Modifier.Optional(), new Modifier.Keyed(), new Modifier.Ranged<>(0, 10));

		assertEquals("x: optional keyed number between 0 and 10", param.toFormattedString());
	}

}
