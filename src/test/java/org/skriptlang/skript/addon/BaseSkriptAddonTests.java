package org.skriptlang.skript.addon;

import org.junit.Test;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.skriptlang.skript.util.Registry;

import java.util.Collection;

import static org.junit.Assert.*;

public abstract class BaseSkriptAddonTests {

	private static class MockRegistry implements Registry<Object> {

		@Override
		public Collection<Object> elements() {
			throw new UnsupportedOperationException();
		}

	}

	public abstract SkriptAddon addon();

	public abstract Class<?> source();

	public abstract String name();

	@Test
	public void testSource() {
		final SkriptAddon addon = addon();

		assertEquals(source(), addon.source());
		assertEquals(source(), addon.unmodifiableView().source());
	}

	@Test
	public void testName() {
		final SkriptAddon addon = addon();

		assertEquals(name(), addon.name());
		assertEquals(name(), addon.unmodifiableView().name());
	}

	@Test
	public void testRegistry() {
		final SkriptAddon addon = addon();
		final SkriptAddon unmodifiable = addon.unmodifiableView();
		final MockRegistry registry = new MockRegistry();

		// storing a registry
		addon.storeRegistry(MockRegistry.class, registry);
		assertThrows(UnsupportedOperationException.class, () -> unmodifiable.storeRegistry(MockRegistry.class, registry));

		// get a registry
		assertEquals(registry, addon.registry(MockRegistry.class));
		assertEquals(registry, unmodifiable.registry(MockRegistry.class));

		// has a registry
		assertTrue(addon.hasRegistry(MockRegistry.class));
		assertTrue(unmodifiable.hasRegistry(MockRegistry.class));

		// remove a registry
		addon.removeRegistry(MockRegistry.class);
		assertThrows(UnsupportedOperationException.class, () -> unmodifiable.removeRegistry(MockRegistry.class));

		// get a registry
		assertThrows(NullPointerException.class, () -> addon.registry(MockRegistry.class));
		assertThrows(NullPointerException.class, () -> unmodifiable.registry(MockRegistry.class));

		// has a registry
		assertFalse(addon.hasRegistry(MockRegistry.class));
		assertFalse(unmodifiable.hasRegistry(MockRegistry.class));

		// get a registry (alternate)
		addon.registry(MockRegistry.class, () -> registry);
		assertEquals(registry, addon.registry(MockRegistry.class));
		assertEquals(registry, unmodifiable.registry(MockRegistry.class));
		assertTrue(addon.hasRegistry(MockRegistry.class));
		assertTrue(unmodifiable.hasRegistry(MockRegistry.class));
	}

	@Test
	public void testSyntaxRegistry() {
		final SkriptAddon addon = addon();
		final SkriptAddon unmodifiable = addon.unmodifiableView();

		assertNotNull(addon.syntaxRegistry());
		assertNotNull(unmodifiable.syntaxRegistry());
		// unmodifiable's syntax registry should be unmodifiable (different)
		assertNotEquals(addon.syntaxRegistry(), unmodifiable.syntaxRegistry());
		assertEquals(addon.registry(SyntaxRegistry.class), addon.syntaxRegistry());
	}

	@Test
	public void testLocalizer() {
		final SkriptAddon addon = addon();

		assertNotNull(addon.localizer());
		assertNotNull(addon.unmodifiableView().localizer());
	}

}
