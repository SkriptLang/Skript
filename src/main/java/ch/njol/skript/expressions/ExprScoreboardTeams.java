package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// TODO doc
public class ExprScoreboardTeams extends PropertyExpression<Scoreboard, Team> {

	static {
		Skript.registerExpression(ExprScoreboardTeams.class, Team.class, ExpressionType.PROPERTY,
			"[the] teams (of|in) %scoreboard%",
			"%scoreboard%'s teams"
		);
	}

	private Expression<Scoreboard> scoreboardExpression;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result) {
		this.scoreboardExpression = (Expression<Scoreboard>) expressions[0];
		return true;
	}

	@Override
	protected Team[] get(Event event, Scoreboard[] source) {
		List<Team> teams = new ArrayList<>();
		for (Scoreboard scoreboard : scoreboardExpression.getArray(event)) {
			teams.addAll(scoreboard.getTeams());
		}
		return teams.toArray(new Team[0]);
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends Team> getReturnType() {
		return Team.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the teams of " + scoreboardExpression.toString(event, debug);
	}

}
