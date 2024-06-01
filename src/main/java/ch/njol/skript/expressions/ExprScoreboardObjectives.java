package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;

// TODO doc
public class ExprScoreboardObjectives extends PropertyExpression<Scoreboard, Objective> {

	private static final Object[] CRITERIA;
	private static final String[] NAMES;

	static {
		if (ExprScoreboard.ARE_CRITERIA_AVAILABLE) { // todo inline in 2.10?
			StringBuilder builder = new StringBuilder();
			builder.append("(");
			List<Object> list = new ArrayList<>(50);
			List<String> names = new ArrayList<>(50);
			list.add(null); // A surprise tool that will help us later!
			names.add(null);
			int count = 0;
			for (Field field : Criteria.class.getFields()) {
				try {
					if (field.getType() != Criteria.class)
						continue;
					Criteria criterion = (Criteria) field.get(null);
					String name = makeNiceName(field.getName());
					list.add(criterion);
					names.add(name);
					builder.append(++count).append(':').append(name).append('|');
				} catch (IllegalArgumentException | IllegalAccessException e) {
					Skript.exception(e, "Can't get criteria patterns.");
				}
			}
			builder.append("0:%-string%");
			builder.append(")");
			CRITERIA = list.toArray();
			NAMES = names.toArray(new String[0]);
			String criteria = builder.toString();
			Skript.registerExpression(ExprScoreboardObjectives.class, Objective.class, ExpressionType.PROPERTY,
				"[the] objectives of %scoreboard%",
				"%scoreboard%'s objectives",
				"%scoreboard%'s " + criteria + " objectives",
				"[the] " + criteria + " objectives of %scoreboard%"
			);
		} else {
			CRITERIA = new Object[0];
			NAMES = new String[0];
			Skript.registerExpression(ExprScoreboardObjectives.class, Objective.class, ExpressionType.PROPERTY,
				"[the] objectives of %scoreboard%",
				"%scoreboard%'s objectives",
				"%scoreboard%'s (0:%string%) objectives",
				"[the] (0:%string%) objectives of %scoreboard%"
			);
		}
	}

	private Expression<Scoreboard> scoreboardExpression;
	private @Nullable Function<Event, Object[]> criteriaExpression;
	private @UnknownNullability BiFunction<Event, Boolean, String> stringifier;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result) {
		if (pattern < 2) {
			this.scoreboardExpression = (Expression<Scoreboard>) expressions[0];
			return true;
		}
		int mark = result.mark;
		if (mark > 0 && mark < CRITERIA.length) {
			this.scoreboardExpression = (Expression<Scoreboard>) expressions[0];
			this.criteriaExpression = event -> new Object[]{CRITERIA[mark]};
			this.stringifier = (a, b) -> NAMES[mark];
			return true;
		}
		if (pattern == 2) {
			this.scoreboardExpression = (Expression<Scoreboard>) expressions[0];
			this.criteriaExpression = expressions[1]::getAll;
			this.stringifier = (event, debug) -> expressions[1].toString(event, debug);
		} else  {
			this.scoreboardExpression = (Expression) expressions[1];
			this.criteriaExpression = expressions[0]::getAll;
			this.stringifier = (event, debug) -> expressions[0].toString(event, debug);
		}
		return true;
	}

	@Override
	protected Objective[] get(Event event, Scoreboard[] source) {
		List<Objective> objectives = new ArrayList<>();
		if (criteriaExpression != null) {
			Object[] criteria = criteriaExpression.apply(event); // we want to include all from 'or' list
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
			return "the " + stringifier.apply(event, debug)
				+ " objectives of " + scoreboardExpression.toString(event, debug);
		return "the objectives of " + scoreboardExpression.toString(event, debug);
	}

	private static String makeNiceName(String original) {
		return original.replace('_', ' ').toLowerCase(Locale.ENGLISH);
	}

}
