package org.skriptlang.skript.common.function;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.Signature;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.registrations.Classes;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.common.function.Parameter.Modifier;
import org.skriptlang.skript.common.function.Parameter.Modifier.RangedModifier;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;

final class DefaultFunctionImpl<T> extends ch.njol.skript.lang.function.Function<T> implements DefaultFunction<T> {

	private final SkriptAddon source;
	private final SequencedMap<String, Parameter<?>> parameters;
	private final Function<FunctionArguments, T> execute;

	private final List<String> description;
	private final List<String> since;
	private final List<String> examples;
	private final List<String> keywords;
	private final List<String> requires;

	DefaultFunctionImpl(
			SkriptAddon source,
			String name,
			SequencedMap<String, Parameter<?>> parameters,
			Class<T> returnType, boolean single,
			@Nullable ch.njol.skript.util.Contract contract,
			Function<FunctionArguments, T> execute,
			String[] description, String[] since, String[] examples,
			String[] keywords, String[] requires
	) {
		super(new Signature<>(null, name, parameters.values().toArray(new Parameter[0]), returnType, single, contract));

		Preconditions.checkNotNull(source, "source cannot be null");
		Preconditions.checkNotNull(name, "name cannot be null");
		Preconditions.checkNotNull(parameters, "parameters cannot be null");
		Preconditions.checkNotNull(returnType, "return type cannot be null");
		Preconditions.checkNotNull(execute, "execute cannot be null");

		this.source = source;
		this.parameters = parameters;
		this.execute = execute;
		this.description = description != null ? List.of(description) : Collections.emptyList();
		this.since = since != null ? List.of(since) : Collections.emptyList();
		this.examples = examples != null ? List.of(examples) : Collections.emptyList();
		this.keywords = keywords != null ? List.of(keywords) : Collections.emptyList();
		this.requires = requires != null ? List.of(requires) : Collections.emptyList();
	}

	@Override
	public T @Nullable [] execute(FunctionEvent<?> event, Object[][] params) {
		Map<String, Object> args = new LinkedHashMap<>();

		int length = Math.min(parameters.size(), params.length);
		Parameter<?>[] arrayParams = parameters.values().toArray(new Parameter[0]);
		for (int i = 0; i < length; i++) {
			Object[] arg = params[i];
			Parameter<?> parameter = arrayParams[i];

			if (arg == null || arg.length == 0) {
				if (parameter.hasModifier(Modifier.OPTIONAL)) {
					continue;
				} else {
					return null;
				}
			}

			if (parameter.hasModifier(Modifier.RANGED)) {
				RangedModifier<?> range = parameter.getModifier(RangedModifier.class);
				if (!range.inRange(arg)) {
					return null;
				}
			}

			// check parameter plurality before arg length, since plural params accept arrays of length 1
			if (parameter.isSingle()) {
				if (arg.length != 1) {
					return null;
				}

				assert parameter.type().isAssignableFrom(arg[0].getClass())
						: "argument type %s does not match parameter type %s".formatted(parameter.type().getSimpleName(),
						arg[0].getClass().getSimpleName());

				args.put(parameter.name(), arg[0]);
			} else {
				assert parameter.type().isAssignableFrom(arg.getClass())
						: "argument type %s does not match parameter type %s".formatted(parameter.type().getSimpleName(),
						arg.getClass().getSimpleName());

				args.put(parameter.name(), arg);
			}
		}

		FunctionArgumentsImpl arguments = new FunctionArgumentsImpl(args);
		T result = execute.apply(arguments);

		if (result == null) {
			return null;
		} else if (result.getClass().isArray()) {
			//noinspection unchecked
			return (T[]) result;
		} else {
			//noinspection unchecked
			T[] array = (T[]) Array.newInstance(result.getClass(), 1);
			array[0] = result;
			return array;
		}
	}

	@Override
	public T execute(@NotNull FunctionEvent<?> event, @NotNull FunctionArguments arguments) {
		for (String name : arguments.names()) {
			Parameter<?> parameter = parameters.get(name);
			Object value = arguments.get(name);

			if (value == null && !parameter.hasModifier(Modifier.OPTIONAL)) {
				return null;
			}

			if (parameter.hasModifier(Modifier.RANGED)) {
				RangedModifier<?> range = parameter.getModifier(RangedModifier.class);
				if (!range.inRange(value)) {
					return null;
				}
			}
		}

		return execute.apply(arguments);
	}

	@Override
	public boolean resetReturnValue() {
		return true;
	}

	@Override
	public @NotNull String name() {
		return getName();
	}

	@Override
	public @Unmodifiable @NotNull List<String> description() {
		return description;
	}

	@Override
	public @Unmodifiable @NotNull List<String> since() {
		return since;
	}

	@Override
	public @Unmodifiable @NotNull List<String> examples() {
		return examples;
	}

