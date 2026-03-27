package org.skriptlang.skript.lang.parsing.sites;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ExprInfo;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ClassInfoReference;
import ch.njol.util.Kleenean;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.skriptlang.skript.lang.parsing.constraints.Constraints;
import org.skriptlang.skript.lang.parsing.constraints.LiteralConstraint;
import org.skriptlang.skript.lang.parsing.constraints.ReturnTypeConstraint;
import org.skriptlang.skript.lang.parsing.constraints.ReturnTypes;

import java.util.List;
import java.util.Set;

/**
 * An immutable parsing site for a single expression slot. Specifies the accepted return types,
 * literal/non-literal constraints, and time state of the expression to be parsed.
 * <br>
 * Use {@link #builder()} to construct instances, or the convenience factories
 * {@link #of(Class[])} and {@link #of(ExprInfo)}.
 * @see Builder
 */
@ApiStatus.Experimental
public final class ExpressionSite implements ParsingSite {

	private final ReturnTypes returnTypes;
	private final boolean optional;
	private final boolean allowLiterals;
	private final boolean allowNonLiterals;
	private final boolean allowSimplifiedLiterals;
	private final TimeState timeState;
	private final Constraints constraints;

	private ExpressionSite(Builder builder) {
		this.returnTypes = builder.returnTypes;
		this.optional = builder.optional;
		this.allowLiterals = builder.allowLiterals;
		this.allowNonLiterals = builder.allowNonLiterals;
		this.allowSimplifiedLiterals = builder.allowSimplifiedLiterals;
		this.timeState = builder.timeState;
		this.constraints = Constraints.of(
			new ReturnTypeConstraint(returnTypes),
			new LiteralConstraint(allowLiterals, allowNonLiterals, allowSimplifiedLiterals));
	}

	// -- Factory methods --

	/**
	 * Returns a new {@link Builder} with default settings.
	 */
	@Contract("-> new")
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Returns a new {@link Builder} with settings copied from the given {@link ExprInfo}.
	 * @param info The {@link ExprInfo} to copy settings from.
	 */
	@Contract("_ -> new")
	public static Builder builderFrom(ExprInfo info) {
		Builder builder = new Builder();
		builder.returnTypes(info.classes, info.isPlural);
		builder.allowLiterals = (info.flagMask & SkriptParser.PARSE_LITERALS) != 0;
		builder.allowNonLiterals = (info.flagMask & SkriptParser.PARSE_EXPRESSIONS) != 0;
		builder.optional = info.isOptional;
		builder.timeState = TimeState.fromInt(info.time);
		return builder;
	}

	/**
	 * Creates a new expression site with default settings and the given return types.
	 * @param returnTypes The accepted return types.
	 */
	@Contract("_ -> new")
	public static ExpressionSite of(Class<?>... returnTypes) {
		return builder().returnTypes(returnTypes).build();
	}

	/**
	 * Creates a new expression site by copying the return types, flags, optional state, and time state
	 * from an existing {@link ExprInfo}.
	 * @param info The {@link ExprInfo} to copy settings from.
	 */
	@Contract("_ -> new")
	public static ExpressionSite of(ExprInfo info) {
		return builderFrom(info).build();
	}

	// -- ParsingSite --

	@Override
	public Constraints constraints() {
		return constraints;
	}

	@Override
	public boolean isOptional() {
		return optional;
	}

	// -- Getters --

	/**
	 * @return An unmodifiable view of the accepted return types.
	 */
	public @UnmodifiableView Set<ClassInfoReference> getReturnTypes() {
		return returnTypes.asSet();
	}

	/**
	 * @return Whether literal expressions are accepted at this site.
	 */
	public boolean allowsLiterals() {
		return allowLiterals;
	}

	/**
	 * @return Whether non-literal expressions are accepted at this site.
	 */
	public boolean allowsNonLiterals() {
		return allowNonLiterals;
	}

	/**
	 * @return Whether simplified literal expressions are accepted at this site.
	 */
	public boolean allowsSimplifiedLiterals() {
		return allowSimplifiedLiterals;
	}

	/**
	 * @return The required time state of the expression at this site.
	 */
	public TimeState timeState() {
		return timeState;
	}

	/**
	 * @return A builder representing this site.
	 */
	@Contract("-> new")
	public Builder toBuilder() {
		Builder builder = new Builder();
		applyTo(builder);
		return builder;
	}

	/**
	 * Applies the values of this site onto the given builder.
	 * @param builder The builder to apply values onto.
	 */
	public void applyTo(Builder builder) {
		builder.returnTypes(returnTypes.asSet().toArray(ClassInfoReference[]::new));
		builder.optional = optional;
		builder.allowLiterals = allowLiterals;
		builder.allowNonLiterals = allowNonLiterals;
		builder.allowSimplifiedLiterals = allowSimplifiedLiterals;
		builder.timeState = timeState;
	}

