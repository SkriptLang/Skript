package org.skriptlang.skript.bukkit.lang.eventvalue;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.skriptlang.skript.lang.converter.Converters;

import static org.junit.Assert.*;

public class EventValueRegistryTest {

	private EventValueRegistry registry;

	@Before
	public void setUp() {
		registry = EventValueRegistry.empty(null);
	}

	private static class TestEvent extends Event {
		@Override
		public @NotNull HandlerList getHandlers() {
			throw new UnsupportedOperationException();
		}
	}
	private static class SubTestEvent extends TestEvent {}
	private static class OtherEvent extends Event {
		@Override
		public @NotNull HandlerList getHandlers() {
			throw new UnsupportedOperationException();
		}
	}

	private static class TestValue {}
	private static class SubTestValue extends TestValue {}
	private static class OtherValue {}

	@Test
	public void testRegistration() {
		EventValue<TestEvent, TestValue> ev = EventValue.builder(TestEvent.class, TestValue.class)
				.patterns("test")
				.getter(e -> new TestValue())
				.build();

		assertFalse(registry.isRegistered(ev));
		registry.register(ev);
		assertTrue(registry.isRegistered(ev));
		assertTrue(registry.isRegistered(TestEvent.class, TestValue.class, EventValue.Time.NOW));

		assertTrue(registry.elements().contains(ev));
		assertTrue(registry.elements(EventValue.Time.NOW).contains(ev));
		assertTrue(registry.elements(TestEvent.class).contains(ev));

		assertTrue(registry.unregister(ev));
		assertFalse(registry.isRegistered(ev));
		assertFalse(registry.elements().contains(ev));
	}

	@Test
	public void testResolveByIdentifier() {
		EventValue<TestEvent, TestValue> ev = EventValue.builder(TestEvent.class, TestValue.class)
				.patterns("test pattern")
				.getter(e -> new TestValue())
				.build();
		registry.register(ev);

		EventValueRegistry.Resolution<TestEvent, TestValue> res = registry.resolve(TestEvent.class, "test pattern");
		assertTrue(res.successful());
		assertEquals(ev, res.unique());

		// Test sub-event
		EventValueRegistry.Resolution<SubTestEvent, TestValue> subRes = registry.resolve(SubTestEvent.class, "test pattern");
		assertTrue(subRes.successful());
		// It should be a converted event value
		assertEquals(ev.valueClass(), subRes.unique().valueClass());
		assertEquals(TestEvent.class, subRes.unique().eventClass());

		// Test non-matching identifier
		assertFalse(registry.resolve(TestEvent.class, "wrong").successful());
	}

	@Test
	public void testResolveByValueClass() {
		EventValue<TestEvent, TestValue> ev = EventValue.builder(TestEvent.class, TestValue.class)
				.getter(e -> new TestValue())
				.build();
		registry.register(ev);

		EventValueRegistry.Resolution<TestEvent, ? extends TestValue> res = registry.resolve(TestEvent.class, TestValue.class);
		assertTrue(res.successful());
		assertEquals(ev, res.unique());

		EventValueRegistry.Resolution<TestEvent, ? extends SubTestValue> subValueRes = registry.resolve(TestEvent.class, SubTestValue.class);
		assertTrue(subValueRes.successful());
	}

	@Test
	public void testCacheLogic() {
		EventValue<TestEvent, TestValue> ev = EventValue.builder(TestEvent.class, TestValue.class)
				.patterns("test")
				.getter(e -> new TestValue())
				.build();
		registry.register(ev);

		EventValueRegistry.Resolution<TestEvent, TestValue> res1 = registry.resolve(TestEvent.class, "test");
		EventValueRegistry.Resolution<TestEvent, TestValue> res2 = registry.resolve(TestEvent.class, "test");

		assertSame("Resolutions should be cached and return the same instance", res1, res2);

		// Registering a new value should clear the cache
		EventValue<OtherEvent, TestValue> ev2 = EventValue.builder(OtherEvent.class, TestValue.class)
				.patterns("other")
				.getter(e -> new TestValue())
				.build();
		registry.register(ev2);

		EventValueRegistry.Resolution<TestEvent, TestValue> res3 = registry.resolve(TestEvent.class, "test");
		assertNotSame("Cache should have been cleared", res1, res3);
		assertEquals(res1, res3);
	}

	@Test
	public void testContextDependentNotCached() {
		EventValue<TestEvent, TestValue> ev = EventValue.builder(TestEvent.class, TestValue.class)
				.patterns("test")
				.getter(e -> new TestValue())
				.contextDependent()
				.build();
		registry.register(ev);

		EventValueRegistry.Resolution<TestEvent, TestValue> res1 = registry.resolve(TestEvent.class, "test");
		EventValueRegistry.Resolution<TestEvent, TestValue> res2 = registry.resolve(TestEvent.class, "test");

		assertNotSame("Context-dependent resolutions should not be cached", res1, res2);
		assertEquals(res1, res2);
	}

	@Test
	public void testUnmodifiableView() {
		EventValue<TestEvent, TestValue> ev = EventValue.builder(TestEvent.class, TestValue.class)
				.patterns("test")
				.getter(e -> new TestValue())
				.build();
		registry.register(ev);

		EventValueRegistry unmodifiable = registry.unmodifiableView();
		assertTrue(unmodifiable.isRegistered(ev));

		assertThrows(UnsupportedOperationException.class, () -> unmodifiable.register(ev));
		assertThrows(UnsupportedOperationException.class, () -> unmodifiable.unregister(ev));
	}

