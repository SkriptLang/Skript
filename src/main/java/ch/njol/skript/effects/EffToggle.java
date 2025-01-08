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
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Toggle")
@Description("Toggle the state of a block.")
@Examples({
	"# use arrows to toggle switches, doors, etc.",
	"on projectile hit:",
		"\tprojectile is arrow",
		"\ttoggle the block at the arrow"
})
@Since("1.4")
public class EffToggle extends Effect {
	
	static {
		Skript.registerEffect(EffToggle.class, "(close|turn off|de[-]activate) %blocks%", "(toggle|switch) [[the] state of] %blocks%", "(open|turn on|activate) %blocks%");
	}

	private Expression<Block> blocks;
	private int toggle;
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		blocks = (Expression<Block>) exprs[0];
		toggle = matchedPattern - 1;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Block block : blocks.getArray(event)) {
			BlockData data = block.getBlockData();
			if (toggle == -1) {
				if (data instanceof Openable openable)
					openable.setOpen(false);
				else if (data instanceof Powerable powerable)
					powerable.setPowered(false);
			} else if (toggle == 1) {
				if (data instanceof Openable openable)
					openable.setOpen(true);
				else if (data instanceof Powerable powerable)
					powerable.setPowered(true);
			} else {
				if (data instanceof Openable openable) // open = NOT was open
					openable.setOpen(!openable.isOpen());
				else if (data instanceof Powerable powerable) // power = NOT power
					powerable.setPowered(!powerable.isPowered());
			}
			block.setBlockData(data);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "toggle " + blocks.toString(event, debug);
	}
	
}
