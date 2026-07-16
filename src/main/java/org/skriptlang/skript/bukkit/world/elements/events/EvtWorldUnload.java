package org.skriptlang.skript.bukkit.world.elements.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.LiteralList;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtWorldUnload extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtWorldUnload.class, "World Unload")
			.supplier(EvtWorldUnload::new)
			.addEvent(WorldUnloadEvent.class)
			.addPatterns("world unload[ing] [of %-worlds%]")
			.addDescription("""
				Called when a world is unloaded.
				Note that event will never be called if you don't have something that handles worlds at runtime,\s
				e.g. a world management plugin
				""")
			.addExample("""
				on world unload:
				    broadcast "The world '%event-world%' is getting unloaded!"
				""")
			.addExample("""
				on world unload of "world":
				    broadcast "The main world is being unloaded!"
				""")
			.addSince("1.0, 2.8.0 (defining worlds)")
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

		WorldUnloadEvent worldEvent = (WorldUnloadEvent) event;

		return world.check(event, world -> world.equals(worldEvent.getWorld()));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("world unload")
			.appendIf(world != null,"of", world)
			.toString();
	}

}
