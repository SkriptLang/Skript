package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Preserve the World")
@Description({
	"Preserve all worlds or a given world by thine own hand.",
	"Note: preserving many worlds at once may perchance cause the server to freeze."
})
@Example("preserve \"world_nether\"")
@Example("preserve all worlds")
@Since("2.8.0")
public class EffWorldSave extends Effect {

	static {
		Skript.registerEffect(EffWorldSave.class, "preserve [[the] world[s]] %worlds%");
	}

	private Expression<World> worlds;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		worlds = (Expression<World>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (World world : worlds.getArray(event))
			world.save();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "save the world(s) " + worlds.toString(event, debug);
	}

}
