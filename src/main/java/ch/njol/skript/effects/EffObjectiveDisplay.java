package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.jetbrains.annotations.Nullable;

// todo doc
public class EffObjectiveDisplay extends Effect {

	static {
		Skript.registerEffect(EffObjectiveDisplay.class,
			"make %objectives% display (0:below [the] name)",
			"make %objectives% display in [the] (1:player[ ]list|2:side[ ]bar)",
			"make %objectives% display (3:nowhere)");
	}

	private Expression<Objective> objectiveExpression;
	private @Nullable DisplaySlot slot;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, SkriptParser.ParseResult result) {
		DisplaySlot[] slots = DisplaySlot.values();
		if (result.mark < 3) // paper added some pointless extra display slot enums
			this.slot = slots[result.mark];
		this.objectiveExpression = (Expression<Objective>) expressions[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Objective objective : objectiveExpression.getArray(event))
			objective.setDisplaySlot(slot);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String place;
		if (slot == null)
			place = "nowhere";
		else switch (slot) {
			case BELOW_NAME:
				place = "below the name";
				break;
			case PLAYER_LIST:
				place = "in the player list";
				break;
			default:
				place = "in the side bar";
		}
		return "make " + objectiveExpression.toString(event, debug) + " display " + place;
	}

}
