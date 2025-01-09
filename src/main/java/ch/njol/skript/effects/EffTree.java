package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
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
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

@Name("Tree")
@Description({
	"Creates a tree.",
	"This may require that there is enough space above the given location and that the block below is dirt/grass," +
	"but it is possible that the tree will just grow anyways, possibly replacing every block in its path."
})
@Examples({"grow a tall redwood tree above the clicked block"})
@Since("1.0")
public class EffTree extends Effect implements SyntaxRuntimeErrorProducer {
	
	static {
		Skript.registerEffect(EffTree.class,
			"(grow|create|generate) tree [of type %structuretype%] %directions% %locations%",
			"(grow|create|generate) %structuretype% %directions% %locations%");
	}

	private Node node;
	private Expression<Location> blocks;
	private Expression<StructureType> type;
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		node = getParser().getNode();
		type = (Expression<StructureType>) exprs[0];
		blocks = Direction.combine((Expression<? extends Direction>) exprs[1], (Expression<? extends Location>) exprs[2]);
		return true;
	}
	
	@Override
	public void execute(Event event) {
		StructureType type = this.type.getSingle(event);
		if (type == null) {
			error("The provided tree type was null.", this.type.toString(null, false));
			return;
		}

		for (Location loc : blocks.getArray(event)) {
			assert loc != null : blocks;
			type.grow(loc.getBlock());
		}
	}

	@Override
	public Node getNode() {
		return node;
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "grow tree of type " + type.toString(event, debug) + " " + blocks.toString(event, debug);
	}

}
