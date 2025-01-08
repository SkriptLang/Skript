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
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Title - Reset")
@Description("Resets the title of the player to the default values.")
@Examples({
	"reset the titles of all players",
	"reset the title"
})
@Since("2.3")
public class EffResetTitle extends Effect {
	
	static {
		Skript.registerEffect(EffResetTitle.class,
			"reset [the] title[s] [of %players%]",
			"reset [the] %players%'[s] title[s]");
	}

	private Expression<Player> recipients;
	
	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		recipients = (Expression<Player>) exprs[0];
		return true;
	}
	
	@Override
	protected void execute(Event event) {
		for (Player recipient : recipients.getArray(event))
			recipient.resetTitle();
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "reset the title of " + recipients.toString(event, debug);
	}
	
}
