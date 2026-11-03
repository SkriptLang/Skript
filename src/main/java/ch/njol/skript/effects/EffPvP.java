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
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("PvP")
@Description("Set the PvP state for a given world.")
@Example("enable PvP #(current world only)")
@Example("disable PvP in all worlds")
@Since("1.3.4")
public class EffPvP extends Effect {

	private static final boolean PVP_GAME_RULE_EXISTS = Skript.fieldExists(GameRule.class, "PVP");

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
	protected void execute(Event event) {
		if (PVP_GAME_RULE_EXISTS) {
			for (World world : worlds.getArray(event))
				world.setGameRule(GameRule.PVP, enable);
		} else {
			for (World world : worlds.getArray(event))
				world.setPVP(enable);
		}
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (enable ? "enable" : "disable") + " PvP in " + worlds.toString(event, debug);
	}
	
}
