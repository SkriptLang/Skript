package org.skriptlang.skript.registration;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.skriptlang.skript.registration.ExpressionSyntaxInfoTest.MockExpression;

import java.util.Iterator;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.junit.Assert.*;

public class ExpressionSyntaxInfoTest extends BaseSyntaxInfoTests<MockExpression, SyntaxInfo.Expression.Builder<?, MockExpression, String>> {

	public static final class MockExpression implements Expression<String> {

		@Override
		public String getSingle(Event event) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String[] getArray(Event event) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String[] getAll(Event event) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isSingle() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean check(Event event, Predicate<? super String> checker, boolean negated) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean check(Event event, Predicate<? super String> checker) {
			throw new UnsupportedOperationException();
		}

		@Override
		@SafeVarargs
		public final <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Class<? extends String> getReturnType() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean getAnd() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean setTime(int time) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int getTime() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isDefault() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Expression<?> getSource() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Expression<? extends String> simplify() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Class<?>[] acceptChange(ChangeMode mode) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString(@Nullable Event event, boolean debug) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Iterator<? extends String> iterator(Event event) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isLoopOf(String input) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
			throw new UnsupportedOperationException();
		}

	}

	@Override
	public SyntaxInfo.Expression.Builder<?, MockExpression, String> builder(boolean addPattern) {
		var info = SyntaxInfo.Expression.builder(MockExpression.class, String.class);
		if (addPattern) {
			info.addPattern("default");
		}
		return info;
	}

	@Override
	public Class<MockExpression> type() {
		return MockExpression.class;
	}

	@Override
	public Supplier<MockExpression> supplier() {
		return MockExpression::new;
	}

	@Test
	public void testReturnType() {
		var info = builder(true)
				.build();
		assertEquals(String.class, info.returnType());
		assertEquals(String.class, info.toBuilder().build().returnType());
	}

}
