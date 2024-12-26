package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.block.Block;

@Name("Humidity")
@Description("Humidity of given blocks.")
@Examples("set {_humidity} to event-block's humidity")
@Since("2.2-dev35")
public class ExprHumidity extends SimplePropertyExpression<Block, Number> {

    static {
        register(ExprHumidity.class, Number.class, "humidit(y|ies)", "blocks");
    }

    @Override
    public Number convert(Block block) {
        return block.getHumidity();
    }

    @Override
    protected String getPropertyName() {
        return "humidity";
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

}
