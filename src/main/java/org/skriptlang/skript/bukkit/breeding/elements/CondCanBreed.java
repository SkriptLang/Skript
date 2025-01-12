package org.skriptlang.skript.bukkit.breeding.elements;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Breedable;
import org.bukkit.entity.LivingEntity;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Can Breed")
@Description("Checks whether or not a living entity can be bred.")
@Examples({
	"on right click on living entity:",
		"\tevent-entity can't breed",
		"\tsend \"Turns out %event-entity% is not breedable. Must be a Skript user!\" to player"
})
@Since("2.10")
public class CondCanBreed extends PropertyCondition<LivingEntity> implements SyntaxRuntimeErrorProducer {

	public static void register(SyntaxRegistry registry) {
		register(registry, CondCanBreed.class, PropertyType.CAN, "(breed|be bred)", "livingentities");
	}

	private Node node;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		node = getParser().getNode();
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (entity instanceof Breedable breedable) {
			return breedable.canBreed();
		} else {
			warning("An entity passed through wasn't breedable, and thus returned false by default.");
			return false;
		}
	}

	@Override
	protected String getPropertyName() {
		return "breed";
	}

	@Override
	public Node getNode() {
		return node;
	}

}
