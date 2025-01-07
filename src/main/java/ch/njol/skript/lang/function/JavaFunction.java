package ch.njol.skript.lang.function;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.util.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * @author Peter Güttinger
 */
public abstract class JavaFunction<T> extends Function<T> {
	
	public JavaFunction(Signature<T> sign) {
		super(sign);
	}

	public JavaFunction(String name, Parameter<?>[] parameters, ClassInfo<T> returnType, boolean single) {
		this(name, parameters, returnType, single, null);
	}

	public JavaFunction(String name, Parameter<?>[] parameters, ClassInfo<T> returnType, boolean single, @Nullable Contract contract) {
		this(new Signature<>("none", name, parameters, false, returnType, single, Thread.currentThread().getStackTrace()[3].getClassName(), contract));
	}
	
	@Override
	public abstract T @Nullable [] execute(FunctionEvent<?> event, Object[][] params);

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
		return true;
	}

}
