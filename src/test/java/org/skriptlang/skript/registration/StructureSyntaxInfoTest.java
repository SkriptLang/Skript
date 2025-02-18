package org.skriptlang.skript.registration;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.structure.Structure;
import org.skriptlang.skript.registration.StructureSyntaxInfoTest.MockStructure;

import java.util.function.Supplier;

import static org.junit.Assert.*;

public class StructureSyntaxInfoTest extends BaseSyntaxInfoTests<MockStructure, SyntaxInfo.Structure.Builder<?, MockStructure>> {

	public static final class MockStructure extends Structure {

		@Override
		public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult, @Nullable EntryContainer entryContainer) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean load() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString(@Nullable Event event, boolean debug) {
			throw new UnsupportedOperationException();
		}

	}

	@Override
	public SyntaxInfo.Structure.Builder<?, MockStructure> builder(boolean addPattern) {
		var info = SyntaxInfo.Structure.builder(MockStructure.class);
		if (addPattern) {
			info.addPattern("default");
		}
		return info;
	}

	@Override
	public Class<MockStructure> type() {
		return MockStructure.class;
	}

	@Override
	public Supplier<MockStructure> supplier() {
		return MockStructure::new;
	}

	@Test
	public void testEntryValidator() {
		final EntryValidator validator = EntryValidator.builder().build();

		var info = builder(true)
				.entryValidator(validator)
				.build();
		assertEquals(validator, info.entryValidator());
		assertEquals(validator, info.toBuilder().build().entryValidator());

		var info2 = builder(true);
		info.toBuilder().applyTo(info2);
		assertEquals(validator, info2.build().entryValidator());
	}

	@Test
	public void testNodeType() {
		for (final var type : SyntaxInfo.Structure.NodeType.values()) {
			var info = builder(true)
					.nodeType(type)
					.build();
			assertEquals(type, info.nodeType());
			assertEquals(type, info.toBuilder().build().nodeType());

			var info2 = builder(true);
			info.toBuilder().applyTo(info2);
			assertEquals(type, info2.build().nodeType());
		}
	}

}
