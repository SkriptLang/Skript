package ch.njol.skript.effects;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.TreeSpecies;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Tree")
@Description({"Creates a tree.",
		"This may require that there is enough space above the given location and that the block below is dirt/grass, but it is possible that the tree will just grow anyways, possibly replacing every block in its path."})
@Example("grow a tall redwood tree above the clicked block")
@Since("1.0")
public class EffTree extends Effect {
	
	static {
		Skript.registerEffect(EffTree.class,
				"(grow|create|generate) tree [of type %treetype%] %directions% %locations%",
				"(grow|create|generate) %treetype% %directions% %locations%");
	}
	
	@SuppressWarnings("null")
	private Expression<Location> blocks;
	@SuppressWarnings("null")
	private Expression<TreeSpecies> type;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		type = (Expression<TreeSpecies>) exprs[0];
		blocks = Direction.combine((Expression<? extends Direction>) exprs[1], (Expression<? extends Location>) exprs[2]);
		return true;
	}

	@Override
	public void execute(Event event) {
		TreeSpecies type = this.type.getSingle(event);
		if (type == null)
			return;
		for (Location location : blocks.getArray(event)) {
			assert location != null : blocks;
			type.grow(location.getBlock());
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "grow tree of type " + type.toString(event, debug) + " " + blocks.toString(event, debug);
	}
	
}
