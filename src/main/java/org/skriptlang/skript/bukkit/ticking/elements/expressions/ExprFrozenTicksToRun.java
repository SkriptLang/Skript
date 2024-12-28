package org.skriptlang.skript.bukkit.ticking.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.ServerUtils;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Frozen Ticks to Run")
@Description("Gets the amount of ticks that are in queue to run while the server's tick state is frozen.")
@Examples("broadcast \"There are %frozen ticks to run% frozen ticks left!\"")
@Since("INSERT VERSION")
@RequiredPlugins("Minecraft 1.20.4+")
public class ExprFrozenTicksToRun extends SimpleExpression<Integer> {

	static {
		if (ServerUtils.isServerTickManagerPresent())
			Skript.registerExpression(ExprFrozenTicksToRun.class, Integer.class, ExpressionType.SIMPLE, "[the] [amount of] frozen ticks [left] to run");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	protected Integer @Nullable [] get(Event event) {
		int frozenTicks = ServerUtils.getServerTickManager().getFrozenTicksToRun();
		if (frozenTicks > 0) {
			return new Integer[]{frozenTicks};
		}
		return new Integer[0];
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the frozen ticks left to run";
	}

}
