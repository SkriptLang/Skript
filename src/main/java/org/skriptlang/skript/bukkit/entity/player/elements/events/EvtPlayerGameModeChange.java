package org.skriptlang.skript.bukkit.entity.player.elements.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.LiteralList;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.GameMode;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtPlayerGameModeChange extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtPlayerGameModeChange.class, "Player GameMode Change")
			.supplier(EvtPlayerGameModeChange::new)
			.addEvent(PlayerGameModeChangeEvent.class)
			.addPatterns("[player] game[ ]mode change [to %-gamemodes%]")
			.addDescription("""
				Called when a player's gamemode is changed. See <a href='#gamemode'>gamemode</a>.
				""")
			.addExample("""
				on game mode change:
					if event-gamemode is creative:
						send "Your gamemode is now creative! Totally don't go spawning items in.."
				""")
			.addExample("""
				on game mode change:
					send "Wow! Your gamemode changed!"
				""")
			.addSince("1.0")
			.build());

		eventValueRegistry.register(EventValue.builder(PlayerGameModeChangeEvent.class, GameMode.class)
			.getter(PlayerGameModeChangeEvent::getNewGameMode)
			.patterns("gamemode")
			.build());
	}

	private Literal<GameMode> gamemodes;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[0] != null) {
			gamemodes = (Literal<GameMode>) args[0];
			if (gamemodes.getAnd() && gamemodes instanceof LiteralList<GameMode> list)
				list.invertAnd();
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (gamemodes == null)
			return true;

		PlayerGameModeChangeEvent playerEvent = (PlayerGameModeChangeEvent) event;
		return this.gamemodes.check(event, gamemode -> gamemode.equals(playerEvent.getNewGameMode()));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("player gamemode change")
			.appendIf(gamemodes != null, "to", gamemodes)
			.toString();
	}

}
