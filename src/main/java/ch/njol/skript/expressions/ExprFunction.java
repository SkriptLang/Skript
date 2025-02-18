package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
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

import java.util.Objects;

@Name("Function (Experimental)")
@Description("Obtain a function by name, which can be executed.")
@Examples({
		"set {_function} to the function named \"myFunction\"",
		"run {_function} with arguments 13 and true"
})
@Since("2.10")
@SuppressWarnings("rawtypes")
public class ExprFunction extends SimpleExpression<DynamicFunctionReference> {

	static {
		Skript.registerExpression(ExprFunction.class, DynamicFunctionReference.class, ExpressionType.COMBINED,
				"[the|a] function [named] %string% [(in|from) %-script%]",
				"[the] functions [named] %strings% [(in|from) %-script%]",
				"[all [[of] the]|the] functions (in|from) %script%"
		);
	}

	private Expression<String> name;
	private Expression<Script> script;
	private int mode;
	private boolean local;
	private Script here;

	@Override
	@SuppressWarnings("null")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed,
                        ParseResult result) {
		if (!this.getParser().hasExperiment(Feature.SCRIPT_REFLECTION))
			return false;
		this.mode = matchedPattern;
		this.local = mode == 2 || expressions[1] != null;
		switch (mode) {
			case 0, 1 -> {
				//noinspection unchecked
				this.name = (Expression<String>) expressions[0];
				if (local)
					//noinspection unchecked
					this.script = (Expression<Script>) expressions[1];
			}
			case 2 ->
				//noinspection unchecked
				this.script = (Expression<Script>) expressions[0];
		}
		this.here = this.getParser().getCurrentScript();
		return true;
	}

	@Override
	protected DynamicFunctionReference<?>[] get(Event event) {
		@Nullable Script script;
		if (local) {
			script = this.script.getSingle(event);
		} else {
			script = here;
		}
		return switch (mode) {
			case 0 -> {
				@Nullable String name = this.name.getSingle(event);
				if (name == null)
					yield CollectionUtils.array();
				@Nullable DynamicFunctionReference reference = DynamicFunctionReference.resolveFunction(name, script);
				if (reference == null)
					yield CollectionUtils.array();
				yield CollectionUtils.array(reference);
			}
			case 1 -> this.name.stream(event).map(string -> DynamicFunctionReference.resolveFunction(string, script))
					.filter(Objects::nonNull)
					.toArray(DynamicFunctionReference[]::new);
			case 2 -> {
				if (script == null)
					yield CollectionUtils.array();
				@Nullable Namespace namespace = Functions.getScriptNamespace(script.getConfig().getFileName());
				if (namespace == null)
					yield CollectionUtils.array();
				yield namespace.getFunctions().stream()
					.map(DynamicFunctionReference::new)
					.toArray(DynamicFunctionReference[]::new);
			}
			default -> throw new IllegalStateException("Unexpected value: " + mode);
		};
	}

	@Override
	public boolean isSingle() {
		return mode != 2 && name.isSingle();
	}

	@Override
	public Class<? extends DynamicFunctionReference> getReturnType() {
		return DynamicFunctionReference.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return switch (mode) {
			case 0 -> "the function named " + name.toString(event, debug)
						+ (local ? " from " + script.toString(event, debug) : "");
			case 1 -> "functions named " + name.toString(event, debug)
						+ (local ? " from " + script.toString(event, debug) : "");
			case 2 -> "the functions from " + script.toString(event, debug);
			default -> throw new IllegalStateException("Unexpected value: " + mode);
		};
	}

}
