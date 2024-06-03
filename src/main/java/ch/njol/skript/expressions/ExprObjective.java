package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.scoreboard.Criterion;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

// todo doc
public class ExprObjective extends SimpleExpression<Objective> {

	static {
		Skript.registerExpression(ExprObjective.class, Objective.class, ExpressionType.SIMPLE,
			"[the] objective [named] %string% in %scoreboard%",
			"%scoreboard%'s objective [named] %string%",
			"[a] new objective [named] %string% for %criterion% in %scoreboard%",
			"[a] new objective [named] %string% for %criterion% with display[ ]name %string% in %scoreboard%"
		);
	}

	private @UnknownNullability Expression<String> nameExpression, displayNameExpression;
	private @UnknownNullability Expression<Scoreboard> scoreboardExpression;
	private @UnknownNullability Expression<Criterion> criterionExpression;
	private boolean get;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, SkriptParser.ParseResult result) {
		switch (pattern) {
			case 0:
				this.get = true;
				this.nameExpression = (Expression<String>) expressions[0];
				this.scoreboardExpression = (Expression<Scoreboard>) expressions[1];
				break;
			case 1:
				this.get = true;
				this.scoreboardExpression = (Expression<Scoreboard>) expressions[0];
				this.nameExpression = (Expression<String>) expressions[1];
				break;
			case 2:
				this.nameExpression = (Expression<String>) expressions[0];
				this.criterionExpression = (Expression<Criterion>) expressions[1];
				this.scoreboardExpression = (Expression<Scoreboard>) expressions[2];
				this.displayNameExpression = nameExpression;
				break;
			case 3:
				this.nameExpression = (Expression<String>) expressions[0];
				this.criterionExpression = (Expression<Criterion>) expressions[1];
				this.displayNameExpression = (Expression<String>) expressions[2];
				this.scoreboardExpression = (Expression<Scoreboard>) expressions[3];
		}
		return true;
	}

	@Override
	protected @Nullable Objective[] get(Event event) {
		String name = this.nameExpression.getSingle(event);
		Scoreboard scoreboard = this.scoreboardExpression.getSingle(event);
		if (name == null || scoreboard == null)
			return new Objective[0];
		if (get) {
			return new Objective[] {scoreboard.getObjective(name)};
		} else {
			@Nullable Criterion criterion = criterionExpression.getSingle(event);
			@NotNull String displayName = displayNameExpression.getOptionalSingle(event).orElse(name);
			if (criterion == null)
				return new Objective[0];
			return new Objective[] {criterion.registerObjective(scoreboard, name, displayName)};
		}
	}

	@Override
	public Class<? extends Objective> getReturnType() {
		return Objective.class;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (get)
			return "the objective named " + nameExpression.toString(event, debug) + " in " + scoreboardExpression.toString(event, debug);
		return "a new objective named " + nameExpression.toString(event, debug)
			+ " for " + criterionExpression.toString(event, debug)
			+ " with display name " + displayNameExpression.toString(event, debug)
			+ " in " + scoreboardExpression.toString(event, debug);
	}

}
