package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.ParseTimeConstant;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Version;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

@Name("Version of")
@Description({
	"The version of Skript, Minecraft, the server, or another plugin, as a comparable version.",
	"\"skript\", \"minecraft\"/\"mc\" and \"server\"/\"bukkit\" return the platform versions; " +
		"any other name is looked up as a plugin (the plugin must be enabled).",
	"Versions can be compared with strings like \"2.15\" using e.g. 'is at least', 'is less than' or 'is'."
})
@Example("if version of \"skript\" is at least \"2.15\":")
@Example("""
	parse if version of "SkBee" is at least "3.0":
		set data tag "key" of {_entity} to true
	""")
@Since("INSERT VERSION")
public class ExprVersionOf extends SimpleExpression<Version> implements ParseTimeConstant {

	static {
		Skript.registerExpression(ExprVersionOf.class, Version.class, ExpressionType.SIMPLE,
			"[the] version of %string%");
	}

	@SuppressWarnings("null")
	private Expression<String> name;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		name = (Expression<String>) exprs[0];
		return true;
	}

	@Override
	protected Version[] get(Event event) {
		String name = this.name.getSingle(event);
		if (name == null)
			return new Version[0];

		Version version = switch (name.toLowerCase()) {
			case "skript" -> Skript.getVersion();
			case "minecraft", "mc" -> Skript.getMinecraftVersion();
			case "server", "bukkit" -> Version.parse(Bukkit.getBukkitVersion());
			default -> {
				Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
				yield plugin != null && plugin.isEnabled()
					? Version.parse(plugin.getDescription().getVersion())
					: null;
			}
		};
		return version == null ? new Version[0] : new Version[]{version};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Version> getReturnType() {
		return Version.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "version of " + name.toString(event, debug);
	}

}
