package ch.njol.skript.hooks.permission.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.hooks.VaultHook;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("All Groups")
@Description("All the groups a player can have. This expression requires Vault and a compatible permissions plugin to be installed.")
@Examples({"command /group &lt;text&gt;:",
		"\ttrigger:",
		"\t\tif argument is \"list\":",
		"\t\t\tsend \"%all groups%\""})
@Since("2.2-dev35")
@RequiredPlugins({"Vault", "a permission plugin that supports Vault"})
public class ExprAllGroups extends SimpleExpression<String> {

	static {
		Skript.registerExpression(ExprAllGroups.class, String.class, ExpressionType.SIMPLE, "all groups");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!VaultHook.permission.hasGroupSupport()) {
			Skript.error(VaultHook.NO_GROUP_SUPPORT);
			return false;
		}
		return true;
	}

	@Override
	@Nullable
	protected String[] get(Event e) {
		return VaultHook.permission.getGroups();
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "all groups";
	}

}
