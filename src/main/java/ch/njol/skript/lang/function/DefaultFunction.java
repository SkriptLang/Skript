package ch.njol.skript.lang.function;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Contract;
import com.google.common.base.Preconditions;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A function that has been implemented in Java, instead of in Skript.
 * <p>
 * An example implementation is stated below.
 * <pre><code>
 * Functions.register(DefaultFunction.builder("floor", Long.class)
 * 	.description("Rounds a number down.")
 * 	.examples("floor(2.34) = 2")
 * 	.since("3.0")
 * 	.parameter("n", Number.class)
 * 	.build(args -> {
 * 		Object value = args.get("n");
 *
 * 		if (value instanceof Long l)
 * 			return l;
 *
 * 		return Math2.floor(((Number) value).doubleValue());
 *    }));
 * </code></pre>
 * </p>
 *
 * @param <T> The return type.
 * @see #builder(String, Class)
 */
public final class DefaultFunction<T> extends ch.njol.skript.lang.function.Function<T> {

    /**
     * Creates a new builder for a function.
     *
     * @param name       The name of the function.
     * @param returnType The type of the function.
     * @param <T>        The return type.
     * @return The builder for a function.
     */
    public static <T> Builder<T> builder(@NotNull String name, @NotNull Class<T> returnType) {
        return new Builder<>(name, returnType);
    }

    private final Parameter<?>[] parameters;
    private final Function<FunctionArguments, T> execute;
    private final BiFunction<Event, FunctionArguments, T> execute2;

    private final String[] description;
    private final String[] since;
    private final String[] examples;
    private final String[] keywords;

    private DefaultFunction(
            String name, Parameter<?>[] parameters,
            ClassInfo<T> returnType, boolean single,
            @Nullable Contract contract, Function<FunctionArguments, T> execute,
            String[] description, String[] since, String[] examples, String[] keywords
    ) {
        super(new Signature<>("none", name, parameters, false,
                returnType, single, Thread.currentThread().getStackTrace()[3].getClassName(), contract));

        Preconditions.checkNotNull(name, "name cannot be null");
        Preconditions.checkNotNull(parameters, "parameters cannot be null");
        Preconditions.checkNotNull(returnType, "return type cannot be null");
        Preconditions.checkNotNull(execute, "execute cannot be null");

        this.parameters = parameters;
        this.execute = execute;
        this.execute2 = null;
        this.description = description;
        this.since = since;
        this.examples = examples;
        this.keywords = keywords;
    }

    private DefaultFunction(
            String name, Parameter<?>[] parameters,
            ClassInfo<T> returnType, boolean single,
            @Nullable Contract contract, BiFunction<Event, FunctionArguments, T> execute2,
            String[] description, String[] since, String[] examples, String[] keywords
    ) {
        super(new Signature<>("none", name, parameters, false,
                returnType, single, Thread.currentThread().getStackTrace()[3].getClassName(), contract));

        Preconditions.checkNotNull(name, "name cannot be null");
        Preconditions.checkNotNull(parameters, "parameters cannot be null");
        Preconditions.checkNotNull(returnType, "return type cannot be null");
        Preconditions.checkNotNull(execute2, "execute2 cannot be null");

        this.parameters = parameters;
        this.execute = null;
        this.execute2 = execute2;
        this.description = description;
        this.since = since;
        this.examples = examples;
        this.keywords = keywords;
    }

    @Override
    public T @Nullable [] execute(FunctionEvent<?> event, Object[][] params) {
        LinkedHashMap<String, Object> args = new LinkedHashMap<>();

        for (int i = 0; i < parameters.length; i++) {
            if (i >= params.length) {
                continue;
            }

            Object[] arg = params[i];
            Parameter<?> parameter = parameters[i];

            if (arg == null || arg.length == 0) {
                if (parameter.isOptional()) {
                    continue;
                } else {
                    return null;
                }
            }

            if (arg.length == 1 || parameter.isSingleValue()) {
                assert parameter.getType().getC().isAssignableFrom(arg[0].getClass()) : "argument type does not match parameter";

                args.put(parameter.getName(), arg[0]);
            } else {
                assert parameter.getType().getC().isAssignableFrom(arg.getClass()) : "argument type does not match parameter";

                args.put(parameter.getName(), arg);
            }
        }

        FunctionArguments arguments = new FunctionArguments(args);
        T result;
        if (execute == null) {
            result = execute2.apply(event, arguments);
        } else {
            result = execute.apply(arguments);
        }

        if (result == null) {
            return null;
        } else if (isArray(result)) {
            //noinspection unchecked
            return (T[]) result;
        } else {
            //noinspection unchecked
            T[] array = (T[]) Array.newInstance(result.getClass(), 1);
            array[0] = result;
            return array;
        }
    }

    private boolean isArray(Object obj) {
        return obj instanceof Object[] || obj instanceof boolean[] ||
                obj instanceof byte[] || obj instanceof short[] ||
                obj instanceof char[] || obj instanceof int[] ||
                obj instanceof long[] || obj instanceof float[] ||
                obj instanceof double[];
    }

