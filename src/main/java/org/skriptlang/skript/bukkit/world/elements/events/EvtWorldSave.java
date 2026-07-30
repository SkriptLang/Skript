package org.skriptlang.skript.bukkit.world.elements.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.LiteralList;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.world.WorldSaveEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtWorldSave extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtWorldSave.class, "World Save")
			.supplier(EvtWorldSave::new)
			.addEvent(WorldSaveEvent.class)
			.addPatterns("world sav(e|ing) [of %-worlds%]")
			.addDescription("""
				Called when a world is saved to disk.
				Usually all worlds are saved simultaneously,\s
				but world management plugins could change this.
				""")
			.addExample("""
				on world save:
					broadcast "The world '%event-world%' is getting saved!"
				""")
			.addExample("""
				on world save of "world":
					broadcast "The main world is being saved!"
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

		WorldSaveEvent worldEvent = (WorldSaveEvent) event;

		return world.check(event, world -> world.equals(worldEvent.getWorld()));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("world save")
			.appendIf(world != null,"of", world)
			.toString();
	}

}