	@Override
	public @Unmodifiable @NotNull List<String> keywords() {
		return keywords;
	}

	@Override
	public @Unmodifiable @NotNull List<String> requires() {
		return requires;
	}

	@Override
	public @NotNull SkriptAddon source() {
		return source;
	}

	@Override
	public org.skriptlang.skript.common.function.@NotNull Signature<T> signature() {
		return getSignature();
	}

	static class BuilderImpl<T> implements DefaultFunctionImpl.Builder<T> {

		private final SkriptAddon source;
		private final String name;
		private final Class<T> returnType;
		private final SequencedMap<String, Parameter<?>> parameters = new LinkedHashMap<>();

		private ch.njol.skript.util.Contract contract = null;

		private String[] description;
		private String[] since;
		private String[] examples;
		private String[] keywords;
		private String[] requires;

		BuilderImpl(@NotNull SkriptAddon source, @NotNull String name, @NotNull Class<T> returnType) {
			Preconditions.checkNotNull(source, "source cannot be null");
			Preconditions.checkNotNull(name, "name cannot be null");
			Preconditions.checkNotNull(returnType, "return type cannot be null");

			this.source = source;
			this.name = name;
			this.returnType = returnType;
		}

		@Override
		public Builder<T> contract(@NotNull ch.njol.skript.util.Contract contract) {
			Preconditions.checkNotNull(contract, "contract cannot be null");

			this.contract = contract;
			return this;
		}

		@Override
		public Builder<T> description(@NotNull String @NotNull ... description) {
			Preconditions.checkNotNull(description, "description cannot be null");
			checkNotNull(description, "description contents cannot be null");

			this.description = description;
			return this;
		}

		@Override
		public Builder<T> since(@NotNull String @NotNull ... since) {
			Preconditions.checkNotNull(since, "since cannot be null");
			checkNotNull(since, "since contents cannot be null");

			this.since = since;
			return this;
		}

		@Override
		public Builder<T> examples(@NotNull String @NotNull ... examples) {
			Preconditions.checkNotNull(examples, "examples cannot be null");
			checkNotNull(examples, "examples contents cannot be null");

			this.examples = examples;
			return this;
		}

		@Override
		public Builder<T> keywords(@NotNull String @NotNull ... keywords) {
			Preconditions.checkNotNull(keywords, "keywords cannot be null");
			checkNotNull(keywords, "keywords contents cannot be null");

			this.keywords = keywords;
			return this;
		}

		@Override
		public Builder<T> requires(@NotNull String @NotNull ... requires) {
			Preconditions.checkNotNull(keywords, "requires cannot be null");
			checkNotNull(keywords, "requires contents cannot be null");

			this.requires = requires;
			return this;
		}

		@Override
		public Builder<T> parameter(@NotNull String name, @NotNull Class<?> type, Modifier @NotNull ... modifiers) {
			Preconditions.checkNotNull(name, "name cannot be null");
			Preconditions.checkNotNull(type, "type cannot be null");

			parameters.put(name, new DefaultParameter<>(name, type, modifiers));
			return this;
		}

		@Override
		public DefaultFunction<T> build(@NotNull Function<FunctionArguments, T> execute) {
			Preconditions.checkNotNull(execute, "execute cannot be null");

			return new DefaultFunctionImpl<>(source, name, parameters,
					returnType, !returnType.isArray(), contract, execute,
					description, since, examples, keywords, requires);
		}

		/**
		 * Checks whether the elements in a {@link String} array are null.
		 *
		 * @param strings The strings.
		 */
		private static void checkNotNull(@NotNull String[] strings, @NotNull String message) {
			for (String string : strings) {
				Preconditions.checkNotNull(string, message);
			}
		}

	}

	/**
	 * A parameter for a {@link DefaultFunction}.
	 *
	 * @param name The name.
	 * @param type The type's class.
	 * @param modifiers The modifiers.
	 * @param <T> The type.
	 */
	record DefaultParameter<T>(String name, Class<T> type, Set<Modifier> modifiers)
			implements Parameter<T> {

		DefaultParameter(String name, Class<T> type, Modifier... modifiers) {
			this(name, type, Set.of(modifiers));
		}

		@Override
		public @NotNull String toString() {
			StringJoiner joiner = new StringJoiner(" ");

			joiner.add("%s:".formatted(name));

			if (hasModifier(Modifier.OPTIONAL)) {
				joiner.add("optional");
			}

			Noun exact = Classes.getSuperClassInfo(type).getName();
			if (type.isArray()) {
				joiner.add(exact.getPlural());
			} else {
				joiner.add(exact.getSingular());
			}

			if (hasModifier(Modifier.RANGED)) {
				RangedModifier<?> range = getModifier(RangedModifier.class);
				joiner.add("between")
						.add(range.getMin().toString())
						.add("and")
						.add(range.getMax().toString());
			}

			return joiner.toString();
		}
	}

}
