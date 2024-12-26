package ch.njol.skript.hooks.regions.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.hooks.regions.classes.Region;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.iterator.ArrayIterator;
import ch.njol.util.coll.iterator.EmptyIterator;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Peter Güttinger
 */
@Name("Blocks in Region")
@Description({
	"All blocks in a <a href='./classes.html#region'>region</a>.",
	"This expression requires a supported regions plugin to be installed."
})
@Examples({
	"loop all blocks in the region {arena.%{faction.%player%}%}:",
	"\tclear the loop-block"
})
@Since("2.1")
@RequiredPlugins("Supported regions plugin")
public class ExprBlocksInRegion extends SimpleExpression<Block> {
	static {
		Skript.registerExpression(ExprBlocksInRegion.class, Block.class, ExpressionType.COMBINED,
				"[(all|the)] blocks (in|of) [[the] region[s]] %regions%");
	}
	
	@SuppressWarnings("null")
	private Expression<Region> regions;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		regions = (Expression<Region>) exprs[0];
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	protected Block[] get(final Event e) {
		final Iterator<Block> iter = iterator(e);
		final ArrayList<Block> r = new ArrayList<>();
		while (iter.hasNext())
			r.add(iter.next());
		return r.toArray(new Block[r.size()]);
	}
	
	@Override
	@NotNull
	public Iterator<Block> iterator(final Event e) {
		final Region[] rs = regions.getArray(e);
		if (rs.length == 0)
			return EmptyIterator.get();
		return new Iterator<Block>() {
			private Iterator<Block> current = rs[0].getBlocks();
			private final Iterator<Region> iter = new ArrayIterator<>(rs, 1);
			
			@Override
			public boolean hasNext() {
				while (!current.hasNext() && iter.hasNext()) {
					final Region r = iter.next();
					if (r != null)
						current = r.getBlocks();
				}
				return current.hasNext();
			}
			
			@SuppressWarnings("null")
			@Override
			public Block next() {
				if (!hasNext())
					throw new NoSuchElementException();
				return current.next();
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	@Override
	public boolean isSingle() {
		return false;
	}
	
	@Override
	public Class<? extends Block> getReturnType() {
		return Block.class;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "all blocks in " + regions.toString(e, debug);
	}
	
}
