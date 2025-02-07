package org.skriptlang.skript.registration;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.registration.SyntaxInfo.Builder;
import org.skriptlang.skript.registration.SyntaxInfoTest.MockSyntaxElement;

import java.util.function.Supplier;

public class SyntaxInfoTest extends BaseSyntaxInfoTests<MockSyntaxElement, Builder<?, MockSyntaxElement>> {

	public static final class MockSyntaxElement implements SyntaxElement {

		@Override
		public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
			throw new UnsupportedOperationException();
		}

		@Override
		public @NotNull String getSyntaxTypeName() {
			throw new UnsupportedOperationException();
		}

	}

	@Override
	public SyntaxInfo.Builder<?, MockSyntaxElement> builder(boolean addPattern) {
		var info = SyntaxInfo.builder(MockSyntaxElement.class);
		if (addPattern) { // sometimes required as infos must have at least one pattern
			info.addPattern("default");
		}
		return info;
	}

	@Override
	public Class<MockSyntaxElement> type() {
		return MockSyntaxElement.class;
	}

	@Override
	public Supplier<MockSyntaxElement> supplier() {
		return MockSyntaxElement::new;
	}

}
