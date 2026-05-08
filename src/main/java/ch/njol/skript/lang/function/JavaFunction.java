package ch.njol.skript.lang.function;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.util.Contract;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.common.function.DefaultFunction;
import org.skriptlang.skript.common.function.FunctionArguments;
import org.skriptlang.skript.common.function.Parameters;
import org.skriptlang.skript.docs.Documentation;
import org.skriptlang.skript.docs.DocumentationAdapter;
import org.skriptlang.skript.docs.DocumentationDocumentable;
import org.skriptlang.skript.docs.Origin;

/**
 * @deprecated Use {@link DefaultFunction} instead.
 */
@Deprecated(since = "2.13", forRemoval = true)
public abstract class JavaFunction<T> extends Function<T>
	implements DocumentationDocumentable, ch.njol.skript.doc.Documentable {

	private @NotNull String @Nullable [] returnedKeys;
	private Documentation documentation;

	public JavaFunction(Signature<T> sign) {
		super(sign);

		// questionably obtain source...
		SkriptAddon source;
		try {
			Class<?> caller = Class.forName(Thread.currentThread().getStackTrace()[2].getClassName());
			JavaPlugin callerPlugin = JavaPlugin.getProvidingPlugin(caller);
			source = Skript.instance().addons().stream()
				.filter(addon -> JavaPlugin.getProvidingPlugin(addon.source()) == callerPlugin)
				.findFirst()
				.orElse(Skript.instance());
		} catch (ClassNotFoundException ignored) {
			source = Skript.instance();
		}
		documentation = Documentation.builder()
			.name(sign.getName())
			.origin(Origin.of(source))
			.build();
		documentation = documentation.toBuilder()
			.id("Func" + documentation.autoId())
			.build();
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
	public final T execute(@NotNull FunctionEvent<?> event, @NotNull FunctionArguments arguments) {
		Parameters parameters = getSignature().parameters();

		// old params
		Object[][] params = new Object[parameters.size()][];
		for (int i = 0; i < parameters.size(); i++) {
			Parameter<?> parameter = (Parameter<?>) parameters.get(i);
			Object object = arguments.get(parameter.name());

			if (object != null && object.getClass().isArray()) {
				// if arg is array, just set the param
				params[i] = (Object[]) object;
			} else if (object == null) {
				// use default if object is null
				Expression<?> defaultExpression = parameter.getDefaultExpression();

				if (defaultExpression == null) {
					return null;
				}

				if (parameter.isSingle()) {
					params[i] = new Object[] { defaultExpression.getSingle(event) };
				} else {
					params[i] = defaultExpression.getArray(event);
				}
			} else {
				// if arg is not array, wrap object with array
				params[i] = new Object[] { object };
			}
		}

		T[] execute = execute(event, params);
		if (execute == null || execute.length == 0) {
			return null;
		} else if (execute.length == 1) {
			return execute[0];
		} else {
			//noinspection unchecked
			return (T) execute;
		}
	}

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

	/**
	 * Only used for Skript's documentation.
	 *
	 * @return This JavaFunction object
	 */
	public JavaFunction<T> description(final String... description) {
		assert documentation.description().isEmpty();
		documentation = documentation.toBuilder()
			.description(String.join("\n", description))
			.build();
		return this;
	}

	/**
	 * Only used for Skript's documentation.
	 *
	 * @return This JavaFunction object
	 */
	public JavaFunction<T> examples(final String... examples) {
		assert documentation.examples().isEmpty();
		documentation = documentation.toBuilder()
			.addExamples(examples)
			.build();
		return this;
	}

	/**
	 * Only used for Skript's documentation.
	 *
	 * @return This JavaFunction object
	 */
	public JavaFunction<T> keywords(final String... keywords) {
		assert documentation.keywords().isEmpty();
		documentation = documentation.toBuilder()
			.addKeywords(keywords)
			.build();
		return this;
	}

	/**
	 * Only used for Skript's documentation.
	 *
	 * @return This JavaFunction object
	 */
	public JavaFunction<T> since(final String since) {
		assert documentation.since().isEmpty();
		documentation = documentation.toBuilder()
			.addSince(since)
			.build();
		return this;
	}

	public String @Nullable [] getDescription() {
		if (documentation.description().isEmpty()) {
			return null;
		}
		return documentation.description().split("\n");
	}

	public String @Nullable [] getExamples() {
		if (documentation.examples().isEmpty()) {
			return null;
		}
		return documentation.examples().toArray(new String[0]);
	}

	public String @Nullable [] getKeywords() {
		if (documentation.keywords().isEmpty()) {
			return null;
		}
		return documentation.keywords().toArray(new String[0]);
	}

	public @Nullable String getSince() {
		if (documentation.since().isEmpty()) {
			return null;
		}
		return documentation.since().getFirst();
	}

	@Override
	public boolean resetReturnValue() {
		returnedKeys = null;
		return true;
	}

	@Override
	public Documentation documentation() {
		return documentation;
	}

	@Override
	public boolean canWrite(DocumentationAdapter adapter) {
		return DocumentationDocumentable.super.canWrite(adapter);
	}

	@Override
	public void preWrite(DocumentationAdapter adapter) {
		DocumentationDocumentable.super.preWrite(adapter);
	}

	@Override
	public void write(DocumentationAdapter adapter) {
		DocumentationDocumentable.super.write(adapter);
		super.write(adapter);
	}

	@Override
	public void postWrite(DocumentationAdapter adapter) {
		DocumentationDocumentable.super.postWrite(adapter);
	}

}
