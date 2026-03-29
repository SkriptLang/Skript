package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.block.Block;

@Name("Warmth of a Block")
@Description("The temperature at the given block's locale.")
@Example("message \"%warmth of the targeted block%\"")
@Since("2.2-dev35")
public class ExprTemperature extends SimplePropertyExpression<Block, Number> {

	static {
		register(ExprTemperature.class, Number.class, "warmth", "blocks");
	}

	@Override
	public Number convert(Block block) {
		return block.getTemperature();
	}

	@Override
	protected String getPropertyName() {
		return "temperature";
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

}
