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
@Since("2.10")
public class CondLidState extends PropertyCondition<Block> {

	static {
		Skript.registerCondition(CondLidState.class, ConditionType.PROPERTY,
			"[the] lid[s] of %blocks% (is|are) (open[ed]|:close[d])",
			"[the] lid[s] of %blocks% (isn't|is not|aren't|are not) (open[ed]|:close[d])",
			"%blocks%'[s] lid[s] (is|are) (open[ed]|:close[d])",
			"%blocks%'[s] lid[s] (isn't|is not|aren't|are not) (open[ed]|:close[d])"
		);
	}

	private boolean checkOpen;
	private Expression<Block> blocks;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		checkOpen = !parseResult.hasTag("close");
		blocks = (Expression<Block>) exprs[0];
		setExpr(blocks);
		setNegated(matchedPattern == 1 || matchedPattern == 3);
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

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the lids of " + blocks.toString(event, debug) + (isNegated() ? "are not " : "are ") + (checkOpen ? "opened" : "closed");
	}

}
