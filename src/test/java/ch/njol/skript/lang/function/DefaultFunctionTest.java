package ch.njol.skript.lang.function;

import ch.njol.skript.registrations.Classes;
import ch.njol.util.StringUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class DefaultFunctionTest {

	@Test
	public void testStrings() {
		DefaultFunction<String> built = DefaultFunction.builder("test", String.class)
			.description()
			.since()
			.keywords()
			.optionalParameter("x", String[].class)
			.build(args -> {
				String[] xes = args.getOrDefault("x", new String[]{""});

				return StringUtils.join(xes, ",");
			});

		assertEquals("test", built.getName());
		assertEquals(String.class, built.getReturnType().getC());
		assertTrue(built.isSingle());
		assertArrayEquals(new String[]{}, built.description());
		assertArrayEquals(new String[]{}, built.since());
		assertArrayEquals(new String[]{}, built.keywords());

		Parameter<?>[] parameters = built.getParameters();

		assertEquals(new Parameter<>("x", DefaultFunction.getClassInfo(String.class), false, true), parameters[0]);

		String[] execute = built.execute(consign());
		assertArrayEquals(new String[]{}, execute);
	}

	@Test
	public void testObjectArrays() {
		DefaultFunction<Object[]> built = DefaultFunction.builder("test", Object[].class)
			.description("x", "y")
			.since("1", "2")
			.keywords("x", "y")
			.optionalParameter("x", Object[].class)
			.parameter("y", Boolean.class)
			.build(args -> new Object[]{true, 1});

		assertEquals("test", built.getName());
		assertEquals(Object.class, built.getReturnType().getC());
		assertFalse(built.isSingle());
		assertArrayEquals(new String[]{"x", "y"}, built.description());
		assertArrayEquals(new String[]{"1", "2"}, built.since());
		assertArrayEquals(new String[]{"x", "y"}, built.keywords());

		Parameter<?>[] parameters = built.getParameters();

		assertEquals(new Parameter<>("x", DefaultFunction.getClassInfo(Object.class), false, true), parameters[0]);
		assertEquals(new Parameter<>("y", Classes.getExactClassInfo(Boolean.class), true, false), parameters[1]);

		Object[] execute = built.execute(consign(new Object[]{1, 2, 3}, new Boolean[]{true}));

		assertArrayEquals(new Object[]{true, 1}, execute);

		execute = built.execute(consign(new Object[]{}, new Boolean[]{true}));

		assertArrayEquals(new Object[]{true, 1}, execute);
	}

	static Object[][] consign(Object... arguments) {
		Object[][] consigned = new Object[arguments.length][];
		for (int i = 0; i < consigned.length; i++) {
			if (arguments[i] instanceof Object[] || arguments[i] == null) {
				consigned[i] = (Object[]) arguments[i];
			} else {
				consigned[i] = new Object[]{arguments[i]};
			}
		}
		return consigned;
	}

}
