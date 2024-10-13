package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.Lidded;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Open/Close Lid")
@Description("Open or close the lid of the block(s).")
@Examples({
	"open the lid of {_chest}",
	"close the lid of {_blocks::*}"
})
@Since("INSERT VERSION")
public class EffLidState extends Effect {

	static {
		Skript.registerEffect(EffLidState.class,
			"(open|:close) [the] lid[s] (of|for) %blocks%",
			"(open|:close) %blocks%'[s] lid[s]"
		);
	}

	private boolean setOpen;
	private Expression<Block> blocks;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setOpen = !parseResult.hasTag("close");
		blocks = (Expression<Block>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Block block : blocks.getArray(event)) {
			if (block.getState() instanceof Lidded lidded) {
				if (setOpen) {
					lidded.open();
				} else {
					lidded.close();
				}
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (setOpen ? "open" : "close") + " lid of " + blocks.toString(event, debug);
	}

}
