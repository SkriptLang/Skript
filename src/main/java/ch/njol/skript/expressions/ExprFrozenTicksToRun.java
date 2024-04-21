package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Frozen Ticks To Run")
@Description({
	"Gets the amount of frozen ticks left to run on the server.",
	"Requires Minecraft 1.20.4+"})
@Examples({"broadcast \"%frozen ticks to run%\""})
@Since("INSERT VERSION")
public class ExprFrozenTicksToRun extends SimpleExpression<Number> {

	static {
		if (Skript.methodExists(Server.class, "getServerTickManager")) {
			Skript.registerExpression(ExprFrozenTicksToRun.class, Number.class, ExpressionType.SIMPLE, "[amount of] frozen ticks [left] to run");
		}
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "frozen ticks to run";
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	@Nullable
	protected Number[] get(Event event) {
		return new Number[]{Bukkit.getServer().getServerTickManager().getFrozenTicksToRun()};
	}
}
