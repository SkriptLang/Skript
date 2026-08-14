package org.skriptlang.skript.common.function;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.lang.function.Signature;
import ch.njol.skript.lang.function.SimpleJavaFunction;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.DefaultClasses;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.skriptlang.skript.common.function.FunctionRegistry.RetrievalResult;
import org.skriptlang.skript.common.function.FunctionRegistryImpl.FunctionIdentifier;

import static org.junit.Assert.*;

public class FunctionRegistryTest {

	private static final FunctionRegistry registry = FunctionRegistry.empty(Skript.instance());
	private static final String FUNCTION_NAME = "testFunctionRegistry";
	private static final String TEST_SCRIPT = "test";

	private static final Function<Boolean> TEST_FUNCTION = DefaultFunction.builder(Skript.instance(), FUNCTION_NAME, Boolean.class)
			.build(b -> true);

	@Test
	public void testGetFunctionRetrieval() {
		assertEquals(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME).result());

		assertEquals(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME).result());
		assertNull(registry.getSignature(FUNCTION_NAME).retrieved());
		assertNull(registry.getSignature(FUNCTION_NAME).conflictingArgs());

		assertEquals(RetrievalResult.NOT_REGISTERED, registry.getFunction(FUNCTION_NAME).result());
		assertNull(registry.getFunction(FUNCTION_NAME).retrieved());
		assertNull(registry.getFunction(FUNCTION_NAME).conflictingArgs());

		registry.register(TEST_FUNCTION);

		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME).result());

		assertEquals(RetrievalResult.EXACT, registry.getSignature(FUNCTION_NAME).result());
		assertEquals(TEST_FUNCTION.signature(), registry.getSignature(FUNCTION_NAME).retrieved());
		assertNull(registry.getSignature(FUNCTION_NAME).conflictingArgs());

		assertEquals(RetrievalResult.EXACT, registry.getFunction(FUNCTION_NAME).result());
		assertEquals(TEST_FUNCTION, registry.getFunction(FUNCTION_NAME).retrieved());
		assertNull(registry.getFunction(FUNCTION_NAME).conflictingArgs());

		registry.remove(TEST_FUNCTION.signature());
	}

	@Test
	public void testSimpleMultipleRegistrationsFunction() {
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME).result());
		assertNull(registry.getSignature(FUNCTION_NAME).retrieved());
		assertNull(registry.getFunction(FUNCTION_NAME).retrieved());

		registry.register(TEST_FUNCTION);

		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME).result());
		assertEquals(TEST_FUNCTION.signature(), registry.getSignature(FUNCTION_NAME).retrieved());
		assertEquals(TEST_FUNCTION, registry.getFunction(FUNCTION_NAME).retrieved());

		assertThrows(SkriptAPIException.class, () -> registry.register(TEST_FUNCTION));

		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME).result());
		assertEquals(TEST_FUNCTION.signature(), registry.getSignature(FUNCTION_NAME).retrieved());
		assertEquals(TEST_FUNCTION, registry.getFunction(FUNCTION_NAME).retrieved());

		registry.remove(TEST_FUNCTION.signature());
	}

	@Test
	public void testSimpleRegisterRemoveRegisterGlobal() {
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME).result());
		assertNull(registry.getSignature(FUNCTION_NAME).retrieved());
		assertNull(registry.getFunction(FUNCTION_NAME).retrieved());

		registry.register(TEST_FUNCTION);

		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME).result());
		assertEquals(TEST_FUNCTION.signature(), registry.getSignature(FUNCTION_NAME).retrieved());
		assertEquals(TEST_FUNCTION, registry.getFunction(FUNCTION_NAME).retrieved());

		registry.remove(TEST_FUNCTION.signature());

		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME).result());
		assertNull(registry.getSignature(FUNCTION_NAME).retrieved());
		assertNull(registry.getFunction(FUNCTION_NAME).retrieved());

		registry.register(TEST_FUNCTION);

		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME).result());
		assertEquals(TEST_FUNCTION.signature(), registry.getSignature(FUNCTION_NAME).retrieved());
		assertEquals(TEST_FUNCTION, registry.getFunction(FUNCTION_NAME).retrieved());

		registry.remove(TEST_FUNCTION.signature());
	}

	@Test
	public void testSimpleRegisterRemoveRegisterLocal() {
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(TEST_SCRIPT, FUNCTION_NAME).result());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME).result());
		assertNull(registry.getSignature(FUNCTION_NAME).retrieved());
		assertNull(registry.getFunction(FUNCTION_NAME).retrieved());

		registry.register(TEST_SCRIPT, LOCAL_TEST_FUNCTION);

		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(TEST_SCRIPT, FUNCTION_NAME).result());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME).result());
		assertEquals(LOCAL_TEST_FUNCTION.signature(), registry.getSignature(TEST_SCRIPT, FUNCTION_NAME).retrieved());
		assertEquals(LOCAL_TEST_FUNCTION, registry.getFunction(TEST_SCRIPT, FUNCTION_NAME).retrieved());

		registry.remove(LOCAL_TEST_FUNCTION.signature());

		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(TEST_SCRIPT, FUNCTION_NAME).result());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME).result());
		assertNull(registry.getSignature(TEST_SCRIPT, FUNCTION_NAME).retrieved());
		assertNull(registry.getSignature(FUNCTION_NAME).retrieved());
		assertNull(registry.getFunction(TEST_SCRIPT, FUNCTION_NAME).retrieved());
		assertNull(registry.getFunction(FUNCTION_NAME).retrieved());

		registry.register(TEST_SCRIPT, LOCAL_TEST_FUNCTION);
		registry.register(TEST_FUNCTION);

		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(TEST_SCRIPT, FUNCTION_NAME).result());
		assertEquals(LOCAL_TEST_FUNCTION.signature(), registry.getSignature(TEST_SCRIPT, FUNCTION_NAME).retrieved());
		assertEquals(LOCAL_TEST_FUNCTION, registry.getFunction(TEST_SCRIPT, FUNCTION_NAME).retrieved());
		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME).result());
		assertEquals(TEST_FUNCTION.signature(), registry.getSignature(FUNCTION_NAME).retrieved());
		assertEquals(TEST_FUNCTION, registry.getFunction(FUNCTION_NAME).retrieved());

		registry.remove(LOCAL_TEST_FUNCTION.signature());
		registry.remove(TEST_FUNCTION.signature());
	}

	private static final Function<Boolean> TEST_FUNCTION_B = DefaultFunction.builder(Skript.instance(), FUNCTION_NAME, Boolean.class)
			.parameter("a", Boolean.class)
			.build(b -> true);

	private static final Function<Boolean> TEST_FUNCTION_N = DefaultFunction.builder(Skript.instance(), FUNCTION_NAME, Boolean.class)
			.parameter("a", Number.class)
			.build(b -> true);

	@Test
	public void testMultipleRegistrations() {
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME, Boolean.class).result());
		assertNull(registry.getSignature(FUNCTION_NAME, Boolean.class).retrieved());
		assertNull(registry.getFunction(FUNCTION_NAME, Boolean.class).retrieved());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME, Number.class).result());
		assertNull(registry.getSignature(FUNCTION_NAME, Number.class).retrieved());
		assertNull(registry.getFunction(FUNCTION_NAME, Number.class).retrieved());

		registry.register(TEST_FUNCTION_B);

		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME, Boolean.class).result());
		assertEquals(TEST_FUNCTION_B.signature(), registry.getSignature(FUNCTION_NAME, Boolean.class).retrieved());
		assertEquals(TEST_FUNCTION_B, registry.getFunction(FUNCTION_NAME, Boolean.class).retrieved());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME, Number.class).result());
		assertNull(registry.getSignature(FUNCTION_NAME, Number.class).retrieved());
		assertNull(registry.getFunction(FUNCTION_NAME, Number.class).retrieved());

		registry.register(TEST_FUNCTION_N);

		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME, Boolean.class).result());
		assertEquals(TEST_FUNCTION_B.signature(), registry.getSignature(FUNCTION_NAME, Boolean.class).retrieved());
		assertEquals(TEST_FUNCTION_B, registry.getFunction(FUNCTION_NAME, Boolean.class).retrieved());
		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME, Number.class).result());
		assertEquals(TEST_FUNCTION_N.signature(), registry.getSignature(FUNCTION_NAME, Number.class).retrieved());
		assertEquals(TEST_FUNCTION_N, registry.getFunction(FUNCTION_NAME, Number.class).retrieved());

		assertThrows(SkriptAPIException.class, () -> registry.register(TEST_FUNCTION_B));
		assertThrows(SkriptAPIException.class, () -> registry.register(TEST_FUNCTION_N));

		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME, Boolean.class).result());
		assertEquals(TEST_FUNCTION_B.signature(), registry.getSignature(FUNCTION_NAME, Boolean.class).retrieved());
		assertEquals(TEST_FUNCTION_B, registry.getFunction(FUNCTION_NAME, Boolean.class).retrieved());
		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME, Number.class).result());
		assertEquals(TEST_FUNCTION_N.signature(), registry.getSignature(FUNCTION_NAME, Number.class).retrieved());
		assertEquals(TEST_FUNCTION_N, registry.getFunction(FUNCTION_NAME, Number.class).retrieved());

		registry.remove(TEST_FUNCTION_B.signature());
		registry.remove(TEST_FUNCTION_N.signature());
	}

	@Test
	public void testRegisterRemoveRegisterGlobal() {
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME, Boolean.class).result());
		assertNull(registry.getSignature(FUNCTION_NAME, Boolean.class).retrieved());
		assertNull(registry.getFunction(FUNCTION_NAME, Boolean.class).retrieved());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME, Number.class).result());
		assertNull(registry.getSignature(FUNCTION_NAME, Number.class).retrieved());
		assertNull(registry.getFunction(FUNCTION_NAME, Number.class).retrieved());

		registry.register(TEST_FUNCTION_B);

		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME, Boolean.class).result());
		assertEquals(TEST_FUNCTION_B.signature(), registry.getSignature(FUNCTION_NAME, Boolean.class).retrieved());
		assertEquals(TEST_FUNCTION_B, registry.getFunction(FUNCTION_NAME, Boolean.class).retrieved());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME, Number.class).result());
		assertNull(registry.getSignature(FUNCTION_NAME, Number.class).retrieved());
		assertNull(registry.getFunction(FUNCTION_NAME, Number.class).retrieved());

		registry.remove(TEST_FUNCTION_B.signature());

		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME, Boolean.class).result());
		assertNull(registry.getSignature(FUNCTION_NAME, Boolean.class).retrieved());
		assertNull(registry.getFunction(FUNCTION_NAME, Boolean.class).retrieved());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME, Number.class).result());
		assertNull(registry.getSignature(FUNCTION_NAME, Number.class).retrieved());
		assertNull(registry.getFunction(FUNCTION_NAME, Number.class).retrieved());

		registry.register(TEST_FUNCTION_N);

		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME, Boolean.class).result());
		assertNull(registry.getSignature(FUNCTION_NAME, Boolean.class).retrieved());
		assertNull(registry.getFunction(FUNCTION_NAME, Boolean.class).retrieved());
		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME, Number.class).result());
		assertEquals(TEST_FUNCTION_N.signature(), registry.getSignature(FUNCTION_NAME, Number.class).retrieved());
		assertEquals(TEST_FUNCTION_N, registry.getFunction(FUNCTION_NAME, Number.class).retrieved());

		registry.remove(TEST_FUNCTION_N.signature());

		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME, Boolean.class).result());
		assertNull(registry.getSignature(FUNCTION_NAME, Boolean.class).retrieved());
		assertNull(registry.getFunction(FUNCTION_NAME, Boolean.class).retrieved());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME, Number.class).result());
		assertNull(registry.getSignature(FUNCTION_NAME, Number.class).retrieved());
		assertNull(registry.getFunction(FUNCTION_NAME, Number.class).retrieved());

		registry.remove(TEST_FUNCTION_B.signature());
		registry.remove(TEST_FUNCTION_N.signature());
	}

	private static final Function<Boolean> LOCAL_TEST_FUNCTION_B = DefaultFunction.builder(Skript.instance(), FUNCTION_NAME, Boolean.class)
			.local(TEST_SCRIPT)
			.parameter("a", Boolean.class)
			.build(b -> true);

	private static final Function<Boolean> LOCAL_TEST_FUNCTION_N = DefaultFunction.builder(Skript.instance(), FUNCTION_NAME, Boolean.class)
			.local(TEST_SCRIPT)
			.parameter("a", Number.class)
			.build(b -> true);

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
		assertEquals(LOCAL_TEST_FUNCTION_B.signature(), registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Boolean.class).retrieved());
		assertEquals(LOCAL_TEST_FUNCTION_B, registry.getFunction(TEST_SCRIPT, FUNCTION_NAME, Boolean.class).retrieved());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME, Boolean.class).result());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME, Number.class).result());

		registry.remove(LOCAL_TEST_FUNCTION_B.signature());

		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Boolean.class).result());
		assertNull(registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Boolean.class).retrieved());
		assertNull(registry.getFunction(TEST_SCRIPT, FUNCTION_NAME, Boolean.class).retrieved());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME, Boolean.class).result());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME, Number.class).result());

		registry.register(TEST_SCRIPT, LOCAL_TEST_FUNCTION_N);

		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Boolean.class).result());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME, Number.class).result());
		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Number.class).result());
		assertEquals(LOCAL_TEST_FUNCTION_N.signature(), registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Number.class).retrieved());
		assertEquals(LOCAL_TEST_FUNCTION_N, registry.getFunction(TEST_SCRIPT, FUNCTION_NAME, Number.class).retrieved());

		registry.remove(LOCAL_TEST_FUNCTION_N.signature());

		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Boolean.class).result());
		assertNull(registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Boolean.class).retrieved());
		assertNull(registry.getFunction(TEST_SCRIPT, FUNCTION_NAME, Boolean.class).retrieved());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Number.class).result());
		assertNull(registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Number.class).retrieved());
		assertNull(registry.getFunction(TEST_SCRIPT, FUNCTION_NAME, Number.class).retrieved());

		registry.register(TEST_SCRIPT, LOCAL_TEST_FUNCTION_N);
		registry.register(TEST_FUNCTION_B);

		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Number.class).result());
		assertEquals(LOCAL_TEST_FUNCTION_N.signature(), registry.getSignature(TEST_SCRIPT, FUNCTION_NAME, Number.class).retrieved());
		assertEquals(LOCAL_TEST_FUNCTION_N, registry.getFunction(TEST_SCRIPT, FUNCTION_NAME, Number.class).retrieved());
		assertNotSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME, Boolean.class).result());
		assertEquals(TEST_FUNCTION_B.signature(), registry.getSignature(FUNCTION_NAME, Boolean.class).retrieved());
		assertEquals(TEST_FUNCTION_B, registry.getFunction(FUNCTION_NAME, Boolean.class).retrieved());

		registry.remove(LOCAL_TEST_FUNCTION_N.signature());
		registry.remove(TEST_FUNCTION_B.signature());
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
				new ch.njol.skript.lang.function.Parameter[]{
						new ch.njol.skript.lang.function.Parameter<>("a", DefaultClasses.BOOLEAN, true, null),
						new ch.njol.skript.lang.function.Parameter<>("b", DefaultClasses.NUMBER, false, new SimpleLiteral<Number>(1, true))
				}, DefaultClasses.BOOLEAN, true) {
			@Override
			public Boolean @Nullable [] executeSimple(Object[][] params) {
				return new Boolean[]{true};
			}
		};

		FunctionIdentifier identifier = FunctionIdentifier.of(function.signature());

		assertEquals(FUNCTION_NAME, identifier.name());
		assertFalse(identifier.local());
		assertEquals(1, identifier.minArgCount());
		assertArrayEquals(new Class[]{Boolean.class, Number[].class}, identifier.args());

		SimpleJavaFunction<Boolean> function2 = new SimpleJavaFunction<>(FUNCTION_NAME,
				new ch.njol.skript.lang.function.Parameter[]{
						new ch.njol.skript.lang.function.Parameter<>("a", DefaultClasses.BOOLEAN, true, null),
						new ch.njol.skript.lang.function.Parameter<>("b", DefaultClasses.NUMBER, false, null)
				}, DefaultClasses.BOOLEAN, true) {
			@Override
			public Boolean @Nullable [] executeSimple(Object[][] params) {
				return new Boolean[]{true};
			}
		};

		assertEquals(FunctionIdentifier.of(function2.signature()), identifier);
	}

	private static final Function<Boolean> LOCAL_TEST_FUNCTION = DefaultFunction.builder(Skript.instance(), FUNCTION_NAME, Boolean.class)
			.local(TEST_SCRIPT)
			.build(b -> true);

	// see https://github.com/SkriptLang/Skript/pull/8015
	@Test
	public void testRemoveGlobalScriptFunctions8015() {
		// create empty TEST_SCRIPT namespace such that it is not null
		registry.register(TEST_SCRIPT, LOCAL_TEST_FUNCTION);
		registry.remove(LOCAL_TEST_FUNCTION.signature());

		assertEquals(RetrievalResult.NOT_REGISTERED, registry.getSignature(TEST_SCRIPT, FUNCTION_NAME).result());

		// construct a global function with a non-null script, which happens in script functions
		ch.njol.skript.lang.function.Signature<Boolean> signature = new Signature<>(TEST_SCRIPT, FUNCTION_NAME, new ch.njol.skript.lang.function.Parameter<?>[0],
				false, DefaultClasses.BOOLEAN, true, "");
		SimpleJavaFunction<Boolean> fn = new SimpleJavaFunction<>(signature) {
			@Override
			public Boolean @Nullable [] executeSimple(Object[][] params) {
				return new Boolean[]{true};
			}
		};

		// ensure new behavior
		assertThrows(IllegalArgumentException.class, () -> registry.register(TEST_SCRIPT, fn));

		registry.register(fn);

		assertEquals(RetrievalResult.EXACT, registry.getSignature(FUNCTION_NAME).result());

		registry.remove(signature);

		assertEquals(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME).result());
	}

	private static final Function<Boolean> TEST_FUNCTION_P = DefaultFunction.builder(Skript.instance(), FUNCTION_NAME, Boolean.class)
			.parameter("a", Player.class)
			.build(b -> true);

	private static final Function<Boolean> TEST_FUNCTION_OP = DefaultFunction.builder(Skript.instance(), FUNCTION_NAME, Boolean.class)
			.parameter("a", OfflinePlayer.class)
			.build(b -> true);

	@Test
	public void testGetExactSignature() {
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME, Player.class).result());
		assertNull(registry.getSignature(FUNCTION_NAME, Player.class).retrieved());
		assertNull(registry.getFunction(FUNCTION_NAME, Player.class).retrieved());
		assertSame(RetrievalResult.NOT_REGISTERED, registry.getSignature(FUNCTION_NAME, OfflinePlayer.class).result());
		assertNull(registry.getSignature(FUNCTION_NAME, OfflinePlayer.class).retrieved());
		assertNull(registry.getFunction(FUNCTION_NAME, OfflinePlayer.class).retrieved());

		registry.register(TEST_FUNCTION_P);

		assertSame(RetrievalResult.EXACT, registry.getExactSignature(FUNCTION_NAME, Player.class).result());
		assertEquals(TEST_FUNCTION_P.signature(), registry.getExactSignature(FUNCTION_NAME, Player.class).retrieved());
		assertNull(registry.getExactSignature(FUNCTION_NAME, OfflinePlayer.class).retrieved());

		assertEquals(TEST_FUNCTION_P.signature(), registry.getSignature(FUNCTION_NAME, Player.class).retrieved());
		assertEquals(TEST_FUNCTION_P.signature(), registry.getSignature(FUNCTION_NAME, OfflinePlayer.class).retrieved());

		registry.remove(TEST_FUNCTION_P.signature());
		registry.remove(TEST_FUNCTION_OP.signature());
	}

}
