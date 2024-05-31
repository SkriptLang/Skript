package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.Nullable;

public class ExprScoreboard extends SimpleExpression<Scoreboard> {

	static {
		Skript.registerExpression(ExprScoreboard.class, Scoreboard.class, ExpressionType.SIMPLE,
			"[the] [main|server] scoreboard",
			"[a] new scoreboard"
		);
	}

	private boolean main;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result) {
		this.main = pattern == 0;
		return true;
	}

	@Override
	protected @Nullable Scoreboard[] get(Event event) {
		if (main)
			return CollectionUtils.array(Bukkit.getScoreboardManager().getMainScoreboard());
		return CollectionUtils.array(Bukkit.getScoreboardManager().getNewScoreboard());
	}

	@Override
	public Class<? extends Scoreboard> getReturnType() {
		return Scoreboard.class;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (main)
			return "the main scoreboard";
		return "a new scoreboard";
	}

}
