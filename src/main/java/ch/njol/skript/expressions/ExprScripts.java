package ch.njol.skript.expressions;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Feature;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Name("All Scripts (Experimental)")
@Description("Returns all of the scripts, or just the enabled or disabled ones.")
@Examples({
	"command /scripts:",
	"\ttrigger:",
	"\t\tsend \"All Scripts: %scripts%\" to player",
	"\t\tsend \"Loaded Scripts: %enabled scripts%\" to player",
	"\t\tsend \"Unloaded Scripts: %disabled scripts%\" to player"
})
@Since("INSERT VERSION")
public class ExprScripts extends SimpleExpression<Script> {

	static {
		Skript.registerExpression(ExprScripts.class, Script.class, ExpressionType.SIMPLE,
				"[all [[of] the]|the] scripts",
				"[all [[of] the]|the] (enabled|loaded) scripts",
				"[all [[of] the]|the] (disabled|unloaded) scripts");
	}

	private int pattern;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!this.getParser().hasExperiment(Feature.SCRIPT_REFLECTION))
			return false;
		this.pattern = matchedPattern;
		return true;
	}

	@Override
	protected Script[] get(Event event) {
		List<Script> scripts = new ArrayList<>();
		if (pattern <= 1)
			scripts.addAll(ScriptLoader.getLoadedScripts());
		if (pattern != 1) {
			scripts.addAll(ScriptLoader.getDisabledScripts()
					.stream()
					.map(ExprScript::getHandle)
					.filter(Objects::nonNull)
					.toList());
		}
		return scripts.toArray(new Script[0]);
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends Script> getReturnType() {
		return Script.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (pattern == 1)
		    return "all enabled scripts";
		else if (pattern == 2)
			return "all disabled scripts";
		return "all scripts";
	}

}
