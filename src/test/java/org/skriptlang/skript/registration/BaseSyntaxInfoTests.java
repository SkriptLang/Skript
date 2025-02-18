package org.skriptlang.skript.registration;

import ch.njol.skript.lang.SyntaxElement;
import org.junit.Test;
import org.skriptlang.skript.util.Priority;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * Contains base tests for SyntaxInfos.
 */
public abstract class BaseSyntaxInfoTests<E extends SyntaxElement, T extends SyntaxInfo.Builder<?, E>> {

	public abstract T builder(boolean addPattern);

	public abstract Class<E> type();

	public abstract Supplier<E> supplier();

	@Test
	public void testOrigin() {
		var info = builder(true)
				.origin(() -> "Hello World!")
				.build();
		assertEquals("Hello World!", info.origin().name());
		assertEquals("Hello World!", info.toBuilder().build().origin().name());

		var info2 = builder(true);
		info.toBuilder().applyTo(info2);
		assertEquals("Hello World!", info2.build().origin().name());
	}

	@Test
	public void testType() {
		var info = builder(true)
				.build();
		assertEquals(type(), info.type());
		assertEquals(type(), info.toBuilder().build().type());
	}

	@Test
	public void testInstance() {
		var info = builder(true)
				.build();
		assertNotNull(info.instance());

		info = builder(true)
				.supplier(supplier())
				.build();
		assertNotNull(info.instance());

		info = builder(true)
				.supplier(() -> {
					throw new UnsupportedOperationException();
				})
				.build();
		assertThrows(UnsupportedOperationException.class, info::instance);

		var info2 = builder(true);
		info.toBuilder().applyTo(info2);
		assertThrows(UnsupportedOperationException.class, () -> info2.build().instance());
	}

	@Test
	public void testPatterns() {
		var info = builder(false)
				.addPattern("1")
				.addPatterns("2")
				.addPatterns(List.of("3"))
				.build();
		assertArrayEquals(new String[]{"1", "2", "3"}, info.patterns().toArray());

		var info2 = info.toBuilder()
				.clearPatterns()
				.addPattern("4")
				.build();
		assertArrayEquals(new String[]{"4"}, info2.patterns().toArray());

		var info3 = info.toBuilder();
		info2.toBuilder().applyTo(info3);
		assertArrayEquals(new String[]{"1", "2", "3", "4"}, info3.build().patterns().toArray());
	}

	@Test
	public void testPriority() {
		final Priority base = Priority.base();

		var info = builder(true)
				.priority(base)
				.build();
		assertEquals(base, info.priority());
		assertEquals(base, info.toBuilder().build().priority());

		var info2 = builder(true);
		info.toBuilder().applyTo(info2);
		assertEquals(base, info2.build().priority());
	}

}
