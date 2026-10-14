package ch.njol.skript.lang.function;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.lang.function.FunctionRegistry.FunctionIdentifier;
import ch.njol.skript.lang.function.FunctionRegistry.RetrievalResult;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.DefaultClasses;
import ch.njol.skript.util.Contract;
import org.skriptlang.skript.common.function.FunctionReference;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

public class FunctionRegistryTest {

	private static final FunctionRegistry registry = FunctionRegistry.getRegistry();
	private static final String FUNCTION_NAME = "testFunctionRegistry";
	private static final String TEST_SCRIPT = "test";

	private static final Function<Boolean> TEST_FUNCTION = new SimpleJavaFunction<>(FUNCTION_NAME, new Parameter[0],
		DefaultClasses.BOOLEAN, true) {
		@Override
		public Boolean @Nullable [] executeSimple(Object[][] params) {
			return new Boolean[]{true};
		}
	};

	@Test
	public void testGetFunctionRetrieval() {
		assertEquals(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME).result());

		assertEquals(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME).result());
		assertNull(registry.getSignature(null, FUNCTION_NAME).retrieved());
		assertNull(registry.getSignature(null, FUNCTION_NAME).conflictingArgs());

		assertEquals(RetrievalResult.NOT_REGISTERED, registry.getFunction(null, FUNCTION_NAME).result());
		assertNull(registry.getFunction(null, FUNCTION_NAME).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME).conflictingArgs());

		registry.register(null, TEST_FUNCTION);

		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME).result());

		assertEquals(RetrievalResult.EXACT, registry.getSignature(null, FUNCTION_NAME).result());
		assertEquals(TEST_FUNCTION.getSignature(), registry.getSignature(null, FUNCTION_NAME).retrieved());
		assertNull(registry.getSignature(null, FUNCTION_NAME).conflictingArgs());

		assertEquals(RetrievalResult.EXACT, registry.getFunction(null, FUNCTION_NAME).result());
		assertEquals(TEST_FUNCTION, registry.getFunction(null, FUNCTION_NAME).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME).conflictingArgs());

		registry.remove(TEST_FUNCTION.getSignature());
	}

	@Test
	public void testSimpleMultipleRegistrationsFunction() {
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME).result());
		assertNull(registry.getSignature(null, FUNCTION_NAME).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME).retrieved());

		registry.register(null, TEST_FUNCTION);

		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME).result());
		assertEquals(TEST_FUNCTION.getSignature(), registry.getSignature(null, FUNCTION_NAME).retrieved());
		assertEquals(TEST_FUNCTION, registry.getFunction(null, FUNCTION_NAME).retrieved());

		assertThrows(SkriptAPIException.class, () -> registry.register(null, TEST_FUNCTION));

		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME).result());
		assertEquals(TEST_FUNCTION.getSignature(), registry.getSignature(null, FUNCTION_NAME).retrieved());
		assertEquals(TEST_FUNCTION, registry.getFunction(null, FUNCTION_NAME).retrieved());

		registry.remove(TEST_FUNCTION.getSignature());
	}

	@Test
	public void testSimpleRegisterRemoveRegisterGlobal() {
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME).result());
		assertNull(registry.getSignature(null, FUNCTION_NAME).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME).retrieved());

		registry.register(null, TEST_FUNCTION);

		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME).result());
		assertEquals(TEST_FUNCTION.getSignature(), registry.getSignature(null, FUNCTION_NAME).retrieved());
		assertEquals(TEST_FUNCTION, registry.getFunction(null, FUNCTION_NAME).retrieved());

		registry.remove(TEST_FUNCTION.getSignature());

		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME).result());
		assertNull(registry.getSignature(null, FUNCTION_NAME).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME).retrieved());

		registry.register(null, TEST_FUNCTION);

		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME).result());
		assertEquals(TEST_FUNCTION.getSignature(), registry.getSignature(null, FUNCTION_NAME).retrieved());
		assertEquals(TEST_FUNCTION, registry.getFunction(null, FUNCTION_NAME).retrieved());

		registry.remove(TEST_FUNCTION.getSignature());
	}

	private static final Function<Boolean> LOCAL_TEST_FUNCTION = new SimpleJavaFunction<>(TEST_SCRIPT, FUNCTION_NAME, new Parameter[0],
		DefaultClasses.BOOLEAN, true) {
		@Override
		public Boolean @Nullable [] executeSimple(Object[][] params) {
			return new Boolean[]{true};
		}
	};

	@Test
	public void testSimpleRegisterRemoveRegisterLocal() {
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(TEST_SCRIPT, FUNCTION_NAME).result());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME).result());
		assertNull(registry.getSignature(null, FUNCTION_NAME).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME).retrieved());

		registry.register(TEST_SCRIPT, LOCAL_TEST_FUNCTION);

		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(TEST_SCRIPT, FUNCTION_NAME).result());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME).result());
		assertEquals(LOCAL_TEST_FUNCTION.getSignature(), registry.getSignature(TEST_SCRIPT, FUNCTION_NAME).retrieved());
		assertEquals(LOCAL_TEST_FUNCTION, registry.getFunction(TEST_SCRIPT, FUNCTION_NAME).retrieved());

		registry.remove(LOCAL_TEST_FUNCTION.getSignature());

		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(TEST_SCRIPT, FUNCTION_NAME).result());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME).result());
		assertNull(registry.getSignature(TEST_SCRIPT, FUNCTION_NAME).retrieved());
		assertNull(registry.getSignature(null, FUNCTION_NAME).retrieved());
		assertNull(registry.getFunction(TEST_SCRIPT, FUNCTION_NAME).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME).retrieved());

		registry.register(TEST_SCRIPT, LOCAL_TEST_FUNCTION);
		registry.register(null, TEST_FUNCTION);

		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(TEST_SCRIPT, FUNCTION_NAME).result());
		assertEquals(LOCAL_TEST_FUNCTION.getSignature(), registry.getSignature(TEST_SCRIPT, FUNCTION_NAME).retrieved());
		assertEquals(LOCAL_TEST_FUNCTION, registry.getFunction(TEST_SCRIPT, FUNCTION_NAME).retrieved());
		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME).result());
		assertEquals(TEST_FUNCTION.getSignature(), registry.getSignature(null, FUNCTION_NAME).retrieved());
		assertEquals(TEST_FUNCTION, registry.getFunction(null, FUNCTION_NAME).retrieved());

		registry.remove(LOCAL_TEST_FUNCTION.getSignature());
		registry.remove(TEST_FUNCTION.getSignature());
	}

	private static final Function<Boolean> TEST_FUNCTION_B = new SimpleJavaFunction<>(FUNCTION_NAME,
		new Parameter[]{
			new Parameter<>("a", DefaultClasses.BOOLEAN, true, null)
		}, DefaultClasses.BOOLEAN, true) {
		@Override
		public Boolean @Nullable [] executeSimple(Object[][] params) {
			return new Boolean[]{true};
		}
	};

	private static final Function<Boolean> TEST_FUNCTION_N = new SimpleJavaFunction<>(FUNCTION_NAME,
		new Parameter[]{
			new Parameter<>("a", DefaultClasses.NUMBER, true, null)
		}, DefaultClasses.BOOLEAN, true) {
		@Override
		public Boolean @Nullable [] executeSimple(Object[][] params) {
			return new Boolean[]{true};
		}
	};

	@Test
	public void testMultipleRegistrations() {
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME, Boolean.class).result());
		assertNull(registry.getSignature(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME, Number.class).result());
		assertNull(registry.getSignature(null, FUNCTION_NAME, Number.class).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME, Number.class).retrieved());

		registry.register(null, TEST_FUNCTION_B);

		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME, Boolean.class).result());
		assertEquals(TEST_FUNCTION_B.getSignature(), registry.getSignature(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertEquals(TEST_FUNCTION_B, registry.getFunction(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME, Number.class).result());
		assertNull(registry.getSignature(null, FUNCTION_NAME, Number.class).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME, Number.class).retrieved());

		registry.register(null, TEST_FUNCTION_N);

		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME, Boolean.class).result());
		assertEquals(TEST_FUNCTION_B.getSignature(), registry.getSignature(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertEquals(TEST_FUNCTION_B, registry.getFunction(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME, Number.class).result());
		assertEquals(TEST_FUNCTION_N.getSignature(), registry.getSignature(null, FUNCTION_NAME, Number.class).retrieved());
		assertEquals(TEST_FUNCTION_N, registry.getFunction(null, FUNCTION_NAME, Number.class).retrieved());

		assertThrows(SkriptAPIException.class, () -> registry.register(null, TEST_FUNCTION_B));
		assertThrows(SkriptAPIException.class, () -> registry.register(null, TEST_FUNCTION_N));

		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME, Boolean.class).result());
		assertEquals(TEST_FUNCTION_B.getSignature(), registry.getSignature(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertEquals(TEST_FUNCTION_B, registry.getFunction(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME, Number.class).result());
		assertEquals(TEST_FUNCTION_N.getSignature(), registry.getSignature(null, FUNCTION_NAME, Number.class).retrieved());
		assertEquals(TEST_FUNCTION_N, registry.getFunction(null, FUNCTION_NAME, Number.class).retrieved());

		registry.remove(TEST_FUNCTION_B.getSignature());
		registry.remove(TEST_FUNCTION_N.getSignature());
	}

	@Test
	public void testRegisterRemoveRegisterGlobal() {
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME, Boolean.class).result());
		assertNull(registry.getSignature(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME, Number.class).result());
		assertNull(registry.getSignature(null, FUNCTION_NAME, Number.class).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME, Number.class).retrieved());

		registry.register(null, TEST_FUNCTION_B);

		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME, Boolean.class).result());
		assertEquals(TEST_FUNCTION_B.getSignature(), registry.getSignature(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertEquals(TEST_FUNCTION_B, registry.getFunction(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME, Number.class).result());
		assertNull(registry.getSignature(null, FUNCTION_NAME, Number.class).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME, Number.class).retrieved());

		registry.remove(TEST_FUNCTION_B.getSignature());

		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME, Boolean.class).result());
		assertNull(registry.getSignature(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME, Number.class).result());
		assertNull(registry.getSignature(null, FUNCTION_NAME, Number.class).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME, Number.class).retrieved());

		registry.register(null, TEST_FUNCTION_N);

		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME, Boolean.class).result());
		assertNull(registry.getSignature(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME, Number.class).result());
		assertEquals(TEST_FUNCTION_N.getSignature(), registry.getSignature(null, FUNCTION_NAME, Number.class).retrieved());
		assertEquals(TEST_FUNCTION_N, registry.getFunction(null, FUNCTION_NAME, Number.class).retrieved());

		registry.remove(TEST_FUNCTION_N.getSignature());

		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME, Boolean.class).result());
		assertNull(registry.getSignature(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME, Number.class).result());
		assertNull(registry.getSignature(null, FUNCTION_NAME, Number.class).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME, Number.class).retrieved());

		registry.remove(TEST_FUNCTION_B.getSignature());
		registry.remove(TEST_FUNCTION_N.getSignature());
	}

	private static final Function<Boolean> LOCAL_TEST_FUNCTION_B = new SimpleJavaFunction<>(TEST_SCRIPT, FUNCTION_NAME,
		new Parameter[]{
			new Parameter<>("a", DefaultClasses.BOOLEAN, true, null)
		}, DefaultClasses.BOOLEAN, true) {
		@Override
		public Boolean @Nullable [] executeSimple(Object[][] params) {
			return new Boolean[]{true};
		}
	};

	private static final Function<Boolean> LOCAL_TEST_FUNCTION_N = new SimpleJavaFunction<>(TEST_SCRIPT, FUNCTION_NAME,
		new Parameter[]{
			new Parameter<>("a", DefaultClasses.NUMBER, true, null)
		}, DefaultClasses.BOOLEAN, true) {
		@Override
		public Boolean @Nullable [] executeSimple(Object[][] params) {
			return new Boolean[]{true};
		}
	};

	@Test
	public void testRegisterRemoveRegisterLocal() {
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Boolean.class).result());
		assertNull(registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Boolean.class).retrieved());
		assertNull(registry.getFunction(TEST_SCRIPT, FUNCTION_NAME, Boolean.class).retrieved());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Number.class).result());
		assertNull(registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Number.class).retrieved());
		assertNull(registry.getFunction(TEST_SCRIPT, FUNCTION_NAME, Number.class).retrieved());

		registry.register(TEST_SCRIPT, LOCAL_TEST_FUNCTION_B);

		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Boolean.class).result());
		assertEquals(LOCAL_TEST_FUNCTION_B.getSignature(), registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Boolean.class).retrieved());
		assertEquals(LOCAL_TEST_FUNCTION_B, registry.getFunction(TEST_SCRIPT, FUNCTION_NAME, Boolean.class).retrieved());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME, Boolean.class).result());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME, Number.class).result());

		registry.remove(LOCAL_TEST_FUNCTION_B.getSignature());

		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Boolean.class).result());
		assertNull(registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Boolean.class).retrieved());
		assertNull(registry.getFunction(TEST_SCRIPT, FUNCTION_NAME, Boolean.class).retrieved());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME, Boolean.class).result());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME, Number.class).result());

		registry.register(TEST_SCRIPT, LOCAL_TEST_FUNCTION_N);

		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Boolean.class).result());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME, Number.class).result());
		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Number.class).result());
		assertEquals(LOCAL_TEST_FUNCTION_N.getSignature(), registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Number.class).retrieved());
		assertEquals(LOCAL_TEST_FUNCTION_N, registry.getFunction(TEST_SCRIPT, FUNCTION_NAME, Number.class).retrieved());

		registry.remove(LOCAL_TEST_FUNCTION_N.getSignature());

		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Boolean.class).result());
		assertNull(registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Boolean.class).retrieved());
		assertNull(registry.getFunction(TEST_SCRIPT, FUNCTION_NAME, Boolean.class).retrieved());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Number.class).result());
		assertNull(registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Number.class).retrieved());
		assertNull(registry.getFunction(TEST_SCRIPT, FUNCTION_NAME, Number.class).retrieved());

		registry.register(TEST_SCRIPT, LOCAL_TEST_FUNCTION_N);
		registry.register(null, TEST_FUNCTION_B);

		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Number.class).result());
		assertEquals(LOCAL_TEST_FUNCTION_N.getSignature(), registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Number.class).retrieved());
		assertEquals(LOCAL_TEST_FUNCTION_N, registry.getFunction(TEST_SCRIPT, FUNCTION_NAME, Number.class).retrieved());
		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME, Boolean.class).result());
		assertEquals(TEST_FUNCTION_B.getSignature(), registry.getSignature(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertEquals(TEST_FUNCTION_B, registry.getFunction(null, FUNCTION_NAME, Boolean.class).retrieved());

		registry.remove(LOCAL_TEST_FUNCTION_N.getSignature());
		registry.remove(TEST_FUNCTION_B.getSignature());
	}

	@Test
	public void testIdentifierEmptyOf() {
		FunctionIdentifier identifier = FunctionIdentifier.of(FUNCTION_NAME, true);

		assertEquals(FUNCTION_NAME, identifier.name());
		assertTrue(identifier.local());
		assertEquals(0, identifier.minArgCount());
		assertArrayEquals(new Class[0], identifier.args());

		assertEquals(FunctionIdentifier.of(FUNCTION_NAME, true), identifier);
	}

	@Test
	public void testIdentifierOf() {
		FunctionIdentifier identifier = FunctionIdentifier.of(FUNCTION_NAME, true, Boolean.class, Number.class);

		assertEquals(FUNCTION_NAME, identifier.name());
		assertTrue(identifier.local());
		assertEquals(2, identifier.minArgCount());
		assertArrayEquals(new Class[]{Boolean.class, Number.class}, identifier.args());

		assertEquals(FunctionIdentifier.of(FUNCTION_NAME, true, Boolean.class, Number.class), identifier);
	}

	@Test
	public void testIdentifierSignatureOf() {
		SimpleJavaFunction<Boolean> function = new SimpleJavaFunction<>(FUNCTION_NAME,
			new Parameter[]{
				new Parameter<>("a", DefaultClasses.BOOLEAN, true, null),
				new Parameter<>("b", DefaultClasses.NUMBER, false, new SimpleLiteral<Number>(1, true))
			}, DefaultClasses.BOOLEAN, true) {
			@Override
			public Boolean @Nullable [] executeSimple(Object[][] params) {
				return new Boolean[]{true};
			}
		};

		FunctionIdentifier identifier = FunctionIdentifier.of(function.getSignature());

		assertEquals(FUNCTION_NAME, identifier.name());
		assertFalse(identifier.local());
		assertEquals(1, identifier.minArgCount());
		assertArrayEquals(new Class[]{Boolean.class, Number[].class}, identifier.args());

		SimpleJavaFunction<Boolean> function2 = new SimpleJavaFunction<>(FUNCTION_NAME,
			new Parameter[]{
				new Parameter<>("a", DefaultClasses.BOOLEAN, true, null),
				new Parameter<>("b", DefaultClasses.NUMBER, false, null)
			}, DefaultClasses.BOOLEAN, true) {
			@Override
			public Boolean @Nullable [] executeSimple(Object[][] params) {
				return new Boolean[]{true};
			}
		};

		assertEquals(FunctionIdentifier.of(function2.getSignature()), identifier);
	}

	// see https://github.com/SkriptLang/Skript/pull/8015
	@Test
	public void testRemoveGlobalScriptFunctions8015() {
		// create empty TEST_SCRIPT namespace such that it is not null
		registry.register(TEST_SCRIPT, LOCAL_TEST_FUNCTION);
		registry.remove(LOCAL_TEST_FUNCTION.getSignature());

		assertEquals(RetrievalResult.NOT_REGISTERED, registry.getSignature(TEST_SCRIPT, FUNCTION_NAME).result());

		// construct a global function with a non-null script, which happens in script functions
		Signature<Boolean> signature = new Signature<>(TEST_SCRIPT, FUNCTION_NAME, new Parameter<?>[0],
			false, DefaultClasses.BOOLEAN, true, "");
		SimpleJavaFunction<Boolean> fn = new SimpleJavaFunction<>(signature) {
			@Override
			public Boolean @Nullable [] executeSimple(Object[][] params) {
				return new Boolean[] { true };
			}
		};

		// ensure new behaviour
		assertThrows(IllegalArgumentException.class, () -> registry.register(TEST_SCRIPT, fn));

		registry.register(null, fn);

		assertEquals(RetrievalResult.EXACT, registry.getSignature(null, FUNCTION_NAME).result());

		registry.remove(signature);

		assertEquals(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME).result());
	}

	private static final Function<Boolean> TEST_FUNCTION_P = new SimpleJavaFunction<>(FUNCTION_NAME,
		new Parameter[]{
			new Parameter<>("a", DefaultClasses.PLAYER, true, null)
		}, DefaultClasses.BOOLEAN, true) {
		@Override
		public Boolean @Nullable [] executeSimple(Object[][] params) {
			return new Boolean[]{true};
		}
	};

	private static final Function<Boolean> TEST_FUNCTION_OP = new SimpleJavaFunction<>(FUNCTION_NAME,
			new Parameter[]{
					new Parameter<>("a", DefaultClasses.OFFLINE_PLAYER, true, null)
			}, DefaultClasses.BOOLEAN, true) {
		@Override
		public Boolean @Nullable [] executeSimple(Object[][] params) {
			return new Boolean[]{true};
		}
	};

	@Test
	public void testGetExactSignature() {
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME, Player.class).result());
		assertNull(registry.getSignature(null, FUNCTION_NAME, Player.class).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME, Player.class).retrieved());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME, OfflinePlayer.class).result());
		assertNull(registry.getSignature(null, FUNCTION_NAME, OfflinePlayer.class).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME, OfflinePlayer.class).retrieved());

		registry.register(null, TEST_FUNCTION_P);

		assertSame(RetrievalResult.EXACT, registry.getExactSignature(null, FUNCTION_NAME, Player.class).result());
		assertEquals(TEST_FUNCTION_P.getSignature(), registry.getExactSignature(null, FUNCTION_NAME, Player.class).retrieved());
		assertNull(registry.getExactSignature(null, FUNCTION_NAME, OfflinePlayer.class).retrieved());

		assertEquals(TEST_FUNCTION_P.getSignature(), registry.getSignature(null, FUNCTION_NAME, Player.class).retrieved());
		assertEquals(TEST_FUNCTION_P.getSignature(), registry.getSignature(null, FUNCTION_NAME, OfflinePlayer.class).retrieved());

		registry.remove(TEST_FUNCTION_P.getSignature());
		registry.remove(TEST_FUNCTION_OP.getSignature());
	}

	private static final String DECLARED_SCRIPT = "testFunctionRegistryDeclared";

	private static Function<Boolean> numberFunction(String name, Parameter<?>... parameters) {
		return new SimpleJavaFunction<>(name, parameters, DefaultClasses.BOOLEAN, true) {
			@Override
			public Boolean @Nullable [] executeSimple(Object[][] params) {
				return new Boolean[]{true};
			}
		};
	}

	@Test
	public void testListParameterOverloadWithUnknownArgumentType() {
		String name = "testFunctionRegistryListOverload";

		Function<Boolean> listFunction = numberFunction(name,
			new Parameter<>("ns", DefaultClasses.NUMBER, false, null));
		Function<Boolean> pairFunction = numberFunction(name,
			new Parameter<>("a", DefaultClasses.NUMBER, true, null),
			new Parameter<>("b", DefaultClasses.NUMBER, true, null));

		registry.register(null, listFunction);
		registry.register(null, pairFunction);

		try {
			// An argument of unknown type (as every variable has) must not make the positional
			// tie-break read past the end of a candidate which takes every passed argument in a
			// single list parameter. This used to throw ArrayIndexOutOfBoundsException.
			assertNotNull(registry.getFunction(null, name, Object.class, Number.class).result());
			assertNotNull(registry.getSignature(null, name, Object.class, Number.class).result());
			assertNotNull(registry.getFunction(null, name, Object.class, Object.class, Object.class).result());
		} finally {
			registry.remove(listFunction.getSignature());
			registry.remove(pairFunction.getSignature());
		}
	}

	@Test
	public void testGetDeclaredFunctions() {
		String localName = "testFunctionRegistryDeclaredLocal";
		String globalName = "testFunctionRegistryDeclaredGlobal";
		String elsewhereName = "testFunctionRegistryDeclaredElsewhere";

		assertTrue(registry.getDeclaredFunctions(DECLARED_SCRIPT).isEmpty());

		// a local function, which lives in the namespace of the script declaring it
		Function<Boolean> local = new SimpleJavaFunction<>(DECLARED_SCRIPT, localName, new Parameter[0],
			DefaultClasses.BOOLEAN, true) {
			@Override
			public Boolean @Nullable [] executeSimple(Object[][] params) {
				return new Boolean[]{true};
			}
		};

		// a global function declared in that same script, which lives in the global namespace but
		// whose signature still records where it was declared
		Signature<Boolean> globalSignature = new Signature<>(DECLARED_SCRIPT, globalName, new Parameter[0],
			false, DefaultClasses.BOOLEAN, true, (Contract) null);
		Function<Boolean> global = new SimpleJavaFunction<>(globalSignature) {
			@Override
			public Boolean @Nullable [] executeSimple(Object[][] params) {
				return new Boolean[]{true};
			}
		};

		// a global function with no declaring script at all, as Java functions have
		Function<Boolean> elsewhere = numberFunction(elsewhereName);

		registry.register(DECLARED_SCRIPT, local);
		registry.register(null, global);
		registry.register(null, elsewhere);

		try {
			Set<Function<?>> functions = registry.getDeclaredFunctions(DECLARED_SCRIPT);
			assertEquals(2, functions.size());
			assertTrue(functions.contains(local));
			assertTrue(functions.contains(global));
			assertFalse(functions.contains(elsewhere));

			// a null namespace means the functions which have no declaring script
			assertTrue(registry.getDeclaredFunctions(null).contains(elsewhere));
			assertFalse(registry.getDeclaredFunctions(null).contains(global));
		} finally {
			registry.remove(local.getSignature());
			registry.remove(globalSignature);
			registry.remove(elsewhere.getSignature());
		}

		assertTrue(registry.getDeclaredFunctions(DECLARED_SCRIPT).isEmpty());
	}

	@Test
	public void testDynamicReferencesAreNotTracked() {
		String name = "testFunctionRegistryUntracked";

		Function<Boolean> function = new SimpleJavaFunction<>(name, new Parameter[0],
			DefaultClasses.BOOLEAN, true) {
			@Override
			public Boolean @Nullable [] executeSimple(Object[][] params) {
				return new Boolean[]{true};
			}
		};

		registry.register(null, function);
		Signature<Boolean> signature = function.getSignature();

		try {
			assertTrue(signature.calls().isEmpty());

			// binding a reference obtained at runtime resolves it, which validates the underlying
			// FunctionReference; that must not register it for revalidation, or a reload would
			// warn about a binding which has already been discarded
			DynamicFunctionReference reference = new DynamicFunctionReference(name);
			assertTrue(reference.valid());

			// resolution is what validates the underlying FunctionReference, so the binding has
			// to actually succeed for the assertion below to mean anything
			assertNotNull("binding a reference to an existing function must resolve it",
				reference.bind().execute(null));

			assertTrue("a reference obtained at runtime must not be tracked",
				signature.calls().isEmpty());

			// a call written in a script opts in, and stays registered so that it is revalidated
			// again after a reload re-resolves it
			org.skriptlang.skript.common.function.FunctionReference<Boolean> tracked =
				new org.skriptlang.skript.common.function.FunctionReference<>(
					null, name, signature, new FunctionReference.Argument[0]);
			tracked.track();

			assertEquals(1, signature.calls().size());
			assertTrue(signature.calls().contains(tracked));
		} finally {
			registry.remove(signature);
		}
	}

	@Test
	public void testResultArrayHasTheFunctionsReturnType() {
		String name = "testFunctionRegistryReturnType";

		Function<Boolean> function = new SimpleJavaFunction<>(name, new Parameter[0],
			DefaultClasses.BOOLEAN, true) {
			@Override
			public Boolean @Nullable [] executeSimple(Object[][] params) {
				return new Boolean[]{true};
			}
		};

		registry.register(null, function);

		try {
			DynamicFunctionReference reference = new DynamicFunctionReference(name);

			Object[] result = reference.execute(new FunctionEvent<>(function));

			// the result is built with the function's return type as its component type, matching
			// what a function returning several values gives back
			assertNotNull(result);
			assertEquals(Boolean[].class, result.getClass());
		} finally {
			registry.remove(function.getSignature());
		}
	}

}
