package org.skriptlang.skript.bukkit.registration;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptEvent.ListeningBehavior;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos.Event.Builder;
import org.skriptlang.skript.bukkit.registration.EventSyntaxInfoTest.MockSkriptEvent;
import org.skriptlang.skript.registration.BaseSyntaxInfoTests;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.Assert.*;

public class EventSyntaxInfoTest extends BaseSyntaxInfoTests<MockSkriptEvent, BukkitSyntaxInfos.Event.Builder<?, MockSkriptEvent>> {

	public static final class MockSkriptEvent extends SkriptEvent {

		@Override
		public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean check(Event event) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString(@Nullable Event event, boolean debug) {
			throw new UnsupportedOperationException();
		}

	}

	@Override
	public Builder<?, MockSkriptEvent> builder(boolean addPattern) {
		var info = BukkitSyntaxInfos.Event.builder(MockSkriptEvent.class, "mock event");
		if (addPattern) {
			info.addPattern("default");
		}
		return info;
	}

	@Override
	public Class<MockSkriptEvent> type() {
		return MockSkriptEvent.class;
	}

	@Override
	public Supplier<MockSkriptEvent> supplier() {
		return MockSkriptEvent::new;
	}

	@Test
	public void testListeningBehavior() {
		for (final var behavior : ListeningBehavior.values()) {
			var info = builder(true)
					.listeningBehavior(behavior)
					.build();
			assertEquals(behavior, info.listeningBehavior());
			assertEquals(behavior, info.toBuilder().build().listeningBehavior());

			var info2 = builder(true);
			info.toBuilder().applyTo(info2);
			assertEquals(behavior, info2.build().listeningBehavior());
		}
	}

	@Test
	public void testSince() {
		var info = builder(true)
				.since("since")
				.build();
		assertEquals("since", info.since());
		assertEquals("since", info.toBuilder().build().since());

		var info2 = builder(true);
		info.toBuilder().applyTo(info2);
		assertEquals("since", info2.build().since());
	}

	@Test
	public void testDocumentationId() {
		var info = builder(true)
			.documentationId("id")
			.build();
		assertEquals("id", info.documentationId());
		assertEquals("id", info.toBuilder().build().documentationId());

		var info2 = builder(true);
		info.toBuilder().applyTo(info2);
		assertEquals("id", info2.build().documentationId());
	}

	@Test
	public void testDescription() {
		var info = builder(true)
			.addDescription("1")
			.addDescription(new String[]{"2"})
			.addDescription(List.of("3"))
			.build();
		assertArrayEquals(new String[]{"1", "2", "3"}, info.description().toArray());

		var info2 = info.toBuilder()
			.clearDescription()
			.addDescription("4")
			.build();
		assertArrayEquals(new String[]{"4"}, info2.description().toArray());

		var info3 = info.toBuilder();
		info2.toBuilder().applyTo(info3);
		assertArrayEquals(new String[]{"1", "2", "3", "4"}, info3.build().description().toArray());
	}

	@Test
	public void testExamples() {
		var info = builder(true)
			.addExample("1")
			.addExamples("2")
			.addExamples(List.of("3"))
			.build();
		assertArrayEquals(new String[]{"1", "2", "3"}, info.examples().toArray());

		var info2 = info.toBuilder()
			.clearExamples()
			.addExample("4")
			.build();
		assertArrayEquals(new String[]{"4"}, info2.examples().toArray());

		var info3 = info.toBuilder();
		info2.toBuilder().applyTo(info3);
		assertArrayEquals(new String[]{"1", "2", "3", "4"}, info3.build().examples().toArray());
	}

	@Test
	public void testKeywords() {
		var info = builder(true)
			.addKeyword("1")
			.addKeywords("2")
			.addKeywords(List.of("3"))
			.build();
		assertArrayEquals(new String[]{"1", "2", "3"}, info.keywords().toArray());

		var info2 = info.toBuilder()
			.clearKeywords()
			.addKeyword("4")
			.build();
		assertArrayEquals(new String[]{"4"}, info2.keywords().toArray());

		var info3 = info.toBuilder();
		info2.toBuilder().applyTo(info3);
		assertArrayEquals(new String[]{"1", "2", "3", "4"}, info3.build().keywords().toArray());
	}

	@Test
	public void testRequiredPlugins() {
		var info = builder(true)
			.addRequiredPlugin("1")
			.addRequiredPlugins("2")
			.addRequiredPlugins(List.of("3"))
			.build();
		assertArrayEquals(new String[]{"1", "2", "3"}, info.requiredPlugins().toArray());

		var info2 = info.toBuilder()
			.clearRequiredPlugins()
			.addRequiredPlugin("4")
			.build();
		assertArrayEquals(new String[]{"4"}, info2.requiredPlugins().toArray());

		var info3 = info.toBuilder();
		info2.toBuilder().applyTo(info3);
		assertArrayEquals(new String[]{"1", "2", "3", "4"}, info3.build().requiredPlugins().toArray());
	}

	@Test
	public void testEvents() {
		var info = builder(true)
			.addEvent(Event.class)
			.addEvents(CollectionUtils.array(PlayerEvent.class))
			.addEvents(List.of(EntityEvent.class))
			.build();
		assertArrayEquals(new Class[]{Event.class, PlayerEvent.class, EntityEvent.class}, info.events().toArray());

		var info2 = info.toBuilder()
			.clearEvents()
			.addEvent(BlockEvent.class)
			.build();
		assertArrayEquals(new Class[]{BlockEvent.class}, info2.events().toArray());

		var info3 = info.toBuilder();
		info2.toBuilder().applyTo(info3);
		assertArrayEquals(new Class[]{Event.class, PlayerEvent.class, EntityEvent.class, BlockEvent.class}, info3.build().events().toArray());
	}

}
