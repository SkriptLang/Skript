package ch.njol.skript.lang.function;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.util.Contract;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class JavaFunction<T> extends Function<T> {

	private @NotNull String @Nullable [] returnedKeys;

	public JavaFunction(Signature<T> sign) {
		super(sign);
	}

	public JavaFunction(String name, Parameter<?>[] parameters, ClassInfo<T> returnType, boolean single) {
		this(name, parameters, returnType, single, null);
	}

	@ApiStatus.Internal
	JavaFunction(String script, String name, Parameter<?>[] parameters, ClassInfo<T> returnType, boolean single) {
		this(script, name, parameters, returnType, single, true, null);
	}

	public JavaFunction(String name, Parameter<?>[] parameters, ClassInfo<T> returnType, boolean single, @Nullable Contract contract) {
		this(null, name, parameters, returnType, single, false, contract);
	}

	@ApiStatus.Internal
	JavaFunction(String script, String name, Parameter<?>[] parameters, ClassInfo<T> returnType, boolean single, boolean local, @Nullable Contract contract) {
		this(new Signature<>(script, name, parameters, local, returnType, single, Thread.currentThread().getStackTrace()[3].getClassName(), contract));
	}

	@Override
	public abstract T @Nullable [] execute(FunctionEvent<?> event, Object[][] params);

	@Override
	public @NotNull String @Nullable [] returnedKeys() {
		return returnedKeys;
	}

	/**
	 * Sets the keys that will be returned by this function.
	 * <br>
	 * Note: The length of the keys array must match the number of return values.
	 *
	 * @param keys An array of keys to be returned by the function. Can be null.
	 * @throws IllegalStateException If the function is returns a single value.
	 */
	public void setReturnedKeys(@NotNull String @Nullable [] keys) {
		if (isSingle())
			throw new IllegalStateException("Cannot return keys for a single return function");
		assert this.returnedKeys == null;
		this.returnedKeys = keys;
	}

	private String @Nullable [] description = null;
	private String @Nullable [] examples = null;
	private String @Nullable [] keywords;
	private @Nullable String since = null;

	/**
	 * Only used for Skript's documentation.
	 *
	 * @return This JavaFunction object
	 */
	public JavaFunction<T> description(final String... description) {
		assert this.description == null;
		this.description = description;
		return this;
	}

	/**
	 * Only used for Skript's documentation.
	 *
	 * @return This JavaFunction object
	 */
	public JavaFunction<T> examples(final String... examples) {
		assert this.examples == null;
		this.examples = examples;
		return this;
	}

	/**
	 * Only used for Skript's documentation.
	 *
	 * @param keywords
	 * @return This JavaFunction object
	 */
	public JavaFunction<T> keywords(final String... keywords) {
		assert this.keywords == null;
		this.keywords = keywords;
		return this;
	}

	/**
	 * Only used for Skript's documentation.
	 *
	 * @return This JavaFunction object
	 */
	public JavaFunction<T> since(final String since) {
		assert this.since == null;
		this.since = since;
		return this;
	}

	public String @Nullable [] getDescription() {
		return description;
	}

	public String @Nullable [] getExamples() {
		return examples;
	}

	public String @Nullable [] getKeywords() {
		return keywords;
	}

	public @Nullable String getSince() {
		return since;
	}

	@Override
	public boolean resetReturnValue() {
		returnedKeys = null;
		return true;
	}

}
