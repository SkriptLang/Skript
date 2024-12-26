package ch.njol.skript.effects;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings("deprecation")
@Name("Toggle")
@Description("Toggle the state of a block.")
@Examples({"# use arrows to toggle switches, doors, etc.",
		"on projectile hit:",
		"\tprojectile is arrow",
		"\ttoggle the block at the arrow"})
@Since("1.4")
public class EffToggle extends Effect {
	
	static {
		Skript.registerEffect(EffToggle.class, "(close|turn off|de[-]activate) %blocks%", "(toggle|switch) [[the] state of] %blocks%", "(open|turn on|activate) %blocks%");
	}

	@SuppressWarnings("null")
	private Expression<Block> blocks;
	private int toggle;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] vars, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		blocks = (Expression<Block>) vars[0];
		toggle = matchedPattern - 1;
		return true;
	}

	@Override
	protected void execute(final Event e) {
		for (Block b : blocks.getArray(e)) {
			BlockData data = b.getBlockData();
			if (toggle == -1) {
				if (data instanceof Openable)
					((Openable) data).setOpen(false);
				else if (data instanceof Powerable)
					((Powerable) data).setPowered(false);
			} else if (toggle == 1) {
				if (data instanceof Openable)
					((Openable) data).setOpen(true);
				else if (data instanceof Powerable)
					((Powerable) data).setPowered(true);
			} else {
				if (data instanceof Openable) // open = NOT was open
					((Openable) data).setOpen(!((Openable) data).isOpen());
				else if (data instanceof Powerable) // power = NOT power
					((Powerable) data).setPowered(!((Powerable) data).isPowered());
			}
			
			b.setBlockData(data);
		}
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "toggle " + blocks.toString(e, debug);
	}
	
}
