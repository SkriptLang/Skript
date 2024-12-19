package ch.njol.skript.conditions;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("PvP")
@Description("Checks the PvP state of a world.")
@Examples({"PvP is enabled",
		"PvP is disabled in \"world\""})
@Since("1.3.4")
public class CondPvP extends Condition {
	
	static {
		Skript.registerCondition(CondPvP.class, "(is PvP|PvP is) enabled [in %worlds%]", "(is PvP|PvP is) disabled [in %worlds%]");
	}
	
	@SuppressWarnings("null")
	private Expression<World> worlds;
	private boolean enabled;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		worlds = (Expression<World>) exprs[0];
		enabled = matchedPattern == 0;
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		return worlds.check(e, w -> w.getPVP() == enabled, isNegated());
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "PvP is " + (enabled ? "enabled" : "disabled") + " in " + worlds.toString(e, debug);
	}
}
