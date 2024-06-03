package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.scoreboard.ScoreUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO doc
public class ExprScore extends PropertyExpression<Object, Number> {

	static {
		Skript.registerExpression(ExprScore.class, Number.class, ExpressionType.PROPERTY,
			"[the] %objective% score[plural:s] of %offlineplayers/entities%",
			"%offlineplayers/entities%'[s] %objective% score[plural:s]"
		);
	}

	private Expression<Objective> objectiveExpression;
	private Expression<?> sourceExpression;
	private boolean single;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result) {
		this.objectiveExpression = (Expression<Objective>) expressions[pattern];
		this.sourceExpression = expressions[pattern ^ 1];
		this.single = sourceExpression.isSingle() && !result.hasTag("plural");
		return true;
	}

	@Override
	protected Number[] get(Event event, Object[] source) {
		Objective objective = objectiveExpression.getSingle(event);
		if (objective == null)
			return new Number[0];
		Number[] numbers = new Number[source.length];
		for (int i = 0; i < source.length; i++) {
			numbers[i] = (long) objective.getScore(ScoreUtils.toEntry(source[i])).getScore();
		}
		return numbers;
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public boolean isSingle() {
		return single;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(Changer.ChangeMode mode) {
		switch (mode) {
			case SET:
			case ADD:
			case REMOVE:
			case RESET:
			case DELETE:
				return new Class[]{Number.class};
		}
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, Changer.ChangeMode mode) {
		Objective objective = objectiveExpression.getSingle(event);
		Object[] sources = this.getExpr().getArray(event);
		if (objective == null || !objective.isModifiable())
			return;
		@NotNull Number value;
		if (delta != null && delta.length > 0 && delta[0] instanceof Number)
			value = (Number) delta[0];
		else
			value = 0;
		if (mode == Changer.ChangeMode.REMOVE)
			value = value.intValue() * -1;
		switch (mode) {
			case DELETE:
			case RESET:
				for (Object object : sources) {
					objective.getScore(ScoreUtils.toEntry(object)).resetScore();
				}
				break;
			case SET:
				for (Object object : sources) {
					objective.getScore(ScoreUtils.toEntry(object)).setScore(value.intValue());
				}
				break;
			case ADD:
			case REMOVE:
				for (Object object : sources) {
					@NotNull Score score = objective.getScore(ScoreUtils.toEntry(object));
					score.setScore(score.getScore() + value.intValue());
				}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the " + objectiveExpression.toString(event, debug)
			+ " score of " + sourceExpression.toString(event, debug);
	}

}
