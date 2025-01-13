package org.skriptlang.skript.registration;

import ch.njol.skript.lang.SyntaxElement;
import org.junit.Test;
import org.skriptlang.skript.registration.SyntaxRegistry.Key;
import org.skriptlang.skript.util.Priority;

import static org.junit.Assert.*;

public class SyntaxRegistryTest {

	private static SyntaxRegistry syntaxRegistry() {
		return SyntaxRegistry.empty();
	}

	private static SyntaxInfo<?> info() {
		return SyntaxInfo.builder(SyntaxElement.class)
			.supplier(() -> {
				throw new UnsupportedOperationException();
			})
			.addPattern("default")
			.build();
	}

	private static Key<SyntaxInfo<?>> key() {
		return key("TestKey");
	}

	private static Key<SyntaxInfo<?>> key(String name) {
		return Key.of(name);
	}

	@Test
	public void testBasic() {
		final SyntaxRegistry registry = syntaxRegistry();
		final SyntaxRegistry unmodifiable = registry.unmodifiableView();
		final var info = info();

		// test registration
		registry.register(key(), info);
		assertThrows(UnsupportedOperationException.class, () -> unmodifiable.register(key(), info));
		assertArrayEquals(new SyntaxInfo[]{info}, registry.syntaxes(key()).toArray());
		assertArrayEquals(new SyntaxInfo[]{info}, unmodifiable.syntaxes(key()).toArray());
		assertArrayEquals(new SyntaxInfo[]{info}, registry.elements().toArray());
		assertArrayEquals(new SyntaxInfo[]{info}, unmodifiable.elements().toArray());

		// test unregistration
		registry.unregister(key(), info);
		assertThrows(UnsupportedOperationException.class, () -> unmodifiable.unregister(key(), info));
		assertTrue(registry.syntaxes(key()).isEmpty());
		assertTrue(unmodifiable.syntaxes(key()).isEmpty());
		assertTrue(registry.elements().isEmpty());
		assertTrue(unmodifiable.elements().isEmpty());
	}

	@Test
	public void testKeylessUnregistration() {
		final SyntaxRegistry registry = syntaxRegistry();
		final SyntaxRegistry unmodifiable = registry.unmodifiableView();
		final var info = info();

		registry.register(key(), info);
		registry.register(key("OtherKey"), info);
		// should not contain duplicates
		assertArrayEquals(new SyntaxInfo[]{info}, registry.elements().toArray());

		registry.unregister(info);
		assertThrows(UnsupportedOperationException.class, () -> unmodifiable.unregister(info));
		assertTrue(registry.elements().isEmpty());
		assertTrue(unmodifiable.elements().isEmpty());
	}

	@Test
	public void testOrdering() {
		final SyntaxRegistry registry = syntaxRegistry();
		final SyntaxRegistry unmodifiable = registry.unmodifiableView();
		final Priority priority = Priority.base();

		// ordering should be info2, info1, info3
		final var info1 = info().toBuilder().priority(priority).build();
		final var info2 = info().toBuilder().priority(Priority.before(priority)).build();
		final var info3 = info().toBuilder().priority(Priority.after(priority)).build();

		// test multiple registrations (differing order)
		registry.register(key(), info3);
		registry.register(key(), info2);
		registry.register(key(), info1);

		assertArrayEquals(new SyntaxInfo[]{info2, info1, info3}, registry.syntaxes(key()).toArray());
		assertArrayEquals(new SyntaxInfo[]{info2, info1, info3}, unmodifiable.syntaxes(key()).toArray());
	}

}
