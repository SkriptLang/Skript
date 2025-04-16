package ch.njol.skript.lang.function;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.lang.function.FunctionRegistry.FunctionIdentifier;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.DefaultClasses;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import static org.junit.Assert.*;

public class FunctionRegistryTest {

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
	public void testSimpleMultipleRegistrationsFunction() {
		assertFalse(FunctionRegistry.signatureExists(null, FUNCTION_NAME));
		assertNull(FunctionRegistry.signature(null, FUNCTION_NAME));
		assertNull(FunctionRegistry.function(null, FUNCTION_NAME));

		FunctionRegistry.register(TEST_FUNCTION);

		assertTrue(FunctionRegistry.signatureExists(null, FUNCTION_NAME));
		assertEquals(TEST_FUNCTION.getSignature(), FunctionRegistry.signature(null, FUNCTION_NAME));
		assertEquals(TEST_FUNCTION, FunctionRegistry.function(null, FUNCTION_NAME));

		assertThrows(SkriptAPIException.class, () -> FunctionRegistry.register(TEST_FUNCTION));

		assertTrue(FunctionRegistry.signatureExists(null, FUNCTION_NAME));
		assertEquals(TEST_FUNCTION.getSignature(), FunctionRegistry.signature(null, FUNCTION_NAME));
		assertEquals(TEST_FUNCTION, FunctionRegistry.function(null, FUNCTION_NAME));

		FunctionRegistry.remove(TEST_FUNCTION.getSignature());
	}

	@Test
	public void testSimpleRegisterRemoveRegisterGlobal() {
		assertFalse(FunctionRegistry.signatureExists(null, FUNCTION_NAME));
		assertNull(FunctionRegistry.signature(null, FUNCTION_NAME));
		assertNull(FunctionRegistry.function(null, FUNCTION_NAME));

		FunctionRegistry.register(TEST_FUNCTION);

		assertTrue(FunctionRegistry.signatureExists(null, FUNCTION_NAME));
		assertEquals(TEST_FUNCTION.getSignature(), FunctionRegistry.signature(null, FUNCTION_NAME));
		assertEquals(TEST_FUNCTION, FunctionRegistry.function(null, FUNCTION_NAME));

		FunctionRegistry.remove(TEST_FUNCTION.getSignature());

		assertFalse(FunctionRegistry.signatureExists(null, FUNCTION_NAME));
		assertNull(FunctionRegistry.signature(null, FUNCTION_NAME));
		assertNull(FunctionRegistry.function(null, FUNCTION_NAME));

		FunctionRegistry.register(TEST_FUNCTION);

		assertTrue(FunctionRegistry.signatureExists(null, FUNCTION_NAME));
		assertEquals(TEST_FUNCTION.getSignature(), FunctionRegistry.signature(null, FUNCTION_NAME));
		assertEquals(TEST_FUNCTION, FunctionRegistry.function(null, FUNCTION_NAME));

		FunctionRegistry.remove(TEST_FUNCTION.getSignature());
	}

	private static final Function<Boolean> LOCAL_TEST_FUNCTION = new SimpleJavaFunction<>(FUNCTION_NAME, new Parameter[0],
		DefaultClasses.BOOLEAN, true, true) {
		@Override
		public Boolean @Nullable [] executeSimple(Object[][] params) {
			return new Boolean[]{true};
		}
	};

