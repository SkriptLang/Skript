package ch.njol.skript.effects;

import org.bukkit.World;
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
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("PvP")
@Description("Set the PvP state for a given world.")
@Examples({"enable PvP #(current world only)",
		"disable PvP in all worlds"})
@Since("1.3.4")
public class EffPvP extends Effect {
	
	static {
		Skript.registerEffect(EffPvP.class, "enable PvP [in %worlds%]", "disable PVP [in %worlds%]");
	}
	
	@SuppressWarnings("null")
	private Expression<World> worlds;
	private boolean enable;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		worlds = (Expression<World>) exprs[0];
		enable = matchedPattern == 0;
		return true;
	}
	
	@Override
	protected void execute(final Event e) {
		for (final World w : worlds.getArray(e)) {
			w.setPVP(enable);
		}
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return (enable ? "enable" : "disable") + " PvP in " + worlds.toString(e, debug);
	}
	
}
