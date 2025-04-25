package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Last Reloaded Errors")
@Description("The errors from the last singular or multiple scripts that were reloaded or enabled via an effect.")
@Example("""
	reload script named "test.sk"
	if last reloaded errors is set:
		disable script file "test.sk"
	""")
@Example("""
	enable script named "-test.sk"
	if last enabled errors is set:
		disable script file "test.sk"
	""")
@Since("INSERT VERSION")
public class ExprScriptFileErrors extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprScriptFileErrors.class, String.class, ExpressionType.SIMPLE,
			"[the] last (:reloaded|enabled) [script|skript] errors");
	}

	public static String[] lastReloadedErrors = null;
	public static String[] lastEnabledErrors = null;

	private boolean getReloaded;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		getReloaded = parseResult.hasTag("reloaded");
		return true;
	}

	@Override
	protected String @Nullable [] get(Event event) {
		return getReloaded ? lastReloadedErrors : lastEnabledErrors;
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the last reloaded script errors";
	}

}