	@Test
	public void testSimpleRegisterRemoveRegisterLocal() {
		assertFalse(FunctionRegistry.signatureExists(TEST_SCRIPT, FUNCTION_NAME));
		assertFalse(FunctionRegistry.signatureExists(null, FUNCTION_NAME));
		assertNull(FunctionRegistry.signature(null, FUNCTION_NAME));
		assertNull(FunctionRegistry.function(null, FUNCTION_NAME));

		FunctionRegistry.register(TEST_SCRIPT, LOCAL_TEST_FUNCTION);

		assertTrue(FunctionRegistry.signatureExists(TEST_SCRIPT, FUNCTION_NAME));
		assertFalse(FunctionRegistry.signatureExists(null, FUNCTION_NAME));
		assertEquals(LOCAL_TEST_FUNCTION.getSignature(), FunctionRegistry.signature(TEST_SCRIPT, FUNCTION_NAME));
		assertEquals(LOCAL_TEST_FUNCTION, FunctionRegistry.function(TEST_SCRIPT, FUNCTION_NAME));

		FunctionRegistry.remove(LOCAL_TEST_FUNCTION.getSignature());

		assertFalse(FunctionRegistry.signatureExists(TEST_SCRIPT, FUNCTION_NAME));
		assertFalse(FunctionRegistry.signatureExists(null, FUNCTION_NAME));
		assertNull(FunctionRegistry.signature(null, FUNCTION_NAME));
		assertNull(FunctionRegistry.function(null, FUNCTION_NAME));

		FunctionRegistry.register(TEST_SCRIPT, LOCAL_TEST_FUNCTION);
		FunctionRegistry.register(TEST_FUNCTION);

		assertTrue(FunctionRegistry.signatureExists(TEST_SCRIPT, FUNCTION_NAME));
		assertEquals(LOCAL_TEST_FUNCTION.getSignature(), FunctionRegistry.signature(TEST_SCRIPT, FUNCTION_NAME));
		assertEquals(LOCAL_TEST_FUNCTION, FunctionRegistry.function(TEST_SCRIPT, FUNCTION_NAME));
		assertTrue(FunctionRegistry.signatureExists(null, FUNCTION_NAME));
		assertEquals(TEST_FUNCTION.getSignature(), FunctionRegistry.signature(null, FUNCTION_NAME));
		assertEquals(TEST_FUNCTION, FunctionRegistry.function(null, FUNCTION_NAME));

		FunctionRegistry.remove(LOCAL_TEST_FUNCTION.getSignature());
		FunctionRegistry.remove(TEST_FUNCTION.getSignature());
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
		assertFalse(FunctionRegistry.signatureExists(null, FUNCTION_NAME, Boolean.class));
		assertNull(FunctionRegistry.signature(null, FUNCTION_NAME, Boolean.class));
		assertNull(FunctionRegistry.function(null, FUNCTION_NAME, Boolean.class));
		assertFalse(FunctionRegistry.signatureExists(null, FUNCTION_NAME, Number.class));
		assertNull(FunctionRegistry.signature(null, FUNCTION_NAME, Number.class));
		assertNull(FunctionRegistry.function(null, FUNCTION_NAME, Number.class));

		FunctionRegistry.register(TEST_FUNCTION_B);

		assertTrue(FunctionRegistry.signatureExists(null, FUNCTION_NAME, Boolean.class));
		assertEquals(TEST_FUNCTION_B.getSignature(), FunctionRegistry.signature(null, FUNCTION_NAME, Boolean.class));
		assertEquals(TEST_FUNCTION_B, FunctionRegistry.function(null, FUNCTION_NAME, Boolean.class));
		assertFalse(FunctionRegistry.signatureExists(null, FUNCTION_NAME, Number.class));
		assertNull(FunctionRegistry.signature(null, FUNCTION_NAME, Number.class));
		assertNull(FunctionRegistry.function(null, FUNCTION_NAME, Number.class));

		FunctionRegistry.register(TEST_FUNCTION_N);

		assertTrue(FunctionRegistry.signatureExists(null, FUNCTION_NAME, Boolean.class));
		assertEquals(TEST_FUNCTION_B.getSignature(), FunctionRegistry.signature(null, FUNCTION_NAME, Boolean.class));
		assertEquals(TEST_FUNCTION_B, FunctionRegistry.function(null, FUNCTION_NAME, Boolean.class));
		assertTrue(FunctionRegistry.signatureExists(null, FUNCTION_NAME, Number.class));
		assertEquals(TEST_FUNCTION_N.getSignature(), FunctionRegistry.signature(null, FUNCTION_NAME, Number.class));
		assertEquals(TEST_FUNCTION_N, FunctionRegistry.function(null, FUNCTION_NAME, Number.class));

		assertThrows(SkriptAPIException.class, () -> FunctionRegistry.register(TEST_FUNCTION_B));
		assertThrows(SkriptAPIException.class, () -> FunctionRegistry.register(TEST_FUNCTION_N));

		assertTrue(FunctionRegistry.signatureExists(null, FUNCTION_NAME, Boolean.class));
		assertEquals(TEST_FUNCTION_B.getSignature(), FunctionRegistry.signature(null, FUNCTION_NAME, Boolean.class));
		assertEquals(TEST_FUNCTION_B, FunctionRegistry.function(null, FUNCTION_NAME, Boolean.class));
		assertTrue(FunctionRegistry.signatureExists(null, FUNCTION_NAME, Number.class));
		assertEquals(TEST_FUNCTION_N.getSignature(), FunctionRegistry.signature(null, FUNCTION_NAME, Number.class));
		assertEquals(TEST_FUNCTION_N, FunctionRegistry.function(null, FUNCTION_NAME, Number.class));

		FunctionRegistry.remove(TEST_FUNCTION_B.getSignature());
		FunctionRegistry.remove(TEST_FUNCTION_N.getSignature());
	}

