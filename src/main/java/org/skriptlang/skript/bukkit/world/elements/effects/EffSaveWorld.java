package org.skriptlang.skript.bukkit.world.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Save World")
@Description("""
	Save all worlds or a given world manually.
	Note: saving many worlds at once may possibly cause the server to freeze.
	""")
@Example("save \"world_nether\"")
@Example("save all worlds")
@Since("2.8.0")
public class EffSaveWorld extends Effect {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffSaveWorld.class)
			.supplier(EffSaveWorld::new)
			.addPattern("save [[the] world[s]] %worlds%")
			.build());
	}

	private Expression<World> worlds;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		worlds = (Expression<World>) expressions[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (World world : worlds.getArray(event))
			world.save();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "save the world(s) " + worlds.toString(event, debug);
	}

}
