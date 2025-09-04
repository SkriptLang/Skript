package ch.njol.skript.lang.function;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.lang.function.FunctionRegistry.FunctionIdentifier;
import ch.njol.skript.lang.function.FunctionRegistry.RetrievalResult;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.DefaultClasses;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

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

}