	@Test
	public void testRegisterRemoveRegisterGlobal() {
		assertFalse(FunctionRegistry.signatureExists(null, FUNCTION_NAME, Boolean.class));
		assertNull(FunctionRegistry.signature(null, FUNCTION_NAME, Boolean.class));
		assertNull(FunctionRegistry.function(null, FUNCTION_NAME, Boolean.class));
		assertFalse(FunctionRegistry.signatureExists(null, FUNCTION_NAME, Number.class));
		assertNull(FunctionRegistry.signature(null, FUNCTION_NAME, Number.class));
		assertNull(FunctionRegistry.function(null, FUNCTION_NAME, Number.class));

		FunctionRegistry.register(TEST_FUNCTION_B);

		assertTrue(FunctionRegistry.signatureExists(null, FUNCTION_NAME, Boolean.class));
		assertEquals(TEST_FUNCTION_B.getSignature(), FunctionRegistry.signature(null, FUNCTION_NAME, Boolean.class));
		assertEquals(TEST_FUNCTION_B, FunctionRegistry.function(null, FUNCTION_NAME, Boolean.class));
		assertFalse(FunctionRegistry.signatureExists(null, FUNCTION_NAME, Number.class));
		assertNull(FunctionRegistry.signature(null, FUNCTION_NAME, Number.class));
		assertNull(FunctionRegistry.function(null, FUNCTION_NAME, Number.class));

		FunctionRegistry.remove(TEST_FUNCTION_B.getSignature());

		assertFalse(FunctionRegistry.signatureExists(null, FUNCTION_NAME, Boolean.class));
		assertNull(FunctionRegistry.signature(null, FUNCTION_NAME, Boolean.class));
		assertNull(FunctionRegistry.function(null, FUNCTION_NAME, Boolean.class));
		assertFalse(FunctionRegistry.signatureExists(null, FUNCTION_NAME, Number.class));
		assertNull(FunctionRegistry.signature(null, FUNCTION_NAME, Number.class));
		assertNull(FunctionRegistry.function(null, FUNCTION_NAME, Number.class));

		FunctionRegistry.register(TEST_FUNCTION_N);

		assertFalse(FunctionRegistry.signatureExists(null, FUNCTION_NAME, Boolean.class));
		assertNull(FunctionRegistry.signature(null, FUNCTION_NAME, Boolean.class));
		assertNull(FunctionRegistry.function(null, FUNCTION_NAME, Boolean.class));
		assertTrue(FunctionRegistry.signatureExists(null, FUNCTION_NAME, Number.class));
		assertEquals(TEST_FUNCTION_N.getSignature(), FunctionRegistry.signature(null, FUNCTION_NAME, Number.class));
		assertEquals(TEST_FUNCTION_N, FunctionRegistry.function(null, FUNCTION_NAME, Number.class));

		FunctionRegistry.remove(TEST_FUNCTION_N.getSignature());

		assertFalse(FunctionRegistry.signatureExists(null, FUNCTION_NAME, Boolean.class));
		assertNull(FunctionRegistry.signature(null, FUNCTION_NAME, Boolean.class));
		assertNull(FunctionRegistry.function(null, FUNCTION_NAME, Boolean.class));
		assertFalse(FunctionRegistry.signatureExists(null, FUNCTION_NAME, Number.class));
		assertNull(FunctionRegistry.signature(null, FUNCTION_NAME, Number.class));
		assertNull(FunctionRegistry.function(null, FUNCTION_NAME, Number.class));

		FunctionRegistry.remove(TEST_FUNCTION_B.getSignature());
		FunctionRegistry.remove(TEST_FUNCTION_N.getSignature());
	}

	private static final Function<Boolean> LOCAL_TEST_FUNCTION_B = new SimpleJavaFunction<>(FUNCTION_NAME,
		new Parameter[]{
			new Parameter<>("a", DefaultClasses.BOOLEAN, true, null)
		}, DefaultClasses.BOOLEAN, true, true) {
		@Override
		public Boolean @Nullable [] executeSimple(Object[][] params) {
			return new Boolean[]{true};
		}
	};

	private static final Function<Boolean> LOCAL_TEST_FUNCTION_N = new SimpleJavaFunction<>(FUNCTION_NAME,
		new Parameter[]{
			new Parameter<>("a", DefaultClasses.NUMBER, true, null)
		}, DefaultClasses.BOOLEAN, true, true) {
		@Override
		public Boolean @Nullable [] executeSimple(Object[][] params) {
			return new Boolean[]{true};
		}
	};

