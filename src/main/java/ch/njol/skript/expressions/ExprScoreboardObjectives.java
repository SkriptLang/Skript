package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// TODO doc
public class ExprScoreboardObjectives extends PropertyExpression<Scoreboard, Objective> {

	static {
		String criteria = ExprScoreboard.ARE_CRITERIA_AVAILABLE ? "%criteria%" : "%strings%";
		Skript.registerExpression(ExprScoreboardObjectives.class, Objective.class, ExpressionType.PROPERTY,
			"[the] objectives of %scoreboard%",
			"%scoreboard%'s objectives",
			"%scoreboard%'s " + criteria + " objectives",
			"[the] " + criteria + " objectives of %scoreboard%",
			"[the] objectives for criteria "+criteria+" of %scoreboard%"
		);
	}

	private Expression<Scoreboard> scoreboardExpression;
	private @Nullable Expression<?> criteriaExpression;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result) {
		if (pattern > 2) {
			this.scoreboardExpression = (Expression) expressions[1];
			this.criteriaExpression = expressions[0];
		} else {
			this.scoreboardExpression = (Expression<Scoreboard>) expressions[0];
			if (pattern == 2)
				this.criteriaExpression = expressions[1];
		}
		return true;
	}

	@Override
	protected Objective[] get(Event event, Scoreboard[] source) {
		List<Objective> objectives = new ArrayList<>();
		if (criteriaExpression != null) {
			Object[] criteria = criteriaExpression.getAll(event); // we want to include all from 'or' list
			for (Scoreboard scoreboard : scoreboardExpression.getArray(event)) {
				objectives.addAll(ExprScoreboard.getObjectivesByCriteria(scoreboard, criteria));
			}
		} else {
			for (Scoreboard scoreboard : scoreboardExpression.getArray(event)) {
				objectives.addAll(scoreboard.getObjectives());
			}
		}
		return objectives.toArray(new Objective[0]);
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends Objective> getReturnType() {
		return Objective.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (criteriaExpression != null)
			return "the " + criteriaExpression.toString(event, debug)
				+ " objectives of " + scoreboardExpression.toString(event, debug);
		return "the objectives of " + scoreboardExpression.toString(event, debug);
	}

}
