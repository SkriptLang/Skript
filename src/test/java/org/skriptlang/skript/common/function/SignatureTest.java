package org.skriptlang.skript.common.function;

import ch.njol.skript.lang.function.Signature;
import org.junit.Test;
import org.skriptlang.skript.common.function.DefaultFunctionImpl.DefaultParameter;
import org.skriptlang.skript.common.function.Signature.Modifier;

import java.util.LinkedHashMap;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class SignatureTest {

	@Test
	public void testToFormattedString() {
		LinkedHashMap<String, Parameter<?>> params = new LinkedHashMap<>();
		params.put("x", new DefaultParameter<>("x", Number.class));
		params.put("y", new DefaultParameter<>("y", Number.class));

		Signature<?> signature = new Signature<>("sum",
				Set.of(new Modifier.Local("script"), new Modifier.Returns<>(Number.class)),
				new Parameters(params),
				null);

		assertEquals("local function sum(x: number, y: number) returns number", signature.toFormattedString());
	}

}
