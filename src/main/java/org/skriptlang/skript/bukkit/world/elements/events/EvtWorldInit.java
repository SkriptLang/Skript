package org.skriptlang.skript.bukkit.world.elements.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.LiteralList;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.world.WorldInitEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtWorldInit extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtWorldInit.class, "World Initialize")
			.supplier(EvtWorldInit::new)
			.addEvent(WorldInitEvent.class)
			.addPatterns("world init[ialization] [of %-worlds%]")
			.addDescription("""
				Called when a world is initialized.
				As all default worlds are initialized before any scripts are loaded, \
				this event is only called for newly created worlds.
				Note that world management plugins might change the behavior of this event.
				""")
			.addExample("""
				on world init:
					broadcast "The world '%event-world%' is being initialized!"
				""")
			.addExample("""
				on world init of "new_world":
					broadcast "World 'new world' was just initialized!"
				""")
			.addSince("1.0")
			.addSince("2.8.0 (defining worlds)")
			.build());
	}

	private @Nullable Literal<World> world;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[0] != null) {
			world = (Literal<World>) args[0];
			if (world.getAnd() && world instanceof LiteralList<World> list)
				list.invertAnd();
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		if (world == null)
			return true;

		WorldInitEvent worldEvent = (WorldInitEvent) event;

		return world.check(event, world -> world.equals(worldEvent.getWorld()));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("world initialize")
			.appendIf(world != null,"of", world)
			.toString();
	}

}
