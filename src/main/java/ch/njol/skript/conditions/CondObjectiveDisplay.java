package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.jetbrains.annotations.Nullable;

// todo doc
public class CondObjectiveDisplay extends Condition {

	static {
		Skript.registerCondition(CondObjectiveDisplay.class,
			"%objectives% (is|are)[not: not|not:n't] displayed (0:below [the] name)",
			"%objectives% (is|are)[not: not|not:n't] displayed in [the] (1:player[ ]list|2:side[ ]bar)",
			"%objectives% (is|are)[not: not|not:n't] displayed");
	}

	private Expression<Objective> objectiveExpression;
	private @Nullable DisplaySlot slot;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, SkriptParser.ParseResult result) {
		if (pattern < 2) {
			DisplaySlot[] slots = DisplaySlot.values();
			if (result.mark < 3) // paper added some pointless extra display slot enums
				this.slot = slots[result.mark];
		}
		this.objectiveExpression = (Expression<Objective>) expressions[0];
		this.setNegated(result.hasTag("not"));
		return true;
	}

	@Override
	public boolean check(Event event) {
		return objectiveExpression.check(event, objective -> {
			if (slot == null)
				return objective.getDisplaySlot() != null; // is displayed
			return objective.getDisplaySlot() == slot;
		}, this.isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String place = "";
		if (slot != null) {
			switch (slot) {
				case BELOW_NAME:
					place = " below the name";
					break;
				case PLAYER_LIST:
					place = " in the player list";
					break;
				default:
					place = " in the side bar";
			}
		}
		return objectiveExpression.toString(event, debug)
			+ (objectiveExpression.isSingle() ? " is " : " are ")
			+ (this.isNegated() ? "not " : "")
			+ "displayed"
			+ place;
	}

}