	/**
	 * Converts this site to an {@link ExprInfo} for compatibility with the legacy parser.
	 * @return A new {@link ExprInfo} representing the settings of this site.
	 */
	@Contract("-> new")
	public ExprInfo toExprInfo() {
		ClassInfo<?>[] classes = new ClassInfo<?>[returnTypes.size()];
		boolean[] isPlural = new boolean[returnTypes.size()];
		int i = 0;
		for (ClassInfoReference ref : returnTypes) {
			classes[i] = ref.getClassInfo();
			isPlural[i] = ref.isPlural().isTrue();
			i++;
		}
		ExprInfo info = new ExprInfo(classes, isPlural);
		info.isOptional = optional;
		if (allowLiterals) {
			info.flagMask |= SkriptParser.PARSE_LITERALS;
		}
		if (allowNonLiterals) {
			info.flagMask |= SkriptParser.PARSE_EXPRESSIONS;
		}
		info.time = timeState.getValue();
		return info;
	}

	// -- Builder --

	/**
	 * Builder for {@link ExpressionSite}. All fields default to the same values as a freshly
	 * constructed site: accepts all literals and non-literals, no return type constraints,
	 * not optional, and present time state.
	 * @see ExpressionSite#builder()
	 */
	@ApiStatus.Experimental
	public static final class Builder {

		private ReturnTypes returnTypes = new ReturnTypes();
		private boolean optional = false;
		private boolean allowLiterals = true;
		private boolean allowNonLiterals = true;
		private boolean allowSimplifiedLiterals = true;
		private TimeState timeState = TimeState.PRESENT;

		private Builder() {}

		/**
		 * Sets the accepted return types from a list of {@link ClassInfoReference}s.
		 * @param returnTypes The accepted return types.
		 * @return This builder.
		 * @see ExpressionSite#getReturnTypes()
		 */
		@Contract("_ -> this")
		public Builder returnTypes(List<ClassInfoReference> returnTypes) {
			this.returnTypes = new ReturnTypes();
			this.returnTypes.add(returnTypes.toArray(ClassInfoReference[]::new));
			return this;
		}

		/**
		 * Sets the accepted return types from an array of {@link ClassInfoReference}s.
		 * @param returnTypes The accepted return types.
		 * @return This builder.
		 * @see ExpressionSite#getReturnTypes()
		 */
		@Contract("_ -> this")
		public Builder returnTypes(ClassInfoReference... returnTypes) {
			this.returnTypes = new ReturnTypes();
			this.returnTypes.add(returnTypes);
			return this;
		}

		/**
		 * Sets the accepted return types from parallel arrays of {@link ClassInfo}s and plurality flags.
		 * @param returnType The accepted return types.
		 * @param isPlural Whether each corresponding return type accepts plural expressions.
		 * @return This builder.
		 * @see ExpressionSite#getReturnTypes()
		 */
		@Contract("_, _ -> this")
		public Builder returnTypes(@NotNull ClassInfo<?>[] returnType, boolean[] isPlural) {
			Preconditions.checkNotNull(returnType, "returnType is null");
			Preconditions.checkNotNull(isPlural, "isPlural is null");
			Preconditions.checkArgument(returnType.length == isPlural.length, "returnType and isPlural must have the same length");
			Preconditions.checkArgument(returnType.length > 0, "returnType must have at least one element");
			this.returnTypes = new ReturnTypes();
			for (int i = 0; i < returnType.length; i++) {
				Preconditions.checkNotNull(returnType[i], "returnType[%s] is null", i);
				this.returnTypes.add(new ClassInfoReference(returnType[i], Kleenean.get(isPlural[i])));
			}
			return this;
		}

		/**
		 * Sets the accepted return types from {@link ClassInfo}s with unknown plurality.
		 * @param returnTypes The accepted return types.
		 * @return This builder.
		 * @see ExpressionSite#getReturnTypes()
		 */
		@Contract("_ -> this")
		public Builder returnTypes(ClassInfo<?>... returnTypes) {
			Preconditions.checkNotNull(returnTypes, "returnTypes is null");
			Preconditions.checkArgument(returnTypes.length > 0, "returnTypes must have at least one element");
			this.returnTypes = new ReturnTypes();
			for (int i = 0; i < returnTypes.length; i++) {
				Preconditions.checkNotNull(returnTypes[i], "returnTypes[%s] is null", i);
				this.returnTypes.add(new ClassInfoReference(returnTypes[i], Kleenean.UNKNOWN));
			}
			return this;
		}

