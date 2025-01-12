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
import org.bukkit.entity.Animals;
import org.bukkit.entity.LivingEntity;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is In Love")
@Description("Checks whether or not a living entity is in love.")
@Examples({
	"on spawn of living entity:",
		"\tif entity is in love:",
			"broadcast \"That was quick!\""
})
@Since("2.10")
public class CondIsInLove extends PropertyCondition<LivingEntity> implements SyntaxRuntimeErrorProducer {

	public static void register(SyntaxRegistry registry) {
		register(registry, CondIsInLove.class, "in lov(e|ing) [state|mode]", "livingentities");
	}

	private Node node;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		node = getParser().getNode();
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (entity instanceof Animals animals) {
			return animals.isLoveMode();
		} else {
			warning("An entity passed through wasn't an animal, and thus returned false by default.");
			return false;
		}
	}

	@Override
	protected String getPropertyName() {
		return "in love";
	}

	@Override
	public Node getNode() {
		return node;
	}

}
