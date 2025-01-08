package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Lightning")
@Description("Strike lightning at a given location. Can use 'lightning effect' to create a lightning that does not harm entities or start fires.")
@Examples({
	"strike lightning at the player",
	"strike lightning effect at the victim"
})
@Since("1.4")
public class EffLightning extends Effect {
	
	static {
		Skript.registerEffect(EffLightning.class, "(create|strike) lightning(1¦[ ]effect|) %directions% %locations%");
	}

	public static @Nullable Entity lastSpawned = null;

	private Expression<Location> locations;
	private boolean effectOnly;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		locations = Direction.combine((Expression<? extends Direction>) exprs[0], (Expression<? extends Location>) exprs[1]);
		effectOnly = parseResult.mark == 1;
		return true;
	}
	
	@Override
	protected void execute(Event event) {
		for (Location loc : locations.getArray(event)) {
			if (effectOnly)
				lastSpawned = loc.getWorld().strikeLightningEffect(loc);
			else
				lastSpawned = loc.getWorld().strikeLightning(loc);
		}
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "strike lightning " + (effectOnly ? "effect " : "") + locations.toString(event, debug);
	}
	
}
