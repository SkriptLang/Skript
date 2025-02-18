package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * @author Peter Güttinger
 */
@Name("Light Level")
@Description({"Gets the light level at a certain location which ranges from 0 to 15.",
		"It can be separated into sunlight (15 = direct sunlight, 1-14 = indirect) and block light (torches, glowstone, etc.). The total light level of a block is the maximum of the two different light types."})
@Examples({"# set vampire players standing in bright sunlight on fire",
		"every 5 seconds:",
		"	loop all players:",
		"		{vampire::%uuid of loop-player%} is true",
		"		sunlight level at the loop-player is greater than 10",
		"		ignite the loop-player for 5 seconds"})
@Since("1.3.4")
public class ExprLightLevel extends PropertyExpression<Location, Byte> {
	static {
		Skript.registerExpression(ExprLightLevel.class, Byte.class, ExpressionType.PROPERTY, "[(1¦sky|1¦sun|2¦block)[ ]]light[ ]level [(of|%direction%) %location%]");
	}
	
	private final int SKY = 1, BLOCK = 2, ANY = SKY | BLOCK;
	int whatLight = ANY;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		setExpr(Direction.combine((Expression<? extends Direction>) exprs[0], (Expression<? extends Location>) exprs[1]));
		whatLight = parseResult.mark == 0 ? ANY : parseResult.mark;
		return true;
	}
	
	@Override
	public Class<Byte> getReturnType() {
		return Byte.class;
	}
	
	@Override
	protected Byte[] get(final Event e, final Location[] source) {
		return get(source, location -> {
			Block block = location.getBlock();
			return whatLight == ANY ? block.getLightLevel()
				: whatLight == BLOCK ? block.getLightFromBlocks() : block.getLightFromSky();
		});
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return (whatLight == BLOCK ? "block " : whatLight == SKY ? "sky " : "") + "light level " + getExpr().toString(e, debug);
	}
	
}
