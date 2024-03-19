/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.lang.function;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.classes.ClassInfo;

/**
 * @author Peter Güttinger
 */
public abstract class JavaFunction<T> extends Function<T> {
	
	public JavaFunction(Signature<T> sign) {
		super(sign);
	}

	public JavaFunction(String name, Parameter<?>[] parameters, ClassInfo<T> returnType, boolean single) {
		this(new Signature<>("none", name, parameters, false, returnType, single, Thread.currentThread().getStackTrace()[3].getClassName()));
	}
	
	@Override
	@Nullable
	public abstract T[] execute(FunctionEvent<?> e, Object[][] params);
	
	@Nullable
	private String[] description = null;
	@Nullable
	private String[] examples = null;
	@Nullable
	private String[] keywords;
	@Nullable
	private String since = null;
	
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
	
	@Nullable
	public String[] getDescription() {
		return description;
	}
	
	@Nullable
	public String[] getExamples() {
		return examples;
	}

	@Nullable
	public String[] getKeywords() {
		return keywords;
	}
	
	@Nullable
	public String getSince() {
		return since;
	}

	@Override
	public boolean resetReturnValue() {
		return true;
	}

}
