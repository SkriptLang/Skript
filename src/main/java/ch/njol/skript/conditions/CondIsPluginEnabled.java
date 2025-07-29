package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

@Name("Is Plugin Enabled")
@Description({"Check if a plugin is enabled/disabled on the server.",
	"Plugin names can be found in the plugin's 'plugin.yml' file or by using the '/plugins' command, they are NOT the name of the plugin's jar file.",
	"When checking if a plugin is not enabled, this will return true if the plugin is either disabled or not on the server. ",
	"When checking if a plugin is disabled, this will return true if the plugin is on the server and is disabled."})
@Examples({"if plugin \"Vault\" is enabled:",
	"if plugin \"WorldGuard\" is not enabled:",
	"if plugins \"Essentials\" and \"Vault\" are enabled:",
	"if plugin \"MyBrokenPlugin\" is disabled:"})
@Since("2.6")
public class CondIsPluginEnabled extends Condition {

	static {
		Skript.registerCondition(CondIsPluginEnabled.class,
			"plugin[s] %strings% (is|are) enabled",
			"plugin[s] %strings% (is|are)(n't| not) enabled",
			"plugin[s] %strings% (is|are) disabled");
	}

	@SuppressWarnings("null")
	private Expression<String> plugins;
	private int pattern;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		plugins = (Expression<String>) exprs[0];
		pattern = matchedPattern;
		return true;
	}

	@Override
	public boolean check(Event e) {
		return plugins.check(e, plugin -> {
			Plugin p = Bukkit.getPluginManager().getPlugin(plugin);
			switch (pattern) {
				case 1:
					return p == null || !p.isEnabled();
				case 2:
					return p != null && !p.isEnabled();
				default:
					return p != null && p.isEnabled();
			}
		});
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		String plugin = plugins.isSingle() ? "plugin " : "plugins ";
		String plural = plugins.isSingle() ? " is" : " are";
		String pattern = this.pattern == 0 ? " enabled" : this.pattern == 1 ? " not enabled" : " disabled";
		return plugin + plugins.toString(e, debug) + plural + pattern;
	}

}
