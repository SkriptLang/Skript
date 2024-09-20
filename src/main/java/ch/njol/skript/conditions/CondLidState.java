package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.Lidded;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Lid Is Open/Closed")
@Description("Check to see whether lidded blocks (chests, shulkers, etc.) are open or closed.")
@Examples({
	"if the lid of {_chest} is closed:",
		"\topen the lid of {_block}"
})
@Since("INSERT VERSION")
public class CondLidState extends PropertyCondition<Block> {

	static {
		Skript.registerCondition(CondLidState.class, ConditionType.PROPERTY,
			"[the] lid [state[s]] of %blocks% (is|are) (open[ed]|:close[d])",
			"[the] lid [state[s]] of %blocks% (isn't|is not|aren't|are not) (open[ed]|:close[d])",
			"%blocks%['s] lid [state[s]] (is|are) (open[ed]|:close[d])",
			"%blocks%['s] lid [state[s]] (isn't|is not|aren't|are not) (open[ed]|:close[d])"
		);
	}

	private boolean checkOpen;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		checkOpen = !parseResult.hasTag("close");
		setExpr((Expression<Block>) exprs[0]);
		if (matchedPattern == 1 || matchedPattern == 3)
			setNegated(true);
		return true;
	}

	@Override
	public boolean check(Block block) {
		return (block.getState() instanceof Lidded lidded) ? lidded.isOpen() == checkOpen : false;
	}

	@Override
	protected String getPropertyName() {
		return (checkOpen ? "opened" : "closed") + " lid state";
	}

}
