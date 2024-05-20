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
package ch.njol.skript.expressions;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptCommand;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.function.DynamicFunctionReference;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.Namespace;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Feature;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

import java.io.File;
import java.util.Objects;

@Name("Function (Experimental)")
@Description("Obtain a function by name, which can be executed.")
@Examples({"set {_function} to the function named \"myFunction\"",
		"run {_function} with arguments 13 and true"})
@Since("INSERT VERSION")
@SuppressWarnings({"rawtypes", "NotNullFieldNotInitialized"})
public class ExprFunction extends SimpleExpression<DynamicFunctionReference> {

	static {
		Skript.registerExpression(ExprFunction.class, DynamicFunctionReference.class, ExpressionType.SIMPLE,
				"[the|a] function [named] %string% [local:(in|from) %-script%]",
				"[the] functions [named] %strings% [local:(in|from) %-script%]",
				"[all] [the] functions (in|from) %script%"
		);
	}

	private Expression<String> name;
	private Expression<Script> script;
	private int mode;
	private boolean local;
	private Script here;

	@SuppressWarnings("null")
	@Override
	public boolean init(final Expression<?>[] expressions, final int matchedPattern, final Kleenean isDelayed,
                        final ParseResult result) {
		if (!this.getParser().hasExperiment(Feature.SCRIPT_REFLECTION))
			return false;
		this.mode = matchedPattern;
		this.local = result.hasTag("local") || mode == 2;
		switch (mode) {
			case 0:
			case 1:
				//noinspection unchecked
				this.name = (Expression<String>) expressions[0];
				if (local)
					//noinspection unchecked
					this.script = (Expression<Script>) expressions[1];
				break;
			case 2:
				//noinspection unchecked
				this.script = (Expression<Script>) expressions[0];
		}
		this.here = this.getParser().getCurrentScript();
		return true;
	}

	@Override
	protected DynamicFunctionReference<?>[] get(final Event event) {
		final @Nullable Script script;
		if (local)
			script = this.script.getSingle(event);
		else
			script = here;
		switch (mode) {
			case 0:
				@Nullable String name = this.name.getSingle(event);
				if (name == null)
					return CollectionUtils.array();
				@Nullable DynamicFunctionReference reference = this.resolveFunction(name, script);
				if (reference == null)
					return CollectionUtils.array();
				return CollectionUtils.array(reference);
			case 1:
				return this.name.stream(event).map(string -> this.resolveFunction(string, script))
						.filter(Objects::nonNull)
						.toArray(DynamicFunctionReference[]::new);
			case 2:
				if (script == null)
					return CollectionUtils.array();
				@Nullable Namespace namespace = Functions.getScriptNamespace(script.getConfig().getFileName());
				if (namespace == null)
					return CollectionUtils.array();
				return namespace.getFunctions().stream()
						.map(DynamicFunctionReference::new)
						.toArray(DynamicFunctionReference[]::new);
		}
		assert false;
		return null;
	}

	private @Nullable DynamicFunctionReference resolveFunction(String name, @Nullable Script script) {
		// Function reference string-ifying appends a () and potentially its source,
		// e.g. `myFunction() from MyScript.sk` and we should turn that into a valid function.
		if (script == null && !local && name.contains(") from ")) {
			// The user might be trying to resolve a local function by name only
			String source = name.substring(name.lastIndexOf(" from ") + 6).trim();
			script = getScript(source);
		}
		if (name.contains("(") && name.contains(")"))
			name = name.replaceAll("\\(.*\\).*", "").trim();
		// In the future, if function overloading is supported, we could even use the header
		// to specify parameter types (e.g. "myFunction(text, player)"
		DynamicFunctionReference<Object> reference = new DynamicFunctionReference<>(name, script);
		if (!reference.valid())
			return null;
		return reference;
	}

	private @Nullable Script getScript(@Nullable String source) {
		if (source == null || source.isEmpty())
			return null;
		@Nullable File file = SkriptCommand.getScriptFromName(source);
		if (file == null || file.isDirectory())
			return null;
		return ScriptLoader.getScript(file);
	}

	@Override
	public boolean isSingle() {
		return mode == 0 || mode == 1 && name.isSingle();
	}

	@Override
	public Class<? extends DynamicFunctionReference> getReturnType() {
		return DynamicFunctionReference.class;
	}

	@Override
	public String toString(final @Nullable Event event, final boolean debug) {
		switch (mode) {
			case 0:
				return "the function named " + name.toString(event, debug)
						+ (local ? " from " + script.toString(event, debug) : "");
			case 1:
				return "functions named " + name.toString(event, debug)
						+ (local ? " from " + script.toString(event, debug) : "");
			case 2:
				return "the functions from " + script.toString(event, debug);
		}
		assert false;
		return null;
	}

}