	@Test
	public void testRegisterRemoveRegisterLocal() {
		assertFalse(FunctionRegistry.signatureExists(TEST_SCRIPT, FUNCTION_NAME, Boolean.class));
		assertNull(FunctionRegistry.signature(TEST_SCRIPT, FUNCTION_NAME, Boolean.class));
		assertNull(FunctionRegistry.function(TEST_SCRIPT, FUNCTION_NAME, Boolean.class));
		assertFalse(FunctionRegistry.signatureExists(TEST_SCRIPT, FUNCTION_NAME, Number.class));
		assertNull(FunctionRegistry.signature(TEST_SCRIPT, FUNCTION_NAME, Number.class));
		assertNull(FunctionRegistry.function(TEST_SCRIPT, FUNCTION_NAME, Number.class));

		FunctionRegistry.register(TEST_SCRIPT, LOCAL_TEST_FUNCTION_B);

		assertTrue(FunctionRegistry.signatureExists(TEST_SCRIPT, FUNCTION_NAME, Boolean.class));
		assertEquals(LOCAL_TEST_FUNCTION_B.getSignature(), FunctionRegistry.signature(TEST_SCRIPT, FUNCTION_NAME, Boolean.class));
		assertEquals(LOCAL_TEST_FUNCTION_B, FunctionRegistry.function(TEST_SCRIPT, FUNCTION_NAME, Boolean.class));
		assertFalse(FunctionRegistry.signatureExists(null, FUNCTION_NAME, Boolean.class));
		assertFalse(FunctionRegistry.signatureExists(null, FUNCTION_NAME, Number.class));

		FunctionRegistry.remove(LOCAL_TEST_FUNCTION_B.getSignature());

		assertFalse(FunctionRegistry.signatureExists(TEST_SCRIPT, FUNCTION_NAME, Boolean.class));
		assertNull(FunctionRegistry.signature(TEST_SCRIPT, FUNCTION_NAME, Boolean.class));
		assertNull(FunctionRegistry.function(TEST_SCRIPT, FUNCTION_NAME, Boolean.class));
		assertFalse(FunctionRegistry.signatureExists(null, FUNCTION_NAME, Boolean.class));
		assertFalse(FunctionRegistry.signatureExists(null, FUNCTION_NAME, Number.class));

		FunctionRegistry.register(TEST_SCRIPT, LOCAL_TEST_FUNCTION_N);

		assertFalse(FunctionRegistry.signatureExists(TEST_SCRIPT, FUNCTION_NAME, Boolean.class));
		assertFalse(FunctionRegistry.signatureExists(null, FUNCTION_NAME, Number.class));
		assertTrue(FunctionRegistry.signatureExists(TEST_SCRIPT, FUNCTION_NAME, Number.class));
		assertEquals(LOCAL_TEST_FUNCTION_N.getSignature(), FunctionRegistry.signature(TEST_SCRIPT, FUNCTION_NAME, Number.class));
		assertEquals(LOCAL_TEST_FUNCTION_N, FunctionRegistry.function(TEST_SCRIPT, FUNCTION_NAME, Number.class));

		FunctionRegistry.remove(LOCAL_TEST_FUNCTION_N.getSignature());

		assertFalse(FunctionRegistry.signatureExists(TEST_SCRIPT, FUNCTION_NAME, Boolean.class));
		assertNull(FunctionRegistry.signature(TEST_SCRIPT, FUNCTION_NAME, Boolean.class));
		assertNull(FunctionRegistry.function(TEST_SCRIPT, FUNCTION_NAME, Boolean.class));
		assertFalse(FunctionRegistry.signatureExists(TEST_SCRIPT, FUNCTION_NAME, Number.class));
		assertNull(FunctionRegistry.signature(TEST_SCRIPT, FUNCTION_NAME, Number.class));
		assertNull(FunctionRegistry.function(TEST_SCRIPT, FUNCTION_NAME, Number.class));

		FunctionRegistry.register(TEST_SCRIPT, LOCAL_TEST_FUNCTION_N);
		FunctionRegistry.register(null, TEST_FUNCTION_B);

		assertTrue(FunctionRegistry.signatureExists(TEST_SCRIPT, FUNCTION_NAME, Number.class));
		assertEquals(LOCAL_TEST_FUNCTION_N.getSignature(), FunctionRegistry.signature(TEST_SCRIPT, FUNCTION_NAME, Number.class));
		assertEquals(LOCAL_TEST_FUNCTION_N, FunctionRegistry.function(TEST_SCRIPT, FUNCTION_NAME, Number.class));
		assertTrue(FunctionRegistry.signatureExists(null, FUNCTION_NAME, Boolean.class));
		assertEquals(TEST_FUNCTION_B.getSignature(), FunctionRegistry.signature(null, FUNCTION_NAME, Boolean.class));
		assertEquals(TEST_FUNCTION_B, FunctionRegistry.function(null, FUNCTION_NAME, Boolean.class));

		FunctionRegistry.remove(LOCAL_TEST_FUNCTION_N.getSignature());
		FunctionRegistry.remove(TEST_FUNCTION_B.getSignature());
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
