package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("PvP")
@Description("Set the PvP state for a given world.")
@Examples({
	"enable PvP #(current world only)",
	"disable PvP in all worlds"
})
@Since("1.3.4")
public class EffPvP extends Effect {
	
	static {
		Skript.registerEffect(EffPvP.class, "enable PvP [in %worlds%]", "disable PVP [in %worlds%]");
	}

	private Expression<World> worlds;
	private boolean enable;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		worlds = (Expression<World>) exprs[0];
		enable = matchedPattern == 0;
		return true;
	}
	
	@Override
	protected void execute(Event event) {
		for (World world : worlds.getArray(event)) {
			world.setPVP(enable);
		}
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (enable ? "enable" : "disable") + " PvP in " + worlds.toString(event, debug);
	}
	
}
