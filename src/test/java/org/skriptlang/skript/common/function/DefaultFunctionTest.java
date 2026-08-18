package org.skriptlang.skript.common.function;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.Signature;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.StringUtils;
import org.junit.Test;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.common.function.DefaultFunction;
import org.skriptlang.skript.common.function.Parameter;
import org.skriptlang.skript.common.function.Parameter.Modifier;

import static org.junit.Assert.*;

public class DefaultFunctionTest {

	private static final SkriptAddon SKRIPT = Skript.instance();

	@Test
	public void testStrings() {
		DefaultFunction<String> built = DefaultFunction.builder(SKRIPT, "test", String.class)
			.description()
			.since()
			.keywords()
			.parameter("x", String[].class, Modifier.OPTIONAL)
			.build(args -> {
				String[] xes = args.getOrDefault("x", new String[]{""});

				return StringUtils.join(xes, ",");
			});

		Signature<?> signature = (Signature<?>) built.signature();

		assertEquals("test", signature.getName());
		assertEquals(String.class, signature.getReturnType().getC());
		assertTrue(signature.isSingle());
		assertArrayEquals(new String[]{}, built.description().toArray(new String[0]));
		assertArrayEquals(new String[]{}, built.since().toArray(new String[0]));
		assertArrayEquals(new String[]{}, built.keywords().toArray(new String[0]));

		Parameter<?>[] parameters = signature.getParameters();

		assertEquals(new ch.njol.skript.lang.function.Parameter<>("x", getClassInfo(String[].class), false, null, false, true), parameters[0]);
	}

	@Test
	public void testObjectArrays() {
		DefaultFunction<Object[]> built = DefaultFunction.builder(SKRIPT, "test", Object[].class)
			.description("x", "y")
			.since("1", "2")
			.keywords("x", "y")
			.parameter("x", Object[].class, Modifier.OPTIONAL)
			.parameter("y", Boolean.class)
			.build(args -> new Object[]{true, 1});

		Signature<?> signature = (Signature<?>) built.signature();

		assertEquals("test", signature.getName());
		assertEquals(Object.class, signature.getReturnType().getC());
		assertFalse(signature.isSingle());
		assertArrayEquals(new String[]{"x", "y"}, built.description().toArray(new String[0]));
		assertArrayEquals(new String[]{"1", "2"}, built.since().toArray(new String[0]));
		assertArrayEquals(new String[]{"x", "y"}, built.keywords().toArray(new String[0]));

		Parameter<?>[] parameters = signature.getParameters();

		assertEquals(new ch.njol.skript.lang.function.Parameter<>("x", getClassInfo(Object[].class), false, null, false, true), parameters[0]);
		assertEquals(new ch.njol.skript.lang.function.Parameter<>("y", getClassInfo(Boolean.class), true, null), parameters[1]);

		Object[] execute = ((DefaultFunctionImpl<?>) built).execute(consign(new Object[]{1, 2, 3}, new Boolean[]{true}));

		assertArrayEquals(new Object[]{true, 1}, execute);

		execute = ((DefaultFunctionImpl<?>) built).execute(consign(new Object[]{}, new Boolean[]{true}));

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

	/**
	 * Returns the {@link ClassInfo} of the non-array type of {@code cls}.
	 *
	 * @param cls The class.
	 * @param <T> The type of class.
	 * @return The non-array {@link ClassInfo} of {@code cls}.
	 */
	private static <T> ClassInfo<T> getClassInfo(Class<T> cls) {
		ClassInfo<T> classInfo;
		if (cls.isArray()) {
			//noinspection unchecked
			classInfo = (ClassInfo<T>) Classes.getExactClassInfo(cls.componentType());
		} else {
			classInfo = Classes.getExactClassInfo(cls);
		}
		if (classInfo == null) {
			throw new IllegalArgumentException("No type found for " + cls.getSimpleName());
		}
		return classInfo;
	}

}
