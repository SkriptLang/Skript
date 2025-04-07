package ch.njol.skript.lang.function;

import ch.njol.skript.SkriptAPIException;
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
		FunctionRegistry.register(LOCAL_TEST_FUNCTION);

		assertTrue(FunctionRegistry.signatureExists(TEST_SCRIPT, FUNCTION_NAME));
		assertEquals(LOCAL_TEST_FUNCTION.getSignature(), FunctionRegistry.signature(TEST_SCRIPT, FUNCTION_NAME));
		assertEquals(LOCAL_TEST_FUNCTION, FunctionRegistry.function(TEST_SCRIPT, FUNCTION_NAME));
		assertTrue(FunctionRegistry.signatureExists(null, FUNCTION_NAME));
		assertEquals(LOCAL_TEST_FUNCTION.getSignature(), FunctionRegistry.signature(null, FUNCTION_NAME));
		assertEquals(LOCAL_TEST_FUNCTION, FunctionRegistry.function(null, FUNCTION_NAME));

		FunctionRegistry.remove(LOCAL_TEST_FUNCTION.getSignature());
	}

	private static final Function<Boolean> TEST_FUNCTION_B = new SimpleJavaFunction<>(FUNCTION_NAME,
		new Parameter[]{
			new Parameter<>("a", DefaultClasses.BOOLEAN, false, null)
		}, DefaultClasses.BOOLEAN, true) {
		@Override
		public Boolean @Nullable [] executeSimple(Object[][] params) {
			return new Boolean[]{true};
		}
	};

	private static final Function<Boolean> TEST_FUNCTION_N = new SimpleJavaFunction<>(FUNCTION_NAME,
		new Parameter[]{
			new Parameter<>("a", DefaultClasses.NUMBER, false, null)
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
			new Parameter<>("a", DefaultClasses.BOOLEAN, false, null)
		}, DefaultClasses.BOOLEAN, true, true) {
		@Override
		public Boolean @Nullable [] executeSimple(Object[][] params) {
			return new Boolean[]{true};
		}
	};

	private static final Function<Boolean> LOCAL_TEST_FUNCTION_N = new SimpleJavaFunction<>(FUNCTION_NAME,
		new Parameter[]{
			new Parameter<>("a", DefaultClasses.NUMBER, false, null)
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
		FunctionRegistry.register(null, LOCAL_TEST_FUNCTION_B);

		assertTrue(FunctionRegistry.signatureExists(TEST_SCRIPT, FUNCTION_NAME, Number.class));
		assertEquals(LOCAL_TEST_FUNCTION_N.getSignature(), FunctionRegistry.signature(TEST_SCRIPT, FUNCTION_NAME, Number.class));
		assertEquals(LOCAL_TEST_FUNCTION_N, FunctionRegistry.function(TEST_SCRIPT, FUNCTION_NAME, Number.class));
		assertTrue(FunctionRegistry.signatureExists(null, FUNCTION_NAME, Boolean.class));
		assertEquals(LOCAL_TEST_FUNCTION_B.getSignature(), FunctionRegistry.signature(null, FUNCTION_NAME, Boolean.class));
		assertEquals(LOCAL_TEST_FUNCTION_B, FunctionRegistry.function(null, FUNCTION_NAME, Boolean.class));

		FunctionRegistry.remove(LOCAL_TEST_FUNCTION_N.getSignature());
		FunctionRegistry.remove(LOCAL_TEST_FUNCTION_B.getSignature());
	}

	// todo add test for two same functions in diff namespaces

}
