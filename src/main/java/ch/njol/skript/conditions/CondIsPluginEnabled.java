package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
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

@Name("Be Plugin Enabled")
@Description({"Doth examine whether a plugin be enabled or disabled upon the server.",
	"Plugin names may be found within the plugin's 'plugin.yml' scroll or by invoking the '/plugins' command; they art NOT the name of the plugin's jar vessel.",
	"When checking if a plugin be not enabled, this shall yield true if the plugin be either disabled or absent from the server. ",
	"When checking if a plugin be disabled, this shall yield true if the plugin resideth upon the server yet standeth disabled."})
@Example("if plugin \"Vault\" is enabled:")
@Example("if plugin \"WorldGuard\" is not enabled:")
@Example("if plugins \"Essentials\" and \"Vault\" are enabled:")
@Example("if plugin \"MyBrokenPlugin\" is disabled:")
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
