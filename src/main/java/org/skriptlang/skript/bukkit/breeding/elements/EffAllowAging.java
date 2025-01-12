package org.skriptlang.skript.bukkit.breeding.elements;

import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Breedable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Allow Aging")
@Description("Sets whether or not living entities will be able to age.")
@Examples({
	"on spawn of animal:",
		"\tallow aging of entity"
})
@Since("2.10")
public class EffAllowAging extends Effect implements SyntaxRuntimeErrorProducer {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffAllowAging.class)
			.addPatterns(
				"lock age of %livingentities%",
				"prevent aging of %livingentities%",
				"prevent %livingentities% from aging",
				"unlock age of %livingentities%",
				"allow aging of %livingentities%",
				"allow %livingentities% to age")
			.build()
		);
	}

	private Node node;
	private boolean unlock;
	private Expression<LivingEntity> entities;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<LivingEntity>) expressions[0];
		unlock = matchedPattern > 2;
		node = getParser().getNode();
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity livingEntity : entities.getArray(event)) {
			if (livingEntity instanceof Breedable breedable) {
				breedable.setAgeLock(!unlock);
			} else {
				warning("An entity passed through wasn't breedable, and was thus unaffected.", entities.toString());
			}
		}
	}

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (unlock ? "allow" : "prevent") + " aging of " + entities.toString(event,debug);
	}

}
