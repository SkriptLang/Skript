package ch.njol.skript.effects;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.StructureType;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Tree")
@Description({"Creates a tree.",
		"This may require that there is enough space above the given location and that the block below is dirt/grass, but it is possible that the tree will just grow anyways, possibly replacing every block in its path."})
@Examples({"grow a tall redwood tree above the clicked block"})
@Since("1.0")
public class EffTree extends Effect {
	
	static {
		Skript.registerEffect(EffTree.class,
				"(grow|create|generate) tree [of type %structuretype%] %directions% %locations%",
				"(grow|create|generate) %structuretype% %directions% %locations%");
	}
	
	@SuppressWarnings("null")
	private Expression<Location> blocks;
	@SuppressWarnings("null")
	private Expression<StructureType> type;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parser) {
		type = (Expression<StructureType>) exprs[0];
		blocks = Direction.combine((Expression<? extends Direction>) exprs[1], (Expression<? extends Location>) exprs[2]);
		return true;
	}
	
	@Override
	public void execute(final Event e) {
		final StructureType type = this.type.getSingle(e);
		if (type == null)
			return;
		for (final Location l : blocks.getArray(e)) {
			assert l != null : blocks;
			type.grow(l.getBlock());
		}
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "grow tree of type " + type.toString(e, debug) + " " + blocks.toString(e, debug);
	}
	
}
