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

@Name("Can Age")
@Description("Checks whether or not an entity will be able to age/grow up.")
@Examples({
	"on breeding:",
		"\tentity can't age",
		"\tbroadcast \"An immortal has been born!\" to player"
})
@Since("2.10")
public class CondCanAge extends PropertyCondition<LivingEntity> implements SyntaxRuntimeErrorProducer {

	public static void register(SyntaxRegistry registry) {
		register(registry, CondCanAge.class, PropertyType.CAN, "(age|grow (up|old[er]))", "livingentities");
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
			return !breedable.getAgeLock();
		} else {
			warning("An entity passed through wasn't breedable, and thus returned false by default.");
			return false;
		}
	}

	@Override
	protected String getPropertyName() {
		return "age";
	}

	@Override
	public Node getNode() {
		return node;
	}

}