	@Test
	public void testTimeFallback() {
		EventValue<TestEvent, TestValue> evPast = EventValue.builder(TestEvent.class, TestValue.class)
				.patterns("test")
				.time(EventValue.Time.PAST)
				.getter(e -> new TestValue())
				.build();
		registry.register(evPast);

		// Resolve past
		assertTrue(registry.resolve(TestEvent.class, "test", EventValue.Time.PAST).successful());
		// Resolve now (should fail)
		assertFalse(registry.resolve(TestEvent.class, "test", EventValue.Time.NOW).successful());

		EventValue<TestEvent, TestValue> evNow = EventValue.builder(TestEvent.class, TestValue.class)
				.patterns("test")
				.time(EventValue.Time.NOW)
				.getter(e -> new TestValue())
				.build();
		registry.register(evNow);

		// Resolve with fallback
		EventValueRegistry.Resolution<TestEvent, TestValue> res = registry.resolve(
				TestEvent.class, "test", EventValue.Time.FUTURE, EventValueRegistry.Flags.of(EventValueRegistry.Flag.FALLBACK_TO_DEFAULT_TIME_STATE));
		assertTrue(res.successful());
		assertEquals(evNow.valueClass(), res.unique().valueClass());
		assertEquals(EventValue.Time.NOW, res.unique().time());
	}

	@Test
	public void testResolveExact() {
		EventValue<TestEvent, TestValue> ev = EventValue.builder(TestEvent.class, TestValue.class)
				.getter(e -> new TestValue())
				.build();
		registry.register(ev);

		assertTrue(registry.resolveExact(TestEvent.class, TestValue.class, EventValue.Time.NOW).successful());
		assertTrue(registry.resolveExact(SubTestEvent.class, TestValue.class, EventValue.Time.NOW).successful());
		assertFalse(registry.resolveExact(TestEvent.class, SubTestEvent.class, EventValue.Time.NOW).successful());
	}

	@Test
	public void testAmbiguousResolution() {
		EventValue<TestEvent, Integer> ev1 = EventValue.builder(TestEvent.class, Integer.class)
				.patterns("test")
				.getter(e -> 10)
				.build();
		EventValue<TestEvent, Double> ev2 = EventValue.builder(TestEvent.class, Double.class)
				.patterns("test")
				.getter(e -> 10.0)
				.build();
		registry.register(ev1);
		registry.register(ev2);

		EventValueRegistry.Resolution<TestEvent, ?> res = registry.resolve(TestEvent.class, "test");
		assertTrue(res.successful());
		assertTrue(res.multiple());
		assertEquals(2, res.size());
		assertNotNull(res.any());
		assertTrue(res.anyOptional().isPresent());
		assertThrows(IllegalStateException.class, res::unique);
	}

	@Test
	public void testAbortValidation() {
		EventValue<TestEvent, TestValue> ev = EventValue.builder(TestEvent.class, TestValue.class)
				.patterns("test")
				.eventValidator(event -> event.equals(SubTestEvent.class) ? EventValue.Validation.ABORT : EventValue.Validation.VALID)
				.getter(e -> new TestValue())
				.build();
		registry.register(ev);

		assertTrue(registry.resolve(TestEvent.class, "test").successful());
		EventValueRegistry.Resolution<SubTestEvent, TestValue> res = registry.resolve(SubTestEvent.class, "test");
		assertFalse(res.successful());
		assertTrue(res.errored());
	}

	@Test
	public void testDowncastConversion() {
		EventValue<TestEvent, TestValue> ev = EventValue.builder(TestEvent.class, TestValue.class)
				.getter(e -> new TestValue())
				.build();
		registry.register(ev);

		// Resolve for SubTestValue (subtype of TestValue) should work with ALLOW_CONVERSION
		EventValueRegistry.Resolution<TestEvent, ? extends SubTestValue> res = registry.resolve(TestEvent.class, SubTestValue.class, EventValue.Time.NOW,
				EventValueRegistry.Flags.of(EventValueRegistry.Flag.ALLOW_CONVERSION));
		assertTrue(res.successful());
		assertEquals(SubTestValue.class, res.unique().valueClass());
	}

	@Test
	public void testConverter() {
		EventValue<TestEvent, TestValue> ev = EventValue.builder(TestEvent.class, TestValue.class)
				.getter(e -> new TestValue())
				.build();
		registry.register(ev);

		// Register a converter from TestValue to OtherValue
		Converters.registerConverter(TestValue.class, OtherValue.class, from -> new OtherValue());

		// Resolve for OtherValue should work with ALLOW_CONVERSION
		EventValueRegistry.Resolution<TestEvent, ? extends OtherValue> res = registry.resolve(TestEvent.class, OtherValue.class, EventValue.Time.NOW,
				EventValueRegistry.Flags.of(EventValueRegistry.Flag.ALLOW_CONVERSION));
		assertTrue(res.successful());
		assertEquals(OtherValue.class, res.unique().valueClass());
	}
}
