package ch.njol.skript.expressions;

import org.bukkit.World;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Sea Level")
@Description("Gets the sea level of a world.")
@Examples("send \"The sea level in your world is %sea level in player's world%\"")
@Since("2.5.1")
public class ExprSeaLevel extends SimplePropertyExpression<World, Long> {
	
	static {
		register(ExprSeaLevel.class, Long.class, "sea level", "worlds");
	}
	
	@Override
	public Long convert(World world) {
		return (long) world.getSeaLevel();
	}
	
	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "sea level";
	}

}
