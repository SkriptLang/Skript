package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("All Ops")
@Description("The list of operators on this server.")
@Examples("set {_ops::*} to all ops")
@Since("INSERT VERSION")

public class ExprOps extends SimpleExpression<OfflinePlayer> {
	static {
		Skript.registerExpression(ExprOps.class, OfflinePlayer.class, ExpressionType.SIMPLE, "[(all [[of] the]|the)] [server] op[erator]s");
	}
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		return true;
	}

	@Override
	protected OfflinePlayer[] get(Event e) {
		return Bukkit.getOperators().toArray(new OfflinePlayer[0]);
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends OfflinePlayer> getReturnType() {
		return OfflinePlayer.class;
	}





	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "all ops";
	}

}
