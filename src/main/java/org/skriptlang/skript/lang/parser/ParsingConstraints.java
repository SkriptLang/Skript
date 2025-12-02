package org.skriptlang.skript.lang.parser;

import ch.njol.skript.lang.ExpressionInfo;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SyntaxElement;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.coll.iterator.CheckedIterator;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.registration.SyntaxInfo;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static ch.njol.skript.lang.SkriptParser.PARSE_EXPRESSIONS;
import static ch.njol.skript.lang.SkriptParser.PARSE_LITERALS;

public class ParsingConstraints {

	private enum ExceptionMode {
		UNUSED,
		EXCLUDE,
		INCLUDE
	}

	private Set<Class<?>> exceptions = Set.of();
	private ExceptionMode exceptionMode;

	private boolean allowFunctionCalls;

	private boolean allowNonLiterals;
	private boolean allowLiterals;

	private Class<?> @Nullable [] validReturnTypes;

	@Contract("-> new")
	public static @NotNull ParsingConstraints empty() {
		return new ParsingConstraints()
			.allowFunctionCalls(false)
			.include()
			.allowLiterals(false)
			.allowNonLiterals(false);
	}

	@Contract(" -> new")
	public static @NotNull ParsingConstraints all() {
		return new ParsingConstraints();
	}

	private ParsingConstraints() {
		exceptionMode = ExceptionMode.UNUSED;
		allowFunctionCalls = true;
		allowNonLiterals = true;
		allowLiterals = true;
		validReturnTypes = new Class[]{Object.class};
	}

	public <T extends SyntaxElement> @NotNull Iterator<? extends SyntaxInfo<? extends T>> constrainIterator(Iterator<? extends SyntaxInfo<? extends T>> uncheckedIterator) {
		return new CheckedIterator<>(uncheckedIterator, info -> {
			assert info != null;
			Class<?> elementClass = info.type();
			if (elementClass == null) {
				return false;
			}

			if (info instanceof ExpressionInfo<?, ?>) {
				// check literals
				if (!allowsLiterals() && Literal.class.isAssignableFrom(elementClass)) {
					return false;
				}
				// check non-literals
				// TODO: allow simplification
				if (!allowsNonLiterals() && !Literal.class.isAssignableFrom(elementClass)) {
					return false;
				}
			}

			// check exceptions
			if (exceptionMode == ExceptionMode.INCLUDE && !exceptions.contains(elementClass)) {
				return false;
			} else if (exceptionMode == ExceptionMode.EXCLUDE && exceptions.contains(elementClass)) {
				return false;
			}

			// check return types
			if (info instanceof ExpressionInfo<?, ?> expressionInfo) {
				if (validReturnTypes == null || expressionInfo.returnType == Object.class)
					return true;

				for (Class<?> returnType : validReturnTypes) {
					if (Converters.converterExists(expressionInfo.returnType, returnType))
						return true;
				}
				return false;
			}
			return true;
		});
	}

	public ParsingConstraints include(Class<?>... exceptions) {
		if (exceptionMode != ExceptionMode.INCLUDE) {
			this.exceptions = new HashSet<>();
		}
		this.exceptions.addAll(Set.of(exceptions));
		exceptionMode = ExceptionMode.INCLUDE;
		return this;
	}

	public ParsingConstraints exclude(Class<?>... exceptions) {
		if (exceptionMode != ExceptionMode.EXCLUDE) {
			this.exceptions = new HashSet<>();
		}
		this.exceptions.addAll(Set.of(exceptions));
		exceptionMode = ExceptionMode.EXCLUDE;
		return this;
	}

	public ParsingConstraints clearExceptions() {
		exceptions = Set.of();
		exceptionMode = ExceptionMode.UNUSED;
		return this;
	}

	public boolean allowsFunctionCalls() { return allowFunctionCalls; }

	public ParsingConstraints allowFunctionCalls(boolean allow) {
		allowFunctionCalls = allow;
		return this;
	}

	public Class<?>[] getValidReturnTypes() {
		return validReturnTypes;
	}

	public ParsingConstraints constrainReturnTypes(Class<?>... validReturnTypes) {
		if (validReturnTypes == null || validReturnTypes.length == 0) {
			this.validReturnTypes = null;
		} else {
			this.validReturnTypes = validReturnTypes;
		}
		return this;
	}

	public boolean allowsNonLiterals() { return allowNonLiterals; }

	public ParsingConstraints allowNonLiterals(boolean allow) {
		allowNonLiterals = allow;
		return this;
	}

	public boolean allowsLiterals() { return allowLiterals; }

	public ParsingConstraints allowLiterals(boolean allow) {
		allowLiterals = allow;
		return this;
	}

	@ApiStatus.Internal
	public int asParseFlags() {
		int flags = 0;
		if (allowNonLiterals) {
			flags |= PARSE_EXPRESSIONS;
		}
		if (allowLiterals) {
			flags |= PARSE_LITERALS;
		}
		return flags;
	}

	@ApiStatus.Internal
	public ParsingConstraints applyParseFlags(int flags) {
		allowNonLiterals = (flags & PARSE_EXPRESSIONS) != 0;
		allowLiterals = (flags & PARSE_LITERALS) != 0;
		return this;
	}

	@ApiStatus.Internal
	public ParsingConstraints andParseFlags(int flags) {
		applyParseFlags(asParseFlags() & flags);
		return this;
	}

	public ParsingConstraints copy() {
		ParsingConstraints copy = new ParsingConstraints();
		copy.exceptions = new HashSet<>(exceptions);
		copy.exceptionMode = exceptionMode;
		copy.validReturnTypes = validReturnTypes;
		copy.allowFunctionCalls = allowFunctionCalls;
		copy.allowNonLiterals = allowNonLiterals;
		copy.allowLiterals = allowLiterals;
		return copy;
	}

	static {
		ParserInstance.registerData(ConstraintData.class, ConstraintData::new);
	}

	public static class ConstraintData extends ParserInstance.Data {
		private ParsingConstraints parsingConstraints = ParsingConstraints.all();

		public ConstraintData(ParserInstance parserInstance) {
			super(parserInstance);
		}

		public ParsingConstraints getConstraints() {
			return parsingConstraints;
		}

		public void setConstraints(ParsingConstraints parsingConstraints) {
			this.parsingConstraints = parsingConstraints;
		}

	}

}
