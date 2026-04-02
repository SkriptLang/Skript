package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.jetbrains.annotations.Nullable;

@Name("Egg Shall Hatch")
@Description("Whether the egg shall hatch in a Player Egg Throw event.")
@Example("""
    on player egg throw:
    	if an entity shan't hatch:
    		send "Better fortune next time!" to the player
    """)

@Since("2.7")
public class CondWillHatch extends Condition {

	static {
		Skript.registerCondition(CondWillHatch.class,
				"[the] egg (will:shall|shall not|shan't) hatch"
		);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(PlayerEggThrowEvent.class)) {
			Skript.error("You can't use the 'egg will hatch' condition outside of a Player Egg Throw event.");
			return false;
		}
		setNegated(!parseResult.hasTag("will"));
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof PlayerEggThrowEvent))
			return false;
		return ((PlayerEggThrowEvent) event).isHatching() ^ isNegated();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the egg " + (isNegated() ? "will" : "will not") + " hatch";
	}

}
