package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.scoreboard.Criterion;
import ch.njol.skript.util.scoreboard.ScoreUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// TODO doc
public class ExprScoreboardObjectives extends PropertyExpression<Scoreboard, Objective> {

	static {
		Skript.registerExpression(ExprScoreboardObjectives.class, Objective.class, ExpressionType.PROPERTY,
			"[the] objectives of %scoreboard%",
			"%scoreboard%'s objectives",
			"%scoreboard%'s %criteria% objectives",
			"[the] %criteria% objectives of %scoreboard%"
		);
	}

	private Expression<Scoreboard> scoreboardExpression;
	private @Nullable Expression<Criterion> criteriaExpression;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result) {
		if (pattern < 2) {
			this.scoreboardExpression = (Expression<Scoreboard>) expressions[0];
			return true;
		}
		if (pattern == 2) {
			this.scoreboardExpression = (Expression<Scoreboard>) expressions[0];
			this.criteriaExpression = (Expression<Criterion>) expressions[1];
		} else  {
			this.scoreboardExpression = (Expression) expressions[1];
			this.criteriaExpression = (Expression<Criterion>) expressions[0];
		}
		return true;
	}

	@Override
	protected Objective[] get(Event event, Scoreboard[] source) {
		List<Objective> objectives = new ArrayList<>();
		if (criteriaExpression != null) {
			Criterion[] criteria = criteriaExpression.getAll(event); // we want to include all from 'or' list
			for (Scoreboard scoreboard : scoreboardExpression.getArray(event)) {
				objectives.addAll(ScoreUtils.getObjectivesByCriteria(scoreboard, criteria));
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