		/**
		 * Sets the accepted return types from raw {@link Class}es, resolved via
		 * {@link Classes#getSuperClassInfo(Class)}, with unknown plurality.
		 * @param returnTypes The accepted return types.
		 * @return This builder.
		 * @see ExpressionSite#getReturnTypes()
		 */
		@Contract("_ -> this")
		public Builder returnTypes(Class<?>... returnTypes) {
			Preconditions.checkNotNull(returnTypes, "returnTypes is null");
			Preconditions.checkArgument(returnTypes.length > 0, "returnTypes must have at least one element");
			this.returnTypes = new ReturnTypes();
			for (int i = 0; i < returnTypes.length; i++) {
				Preconditions.checkNotNull(returnTypes[i], "returnTypes[%s] is null", i);
				ClassInfo<?> info = Classes.getSuperClassInfo(returnTypes[i]);
				this.returnTypes.add(new ClassInfoReference(info, Kleenean.UNKNOWN));
			}
			return this;
		}

		/**
		 * Sets whether this site is optional.
		 * @param optional Whether this site is optional.
		 * @return This builder.
		 * @see ExpressionSite#isOptional()
		 */
		@Contract("_ -> this")
		public Builder optional(boolean optional) {
			this.optional = optional;
			return this;
		}

		/**
		 * Sets whether literal expressions are accepted at this site.
		 * @param allowLiterals Whether literal expressions are accepted.
		 * @return This builder.
		 * @see ExpressionSite#allowsLiterals()
		 */
		@Contract("_ -> this")
		public Builder allowLiterals(boolean allowLiterals) {
			this.allowLiterals = allowLiterals;
			return this;
		}

		/**
		 * Sets whether non-literal expressions are accepted at this site.
		 * @param allowNonLiterals Whether non-literal expressions are accepted.
		 * @return This builder.
		 * @see ExpressionSite#allowsNonLiterals()
		 */
		@Contract("_ -> this")
		public Builder allowNonLiterals(boolean allowNonLiterals) {
			this.allowNonLiterals = allowNonLiterals;
			return this;
		}

		/**
		 * Sets whether simplified literal expressions are accepted at this site.
		 * @param allowSimplifiedLiterals Whether simplified literal expressions are accepted.
		 * @return This builder.
		 * @see ExpressionSite#allowsSimplifiedLiterals()
		 */
		@Contract("_ -> this")
		public Builder allowSimplifiedLiterals(boolean allowSimplifiedLiterals) {
			this.allowSimplifiedLiterals = allowSimplifiedLiterals;
			return this;
		}

		/**
		 * Sets the required time state of the expression using an integer value.
		 * @param timeState The time state value ({@code -1} for past, {@code 0} for present, {@code 1} for future).
		 * @return This builder.
		 * @throws IllegalArgumentException If the value does not correspond to a valid {@link TimeState}.
		 * @see ExpressionSite#timeState()
		 */
		@Contract("_ -> this")
		public Builder timeState(int timeState) {
			this.timeState = TimeState.fromInt(timeState);
			return this;
		}

		/**
		 * Sets the required time state of the expression.
		 * @param timeState The time state.
		 * @return This builder.
		 * @see ExpressionSite#timeState()
		 */
		@Contract("_ -> this")
		public Builder timeState(TimeState timeState) {
			this.timeState = timeState;
			return this;
		}

		/**
		 * Applies the values of this builder onto {@code builder}.
		 * @param builder The builder to apply values onto.
		 */
		public void applyTo(Builder builder) {
			builder.returnTypes(returnTypes.asSet().toArray(ClassInfoReference[]::new));
			builder.optional = optional;
			builder.allowLiterals = allowLiterals;
			builder.allowNonLiterals = allowNonLiterals;
			builder.allowSimplifiedLiterals = allowSimplifiedLiterals;
			builder.timeState = timeState;
		}

		/**
		 * Builds and returns the configured {@link ExpressionSite}.
		 * @return A new immutable {@link ExpressionSite}.
		 */
		@Contract("-> new")
		public ExpressionSite build() {
			return new ExpressionSite(this);
		}

	}

	// -- TimeState --

	/**
	 * Represents the required time state of an expression at a parsing site.
	 */
	public enum TimeState {
		/** The expression must refer to a past state. */
		PAST(-1),
		/** The expression refers to the present state. */
		PRESENT(0),
		/** The expression must refer to a future state. */
		FUTURE(1);

		private final int value;

		TimeState(int value) {
			this.value = value;
		}

		/**
		 * @return The integer value of this time state ({@code -1} for past, {@code 0} for present, {@code 1} for future).
		 */
		public int getValue() {
			return value;
		}

		/**
		 * Returns the {@link TimeState} corresponding to the given integer value.
		 * @param value The integer value to look up.
		 * @return The matching {@link TimeState}.
		 * @throws IllegalArgumentException If no {@link TimeState} corresponds to the given value.
		 */
		public static TimeState fromInt(int value) {
			for (TimeState state : values()) {
				if (state.getValue() == value) {
					return state;
				}
			}
			throw new IllegalArgumentException("Invalid TimeState value: " + value);
		}
	}

}
