package ch.njol.skript.lang.function;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.lang.function.FunctionRegistry.FunctionIdentifier;
import ch.njol.skript.lang.function.FunctionRegistry.RetrievalResult;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.DefaultClasses;
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
		assertFalse(registry.signatureExists(null, FUNCTION_NAME));

		assertEquals(RetrievalResult.NOT_REGISTERED, registry.getSignature(null, FUNCTION_NAME).result());
		assertNull(registry.getSignature(null, FUNCTION_NAME).retrieved());
		assertNull(registry.getSignature(null, FUNCTION_NAME).conflictingArgs());

		assertEquals(RetrievalResult.NOT_REGISTERED, registry.getFunction(null, FUNCTION_NAME).result());
		assertNull(registry.getFunction(null, FUNCTION_NAME).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME).conflictingArgs());

		registry.register(TEST_FUNCTION);

		assertTrue(registry.signatureExists(null, FUNCTION_NAME));

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
		assertFalse(registry.signatureExists(null, FUNCTION_NAME));
		assertNull(registry.getSignature(null, FUNCTION_NAME).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME).retrieved());

		registry.register(TEST_FUNCTION);

		assertTrue(registry.signatureExists(null, FUNCTION_NAME));
		assertEquals(TEST_FUNCTION.getSignature(), registry.getSignature(null, FUNCTION_NAME).retrieved());
		assertEquals(TEST_FUNCTION, registry.getFunction(null, FUNCTION_NAME).retrieved());

		assertThrows(SkriptAPIException.class, () -> registry.register(TEST_FUNCTION));

		assertTrue(registry.signatureExists(null, FUNCTION_NAME));
		assertEquals(TEST_FUNCTION.getSignature(), registry.getSignature(null, FUNCTION_NAME).retrieved());
		assertEquals(TEST_FUNCTION, registry.getFunction(null, FUNCTION_NAME).retrieved());

		registry.remove(TEST_FUNCTION.getSignature());
	}

	@Test
	public void testSimpleRegisterRemoveRegisterGlobal() {
		assertFalse(registry.signatureExists(null, FUNCTION_NAME));
		assertNull(registry.getSignature(null, FUNCTION_NAME).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME).retrieved());

		registry.register(TEST_FUNCTION);

		assertTrue(registry.signatureExists(null, FUNCTION_NAME));
		assertEquals(TEST_FUNCTION.getSignature(), registry.getSignature(null, FUNCTION_NAME).retrieved());
		assertEquals(TEST_FUNCTION, registry.getFunction(null, FUNCTION_NAME).retrieved());

		registry.remove(TEST_FUNCTION.getSignature());

		assertFalse(registry.signatureExists(null, FUNCTION_NAME));
		assertNull(registry.getSignature(null, FUNCTION_NAME).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME).retrieved());

		registry.register(TEST_FUNCTION);

		assertTrue(registry.signatureExists(null, FUNCTION_NAME));
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
		assertFalse(registry.signatureExists(TEST_SCRIPT, FUNCTION_NAME));
		assertFalse(registry.signatureExists(null, FUNCTION_NAME));
		assertNull(registry.getSignature(null, FUNCTION_NAME).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME).retrieved());

		registry.register(TEST_SCRIPT, LOCAL_TEST_FUNCTION);

		assertTrue(registry.signatureExists(TEST_SCRIPT, FUNCTION_NAME));
		assertFalse(registry.signatureExists(null, FUNCTION_NAME));
		assertEquals(LOCAL_TEST_FUNCTION.getSignature(), registry.getSignature(TEST_SCRIPT, FUNCTION_NAME).retrieved());
		assertEquals(LOCAL_TEST_FUNCTION, registry.getFunction(TEST_SCRIPT, FUNCTION_NAME).retrieved());

		registry.remove(LOCAL_TEST_FUNCTION.getSignature());

		assertFalse(registry.signatureExists(TEST_SCRIPT, FUNCTION_NAME));
		assertFalse(registry.signatureExists(null, FUNCTION_NAME));
		assertNull(registry.getSignature(TEST_SCRIPT, FUNCTION_NAME).retrieved());
		assertNull(registry.getSignature(null, FUNCTION_NAME).retrieved());
		assertNull(registry.getFunction(TEST_SCRIPT, FUNCTION_NAME).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME).retrieved());

		registry.register(TEST_SCRIPT, LOCAL_TEST_FUNCTION);
		registry.register(TEST_FUNCTION);

		assertTrue(registry.signatureExists(TEST_SCRIPT, FUNCTION_NAME));
		assertEquals(LOCAL_TEST_FUNCTION.getSignature(), registry.getSignature(TEST_SCRIPT, FUNCTION_NAME).retrieved());
		assertEquals(LOCAL_TEST_FUNCTION, registry.getFunction(TEST_SCRIPT, FUNCTION_NAME).retrieved());
		assertTrue(registry.signatureExists(null, FUNCTION_NAME));
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
		assertFalse(registry.signatureExists(null, FUNCTION_NAME, Boolean.class));
		assertNull(registry.getSignature(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertFalse(registry.signatureExists(null, FUNCTION_NAME, Number.class));
		assertNull(registry.getSignature(null, FUNCTION_NAME, Number.class).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME, Number.class).retrieved());

		registry.register(TEST_FUNCTION_B);

		assertTrue(registry.signatureExists(null, FUNCTION_NAME, Boolean.class));
		assertEquals(TEST_FUNCTION_B.getSignature(), registry.getSignature(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertEquals(TEST_FUNCTION_B, registry.getFunction(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertFalse(registry.signatureExists(null, FUNCTION_NAME, Number.class));
		assertNull(registry.getSignature(null, FUNCTION_NAME, Number.class).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME, Number.class).retrieved());

		registry.register(TEST_FUNCTION_N);

		assertTrue(registry.signatureExists(null, FUNCTION_NAME, Boolean.class));
		assertEquals(TEST_FUNCTION_B.getSignature(), registry.getSignature(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertEquals(TEST_FUNCTION_B, registry.getFunction(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertTrue(registry.signatureExists(null, FUNCTION_NAME, Number.class));
		assertEquals(TEST_FUNCTION_N.getSignature(), registry.getSignature(null, FUNCTION_NAME, Number.class).retrieved());
		assertEquals(TEST_FUNCTION_N, registry.getFunction(null, FUNCTION_NAME, Number.class).retrieved());

		assertThrows(SkriptAPIException.class, () -> registry.register(TEST_FUNCTION_B));
		assertThrows(SkriptAPIException.class, () -> registry.register(TEST_FUNCTION_N));

		assertTrue(registry.signatureExists(null, FUNCTION_NAME, Boolean.class));
		assertEquals(TEST_FUNCTION_B.getSignature(), registry.getSignature(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertEquals(TEST_FUNCTION_B, registry.getFunction(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertTrue(registry.signatureExists(null, FUNCTION_NAME, Number.class));
		assertEquals(TEST_FUNCTION_N.getSignature(), registry.getSignature(null, FUNCTION_NAME, Number.class).retrieved());
		assertEquals(TEST_FUNCTION_N, registry.getFunction(null, FUNCTION_NAME, Number.class).retrieved());

		registry.remove(TEST_FUNCTION_B.getSignature());
		registry.remove(TEST_FUNCTION_N.getSignature());
	}

	@Test
	public void testRegisterRemoveRegisterGlobal() {
		assertFalse(registry.signatureExists(null, FUNCTION_NAME, Boolean.class));
		assertNull(registry.getSignature(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertFalse(registry.signatureExists(null, FUNCTION_NAME, Number.class));
		assertNull(registry.getSignature(null, FUNCTION_NAME, Number.class).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME, Number.class).retrieved());

		registry.register(TEST_FUNCTION_B);

		assertTrue(registry.signatureExists(null, FUNCTION_NAME, Boolean.class));
		assertEquals(TEST_FUNCTION_B.getSignature(), registry.getSignature(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertEquals(TEST_FUNCTION_B, registry.getFunction(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertFalse(registry.signatureExists(null, FUNCTION_NAME, Number.class));
		assertNull(registry.getSignature(null, FUNCTION_NAME, Number.class).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME, Number.class).retrieved());

		registry.remove(TEST_FUNCTION_B.getSignature());

		assertFalse(registry.signatureExists(null, FUNCTION_NAME, Boolean.class));
		assertNull(registry.getSignature(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertFalse(registry.signatureExists(null, FUNCTION_NAME, Number.class));
		assertNull(registry.getSignature(null, FUNCTION_NAME, Number.class).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME, Number.class).retrieved());

		registry.register(TEST_FUNCTION_N);

		assertFalse(registry.signatureExists(null, FUNCTION_NAME, Boolean.class));
		assertNull(registry.getSignature(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertTrue(registry.signatureExists(null, FUNCTION_NAME, Number.class));
		assertEquals(TEST_FUNCTION_N.getSignature(), registry.getSignature(null, FUNCTION_NAME, Number.class).retrieved());
		assertEquals(TEST_FUNCTION_N, registry.getFunction(null, FUNCTION_NAME, Number.class).retrieved());

		registry.remove(TEST_FUNCTION_N.getSignature());

		assertFalse(registry.signatureExists(null, FUNCTION_NAME, Boolean.class));
		assertNull(registry.getSignature(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertNull(registry.getFunction(null, FUNCTION_NAME, Boolean.class).retrieved());
		assertFalse(registry.signatureExists(null, FUNCTION_NAME, Number.class));
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
		assertFalse(registry.signatureExists(TEST_SCRIPT, FUNCTION_NAME, Boolean.class));
		assertNull(registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Boolean.class).retrieved());
		assertNull(registry.getFunction(TEST_SCRIPT, FUNCTION_NAME, Boolean.class).retrieved());
		assertFalse(registry.signatureExists(TEST_SCRIPT, FUNCTION_NAME, Number.class));
		assertNull(registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Number.class).retrieved());
		assertNull(registry.getFunction(TEST_SCRIPT, FUNCTION_NAME, Number.class).retrieved());

		registry.register(TEST_SCRIPT, LOCAL_TEST_FUNCTION_B);

		assertTrue(registry.signatureExists(TEST_SCRIPT, FUNCTION_NAME, Boolean.class));
		assertEquals(LOCAL_TEST_FUNCTION_B.getSignature(), registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Boolean.class).retrieved());
		assertEquals(LOCAL_TEST_FUNCTION_B, registry.getFunction(TEST_SCRIPT, FUNCTION_NAME, Boolean.class).retrieved());
		assertFalse(registry.signatureExists(null, FUNCTION_NAME, Boolean.class));
		assertFalse(registry.signatureExists(null, FUNCTION_NAME, Number.class));

		registry.remove(LOCAL_TEST_FUNCTION_B.getSignature());

		assertFalse(registry.signatureExists(TEST_SCRIPT, FUNCTION_NAME, Boolean.class));
		assertNull(registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Boolean.class).retrieved());
		assertNull(registry.getFunction(TEST_SCRIPT, FUNCTION_NAME, Boolean.class).retrieved());
		assertFalse(registry.signatureExists(null, FUNCTION_NAME, Boolean.class));
		assertFalse(registry.signatureExists(null, FUNCTION_NAME, Number.class));

		registry.register(TEST_SCRIPT, LOCAL_TEST_FUNCTION_N);

		assertFalse(registry.signatureExists(TEST_SCRIPT, FUNCTION_NAME, Boolean.class));
		assertFalse(registry.signatureExists(null, FUNCTION_NAME, Number.class));
		assertTrue(registry.signatureExists(TEST_SCRIPT, FUNCTION_NAME, Number.class));
		assertEquals(LOCAL_TEST_FUNCTION_N.getSignature(), registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Number.class).retrieved());
		assertEquals(LOCAL_TEST_FUNCTION_N, registry.getFunction(TEST_SCRIPT, FUNCTION_NAME, Number.class).retrieved());

		registry.remove(LOCAL_TEST_FUNCTION_N.getSignature());

		assertFalse(registry.signatureExists(TEST_SCRIPT, FUNCTION_NAME, Boolean.class));
		assertNull(registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Boolean.class).retrieved());
		assertNull(registry.getFunction(TEST_SCRIPT, FUNCTION_NAME, Boolean.class).retrieved());
		assertFalse(registry.signatureExists(TEST_SCRIPT, FUNCTION_NAME, Number.class));
		assertNull(registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Number.class).retrieved());
		assertNull(registry.getFunction(TEST_SCRIPT, FUNCTION_NAME, Number.class).retrieved());

		registry.register(TEST_SCRIPT, LOCAL_TEST_FUNCTION_N);
		registry.register(null, TEST_FUNCTION_B);

		assertTrue(registry.signatureExists(TEST_SCRIPT, FUNCTION_NAME, Number.class));
		assertEquals(LOCAL_TEST_FUNCTION_N.getSignature(), registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Number.class).retrieved());
		assertEquals(LOCAL_TEST_FUNCTION_N, registry.getFunction(TEST_SCRIPT, FUNCTION_NAME, Number.class).retrieved());
		assertTrue(registry.signatureExists(null, FUNCTION_NAME, Boolean.class));
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

}
