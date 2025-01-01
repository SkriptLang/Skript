package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.block.Block;

@Name("Temperature")
@Description("Temperature at given block.")
@Examples("message \"%temperature of the targeted block%\"")
@Since("2.2-dev35")
public class ExprTemperature extends SimplePropertyExpression<Block, Number> {

	static {
		register(ExprTemperature.class, Number.class, "temperature[s]", "blocks");
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
