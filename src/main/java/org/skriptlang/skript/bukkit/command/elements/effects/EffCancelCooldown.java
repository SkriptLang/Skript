package org.skriptlang.skript.bukkit.command.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Date;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.bukkit.command.custom.CommandParsingData;
import org.skriptlang.skript.bukkit.command.custom.CommandParsingData.ExecutorData;
import org.skriptlang.skript.bukkit.command.custom.ScriptCommandExecutionEvent;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Cancel Command Cooldown")
@Description("Only usable in commands. Makes it so the current command usage isn't counted towards the cooldown.")
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
public class EffCancelCooldown extends Effect implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EFFECT,
			SyntaxInfo.simple(EffCancelCooldown.class, EffCancelCooldown::new,
				"[:un](cancel|ignore) [the] [current] [command] cooldown"));
	}

	private boolean cancel;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		cancel = !parseResult.hasTag("un");
		if (getParser().getData(CommandParsingData.class).getExecutorData(ExecutorData::cooldownManager) == null) {
			Skript.error("'" + toString(null, false) + "' can't be used because the command doesn't have a cooldown.");
			return false;
		}
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(ScriptCommandExecutionEvent.class);
	}

	@Override
	protected void execute(Event event) {
		if (event instanceof ScriptCommandExecutionEvent commandEvent) {
			assert commandEvent.getCommandExecutor().getCooldownManager() != null;
			commandEvent.getCommandExecutor().getCooldownManager()
				.setStartDate(event, commandEvent.getSender(), cancel ? null : Date.now());
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (cancel ? "" : "un") + "cancel the command cooldown";
	}

}
