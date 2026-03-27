package ch.njol.skript.effects;

import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.command.ScriptCommandEvent;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;

@Name("Cancel Command Cooldown")
@Description({"Only usable in commands. Makes it so the current command usage isn't counted towards the cooldown."})
@Example("""
	command /nick <text>:
		executable by: players
		cooldown: 10 seconds
		trigger:
			if length of arg-1 is more than 16:
				# Makes it so that invalid arguments don't make you wait for the cooldown again
				cancel the cooldown
				send "Your nickname may be at most 16 characters."
				stop
			set the player's display name to arg-1
	""")
@Since("2.2-dev34")
public class EffCancelCooldown extends Effect {

	static {
		Skript.registerEffect(EffCancelCooldown.class,
				"(cancel|ignore) [the] [current] [command] cooldown",
				"un(cancel|ignore) [the] [current] [command] cooldown");
	}

	private boolean cancel;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		if (!getParser().isCurrentEvent(ScriptCommandEvent.class)) {
			Skript.error("The cancel cooldown effect may only be used in a command", ErrorQuality.SEMANTIC_ERROR);
			return false;
		}
		cancel = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event e) {
		if (!(e instanceof ScriptCommandEvent))
			return;

		((ScriptCommandEvent) e).setCooldownCancelled(cancel);
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return (cancel ? "" : "un") + "cancel the command cooldown";
	}

}
