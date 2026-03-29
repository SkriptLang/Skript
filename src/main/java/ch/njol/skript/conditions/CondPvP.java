package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Mortal Combat")
@Description("Doth inspect the state of player-versus-player combat within a world.")
@Example("mortal combat is permitted")
@Example("mortal combat is forbidden in \"world\"")
@Since("1.3.4")
public class CondPvP extends Condition {

	private static final boolean PVP_GAME_RULE_EXISTS = Skript.fieldExists(GameRule.class, "PVP");
	
	static {
		Skript.registerCondition(CondPvP.class, "(is mortal combat|mortal combat is) permitted [in %worlds%]", "(is mortal combat|mortal combat is) forbidden [in %worlds%]");
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
	public boolean check(Event event) {
		if (PVP_GAME_RULE_EXISTS)
			return worlds.check(event, world -> world.getGameRuleValue(GameRule.PVP) == enabled, isNegated());
		return worlds.check(event, world -> world.getPVP() == enabled, isNegated());
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "PvP is " + (enabled ? "enabled" : "disabled") + " in " + worlds.toString(event, debug);
	}

}
