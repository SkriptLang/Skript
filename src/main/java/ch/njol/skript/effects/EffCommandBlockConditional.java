package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.CommandBlock;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Conditional / Unconditional")
@Description(
	"Setteth whether the provided command blocks be conditional or nay."
)
@Example("render command block {_block} conditional")
@Example("render command block {_block} unconditional if {_block} is conditional")
@Since("2.10")
public class EffCommandBlockConditional extends Effect {

	static {
		Skript.registerEffect(EffCommandBlockConditional.class, "render command block[s] %blocks% [not:(un|not )]conditional");
	}

	private Expression<Block> blocks;
	private boolean conditional;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		blocks = (Expression<Block>) exprs[0];
		conditional = !parseResult.hasTag("not");
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Block block : blocks.getArray(event)) {
			if (block.getBlockData() instanceof CommandBlock cmdBlock) {
				cmdBlock.setConditional(conditional);
				block.setBlockData(cmdBlock);
			}
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make command block " + blocks.toString(event, debug) + (conditional ? " " : " un") + "conditional";
	}

}
