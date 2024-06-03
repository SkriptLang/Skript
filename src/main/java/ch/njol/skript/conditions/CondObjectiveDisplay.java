package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.jetbrains.annotations.Nullable;

// todo doc
public class CondObjectiveDisplay extends PropertyCondition<Objective> {

	static {
		PropertyCondition.register(CondObjectiveDisplay.class,
			PropertyType.BE,
			"displayed (0:below [the] name|1:in [the] player[ ]list|2:in [the] side[ ]bar|3:)",
			"objectives");
	}

	private @Nullable DisplaySlot slot;

	@Override
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, SkriptParser.ParseResult result) {
		DisplaySlot[] slots = DisplaySlot.values();
		if (result.mark < 3)
			this.slot = slots[result.mark];
		return true;
	}

	@Override
	public boolean check(Objective objective) {
		if (slot == null)
			return objective.getDisplaySlot() != null; // is displayed
		return objective.getDisplaySlot() == slot;
	}

	@Override
	protected String getPropertyName() {
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
		return "displayed" + place;
	}

}
