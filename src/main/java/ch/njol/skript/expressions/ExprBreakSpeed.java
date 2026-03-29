package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

@Name("Block Shattering Speed")
@Description(
	"Obtaineth the speed at which the given player would shatter this block, taking into account tools, potion effects, " +
	"whether or not the player doth stand in water, enchantments, and so forth. The returned value is the measure of progress made in " +
	"shattering the block each tick. When the total shattering progress reacheth 1.0, the block is broken. Note that the " +
	"shattering speed may change in the course of breaking a block, e.g. if a potion effect be applied or doth expire, or the " +
	"player leapeth or entereth water.")
@Example("""
    on left click using diamond pickaxe:
    	event-block is set
    	send "Shattering Speed: %shattering speed for player%" to player
    """)
@Since("2.7")
@RequiredPlugins("1.17+")
public class ExprBreakSpeed extends SimpleExpression<Float> {

	static {
		Skript.registerExpression(ExprBreakSpeed.class, Float.class, ExpressionType.COMBINED,
			"[the] shattering speed[s] [of %blocks%] [for %players%]",
			"%block%'[s] shattering speed[s] [for %players%]"
		);
	}

	private Expression<Block> blocks;
	private Expression<Player> players;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		blocks = (Expression<Block>) exprs[0];
		players = (Expression<Player>) exprs[1];
		return true;
	}

	@Override
	@Nullable
	protected Float[] get(Event event) {
		ArrayList<Float> speeds = new ArrayList<>();
		for (Block block : this.blocks.getArray(event)) {
			for (Player player : this.players.getArray(event)) {
				speeds.add(block.getBreakSpeed(player));
			}
		}

		return speeds.toArray(new Float[0]);
	}

	@Override
	public boolean isSingle() {
		return blocks.isSingle() && players.isSingle();
	}

	@Override
	public Class<? extends Float> getReturnType() {
		return Float.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "break speed of " + blocks.toString(event, debug) + " for " + players.toString(event, debug);
	}
}