    @Override
    public boolean resetReturnValue() {
        return true;
    }

    /**
     * Returns this function's description.
     *
     * @return The description.
     */
    public String @NotNull [] description() {
        return description;
    }

    /**
     * Returns this function's version history.
     *
     * @return The version history.
     */
    public String @NotNull [] since() {
        return since;
    }

    /**
     * Returns this function's examples.
     *
     * @return The examples.
     */
    public String @NotNull [] examples() {
        return examples;
    }

    /**
     * Returns this function's keywords.
     *
     * @return The keywords.
     */
    public String[] keywords() {
        return keywords;
    }

    public static class Builder<T> {

        private final String name;
        private final Class<T> returnType;
        private final Map<String, Parameter<?>> parameters = new LinkedHashMap<>();

        private Contract contract = null;

        private String[] description, since, examples, keywords;

        private Builder(@NotNull String name, @NotNull Class<T> returnType) {
            Preconditions.checkNotNull(name, "name cannot be null");
            Preconditions.checkNotNull(returnType, "return type cannot be null");

            this.name = name;
            this.returnType = returnType;
        }

        public Builder<T> contract(@NotNull Contract contract) {
            Preconditions.checkNotNull(contract, "contract cannot be null");

            this.contract = contract;
            return this;
        }

        /**
         * Sets this function builder's description.
         *
         * @return This builder.
         */
        public Builder<T> description(@NotNull String... description) {
            Preconditions.checkNotNull(description, "description cannot be null");

            this.description = description;
            return this;
        }

        /**
         * Sets this function builder's version history.
         *
         * @return This builder.
         */
        public Builder<T> since(@NotNull String... since) {
            Preconditions.checkNotNull(since, "since cannot be null");

            this.since = since;
            return this;
        }

        /**
         * Sets this function builder's examples.
         *
         * @return This builder.
         */
        public Builder<T> examples(@NotNull String... examples) {
            Preconditions.checkNotNull(examples, "examples cannot be null");

            this.examples = examples;
            return this;
        }

        /**
         * Sets this function builder's keywords.
         *
         * @return This builder.
         */
        public Builder<T> keywords(@NotNull String... keywords) {
            Preconditions.checkNotNull(keywords, "keywords cannot be null");

            this.keywords = keywords;
            return this;
        }

        /**
         * Adds a parameter to this function builder.
         *
         * @param name The parameter name.
         * @param type The type of the parameter.
         * @return This builder.
         */
        public <PT> Builder<T> parameter(@NotNull String name, @NotNull Class<PT> type) {
            Preconditions.checkNotNull(name, "name cannot be null");
            Preconditions.checkNotNull(type, "type cannot be null");

            parameters.put(name, new Parameter<>(name, getClassInfo(type), !type.isArray(), null));
            return this;
        }

        /**
         * Adds an optional parameter to this function builder.
         *
         * @param name The parameter name.
         * @param type The type of the parameter.
         * @return This builder.
         */
        public <PT> Builder<T> optionalParameter(@NotNull String name, @NotNull Class<PT> type) {
            Preconditions.checkNotNull(name, "name cannot be null");
            Preconditions.checkNotNull(type, "type cannot be null");

            parameters.put(name, new Parameter<>(name, getClassInfo(type), !type.isArray(), true));
            return this;
        }

        /**
         * Completes this builder with the code to execute on call of this function.
         *
         * @param execute The code to execute.
         * @return The final function.
         */
        public DefaultFunction<T> build(Function<FunctionArguments, T> execute) {
            Preconditions.checkNotNull(execute, "execute cannot be null");

            return new DefaultFunction<>(name, parameters.values().toArray(new Parameter[0]), getClassInfo(returnType),
                    !returnType.isArray(), contract, execute, description, since, examples, keywords);
        }

        /**
         * Completes this builder with the code to execute on call of this function.
         *
         * @param execute The code to execute.
         * @return The final function.
         */
        public DefaultFunction<T> build(BiFunction<Event, FunctionArguments, T> execute) {
            Preconditions.checkNotNull(execute, "execute cannot be null");

            return new DefaultFunction<>(name, parameters.values().toArray(new Parameter[0]), getClassInfo(returnType),
                    !returnType.isArray(), contract, execute, description, since, examples, keywords);
        }
    }

    /**
     * Returns the {@link ClassInfo} of the non-array type of {@code cls}.
     *
     * @param cls The class.
     * @param <X> The type of class.
     * @return The non-array {@link ClassInfo} of {@code cls}.
     */
    static <X> ClassInfo<X> getClassInfo(Class<X> cls) {
        ClassInfo<X> classInfo;
        if (cls.isArray()) {
            //noinspection unchecked
            classInfo = (ClassInfo<X>) Classes.getExactClassInfo(cls.componentType());
        } else {
            classInfo = Classes.getExactClassInfo(cls);
        }
        if (classInfo == null) {
            throw new IllegalArgumentException("No type found for " + cls.getSimpleName());
        }
        return classInfo;
    }

}