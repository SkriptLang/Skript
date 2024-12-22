package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.config.Config;
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

@Name("Config (Experimental)")
@Description({
	"The Skript config.",
	"This can be reloaded, or navigated to retrieve options."
})
@Examples({"""
	set {_node} to node "language" in the skript config
	if text value of {_node} is "french":
		broadcast "Bonjour!"
	"""})
@Since("INSERT VERSION")
public class ExprConfig extends SimpleExpression<Config> {

	static {
		Skript.registerExpression(ExprConfig.class, Config.class, ExpressionType.SIMPLE,
			"[the] [skript] config"
		);
	}

	private @Nullable Config config;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!this.getParser().hasExperiment(Feature.SCRIPT_REFLECTION))
			return false;
		this.config = SkriptConfig.getConfig();
		if (config == null) {
			Skript.warning("The main config is unavailable here!");
			return false;
		}
		return true;
	}

	@Override
	protected Config[] get(Event event) {
		if (config == null || !config.valid())
			this.config = SkriptConfig.getConfig();
		if (config != null && config.valid())
			return new Config[] {config};
		return new Config[0];
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Config> getReturnType() {
		return Config.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the skript config";
	}

}
