package ch.njol.skript.expressions;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.BlockSphereIterator;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.iterator.EmptyIterator;
import ch.njol.util.coll.iterator.IteratorIterable;

/**
 * @author Peter Güttinger
 */
@Name("Block Sphere")
@Description("All blocks in a sphere around a center, mostly useful for looping.")
@Examples({"loop blocks in radius 5 around the player:",
			"\tset loop-block to air"})
@Since("1.0")
public class ExprBlockSphere extends SimpleExpression<Block> {
	static {
		Skript.registerExpression(ExprBlockSphere.class, Block.class, ExpressionType.COMBINED,
				"[(all [[of] the]|the)] blocks in radius %number% [(of|around) %location%]",
				"[(all [[of] the]|the)] blocks around %location% in radius %number%");
	}
	
	@SuppressWarnings("null")
	private Expression<Number> radius;
	@SuppressWarnings("null")
	private Expression<Location> center;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		radius = (Expression<Number>) exprs[matchedPattern];
		center = (Expression<Location>) exprs[1 - matchedPattern];
		return true;
	}
	
	@Override
	public Iterator<Block> iterator(final Event e) {
		final Location l = center.getSingle(e);
		final Number r = radius.getSingle(e);
		if (l == null || r == null)
			return new EmptyIterator<>();
		return new BlockSphereIterator(l, r.doubleValue());
	}
	
	@Override
	@Nullable
	protected Block[] get(final Event e) {
		final Number r = radius.getSingle(e);
		if (r == null)
			return new Block[0];
		final ArrayList<Block> list = new ArrayList<>((int) (1.1 * 4 / 3. * Math.PI * Math.pow(r.doubleValue(), 3)));
		for (final Block b : new IteratorIterable<>(iterator(e)))
			list.add(b);
		return list.toArray(new Block[list.size()]);
	}
	
	@Override
	public Class<? extends Block> getReturnType() {
		return Block.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the blocks in radius " + radius + " around " + center.toString(e, debug);
	}
	
	@Override
	public boolean isLoopOf(final String s) {
		return s.equalsIgnoreCase("block");
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
}
