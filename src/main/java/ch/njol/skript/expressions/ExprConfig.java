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
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Feature;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

@Name("Config (Experimental)")
@Description({
	"The Skript config, or a user-provided custom config file.",
	"This can be reloaded, or navigated to retrieve options."
})
@Examples({
	"""
		set {_node} to node "language" in the skript config
		if text value of {_node} is "french":
			broadcast "Bonjour!"
		""",
	"""
		set {_config} to the skript config
		set {_node} to the node "number accuracy" of {_config}
		set {_value} to the number value of {_node}""",
	"""
		register config "my cool config"
		
		on load:
			set {_config} to the config named "my cool config"
			set {_node} to node "welcome message" of {_config}
			broadcast the text value of {_node}"""
})
@Since("2.10")
public class ExprConfig extends SimpleExpression<Config> {

	static {
		Skript.registerExpression(ExprConfig.class, Config.class, ExpressionType.SIMPLE,
				"[the] [skript] config",
				"[the] config [named] %*string%"
		);
	}

	private @Nullable Config config;
	private @Nullable Literal<String> name;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!this.getParser().hasExperiment(Feature.SCRIPT_REFLECTION))
			return false;
		if (matchedPattern == 0) {
			this.config = SkriptConfig.getConfig();
			if (config == null) {
				Skript.warning("The main config is unavailable here!");
				return false;
			}
		} else {
			//noinspection unchecked
			this.name = (Literal<String>) exprs[0];
			String string = name.getSingle();
			Script script = this.getParser().getCurrentScript();
			if (!Skript.userConfigs().isRegistered(script, string)) {
				Skript.warning("You must register a config '" + string + "' in order to access it.");
				return false;
			}
			this.config = Skript.userConfigs().getConfig(script, string);
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
		if (name != null)
			return "the config named " + name.toString(event, debug);
		return "the skript config";
	}

}
