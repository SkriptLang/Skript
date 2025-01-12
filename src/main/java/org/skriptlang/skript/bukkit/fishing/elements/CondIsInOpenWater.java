package org.skriptlang.skript.bukkit.fishing.elements;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Fish Hook in Open Water")
@Description({
	"Checks whether the fish hook is in open water.",
	"Open water is defined by a 5x4x5 area of water, air and lily pads. " +
	"If in open water, treasure items may be caught."
})
@Examples({
	"on fish catch:",
		"\tif fish hook is in open water:",
			"\t\tsend \"You will catch a shark soon!\""
})
@Events("Fishing")
@Since("2.10")
public class CondIsInOpenWater extends PropertyCondition<Entity> implements SyntaxRuntimeErrorProducer {
	
	public static void register(SyntaxRegistry registry) {
		register(registry, CondIsInOpenWater.class, PropertyType.BE,
			"in open water[s]", "entities");
	}

	private Node node;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		node = getParser().getNode();
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(Entity entity) {
		if (entity instanceof FishHook hook) {
			return hook.isInOpenWater();
		} else {
			warning("An entity passed through wasn't a fish hook, and thus returned false by default.");
			return false;
		}
	}

	@Override
	protected String getPropertyName() {
		return "in open water";
	}

	@Override
	public Node getNode() {
		return node;
	}

}
