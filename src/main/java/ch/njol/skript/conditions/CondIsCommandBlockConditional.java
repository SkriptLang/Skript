package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.CommandBlock;

@Name("Is Conditional")
@Description(
	"Checks whether a command block is conditional or not."
)
@Examples({
	"if {_block} is conditional:",
		"\tmake {_block} unconditional"
})
@Since("INSERT VERSION")
public class CondIsCommandBlockConditional extends PropertyCondition<Block> {

	static {
		register(CondIsCommandBlockConditional.class, "[:un]conditional", "blocks");
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<Block>) exprs[0]);
		setNegated(parseResult.hasTag("un") ^ matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Block block) {
		if (block.getBlockData() instanceof CommandBlock cmdBlock)
			return cmdBlock.isConditional();
		return false;
	}

	@Override
	protected String getPropertyName() {
		return "conditional";
	}

}
