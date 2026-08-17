package org.skriptlang.skript.common.function;

import ch.njol.skript.localization.Noun;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Contract;
import ch.njol.util.StringUtils;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.ApiStatus.Experimental;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.common.function.Signature.Modifier.Returns;
import org.skriptlang.skript.util.Priority;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Represents a function signature.
 *
 * <h2>This interface should only be extended by {@link ch.njol.skript.lang.function.Signature}.</h2>
 * <p>It will contain methods when Signature has been properly reworked.</p>
 */
@NonExtendable
@Internal
@Experimental
public interface Signature<T> {

	/**
	 * @return The name of the function.
	 */
	@NotNull String name();

	/**
	 * @deprecated Use {@link #hasModifier(Class)} and {@link #getModifier(Class)}
	 * with {@link Returns} instead.
	 */
	@Deprecated(forRemoval = true, since = "INSERT VERSION")
	default @Nullable Class<T> returnType() {
		if (!hasModifier(Modifier.Returns.class))
			return null;

		//noinspection unchecked
		return (Class<T>) getModifier(Returns.class).type();
	}

	/**
	 * @return An unmodifiable view of all the parameters that this signature has.
	 */
	@NotNull Parameters parameters();

	/**
	 * @return The namespace of this signature.
	 */
	@Nullable String namespace();

	/**
	 * @return The contract of this signature.
	 */
	@Experimental
	Contract contract();

	/**
	 * Adds a reference to the clearing list.
	 *
	 * @param reference The reference.
	 */
	@Experimental
	void addCall(FunctionReference<?> reference);

	/**
	 * @return Whether this signature returns single values.
	 */
	default boolean isSingle() {
		if (!hasModifier(Modifier.Returns.class))
			return false;

		return !getModifier(Returns.class).type().isArray();
	}

	/**
	 * @return All modifiers belonging to this signature.
	 */
	@Unmodifiable
	@NotNull Set<Modifier> modifiers();

	/**
	 * Returns whether this signature has the specified modifier.
	 *
	 * @param modifier The modifier.
	 * @return True when {@link #modifiers()} contains the specified modifier, false if not.
	 */
	default boolean hasModifier(Class<? extends Modifier> modifier) {
		return modifiers().stream().anyMatch(modifier::isInstance);
	}

	/**
	 * Gets a modifier of the specified type if present.
	 *
	 * @param modifierClass The class of the modifier to retrieve
	 * @return The modifier instance, or null if not present
	 */
	default <M extends Modifier> M getModifier(Class<M> modifierClass) {
		return modifiers().stream()
				.filter(modifierClass::isInstance)
				.map(modifierClass::cast)
				.findAny()
				.orElseThrow(() -> new NoSuchElementException("No value present for modifier " + modifierClass.getSimpleName()));
	}

	/**
	 * @return A human-readable string representing this parameter.
	 */
	default @NotNull String toFormattedString() {
		StringJoiner joiner = new StringJoiner(" ");

		for (Modifier modifier : modifiers()) {
			if (!modifier.toStringPriority().isBefore(Modifier.FUNCTION_PRIORITY)) {
				continue;
			}
			String string = modifier.toFormattedString();
			if (string.isEmpty()) {
				continue;
			}
			joiner.add(string);
		}

		joiner.add(name());
		joiner.add("(%s)".formatted(StringUtils.join(parameters().all(), ", ")));

		for (Modifier modifier : modifiers()) {
			if (!modifier.toStringPriority().isAfter(Modifier.FUNCTION_PRIORITY)) {
				continue;
			}
			String string = modifier.toFormattedString();
			if (string.isEmpty()) {
				continue;
			}
			joiner.add(string);
		}

		return joiner.toString();
	}

	/**
	 * Represents a modifier that can be applied to a function signature.
	 */
	interface Modifier {

		/**
		 * The priority used for printing the type in a signature's string representation.
		 */
		Priority FUNCTION_PRIORITY = Priority.base();

		/**
		 * @return The modifier as a human-readable, formatted string.
		 */
		@NotNull String toFormattedString();

		/**
		 * The priority used when converting this modifier to a string representation.
		 *
		 * <p>
		 * Registering after {@link #FUNCTION_PRIORITY} will print after the function,
		 * e.g. {@code function x() local}.
		 * Registering before {@link #FUNCTION_PRIORITY} will print before the function,
		 * e.g. {@code local function x()}.
		 * </p>
		 *
		 * @return The priority used.
		 */
		@NotNull Priority toStringPriority();

		record Local(@NotNull String namespace) implements Modifier {

			public Local {
				Preconditions.checkNotNull(namespace, "namespace cannot be null");
			}

			private static final Priority PRIORITY = Priority.before(FUNCTION_PRIORITY);

			@Override
			public @NotNull String toFormattedString() {
				return "local";
			}

			@Override
			public @NotNull Priority toStringPriority() {
				return PRIORITY;
			}

		}

		record Returns<T>(@NotNull Class<T> type) implements Modifier {

			private static final Priority PRIORITY = Priority.after(FUNCTION_PRIORITY);

			public Returns {
				Preconditions.checkNotNull(type, "type cannot be null");
			}

			@Override
			public @NotNull String toFormattedString() {
				Noun exact = Classes.getSuperClassInfo(type).getName();
				if (type.isArray()) {
					return "returns " + exact.getPlural();
				} else {
					return "returns " + exact.getSingular();
				}
			}

			@Override
			public @NotNull Priority toStringPriority() {
				return PRIORITY;
			}

		}

	}

}
