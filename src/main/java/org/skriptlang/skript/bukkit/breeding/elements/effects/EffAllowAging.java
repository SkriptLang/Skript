package org.skriptlang.skript.bukkit.breeding.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
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
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Allow Aging")
@Description("Sets whether or not living entities will be able to age.")
@Example("""
	on spawn of animal:
		allow aging of entity
	""")
@Since("2.10")
public class EffAllowAging extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffAllowAging.class)
				.addPatterns(
					"lock age of %livingentities%",
					"prevent aging of %livingentities%",
					"prevent %livingentities% from aging",
					"unlock age of %livingentities%",
					"allow aging of %livingentities%",
					"allow %livingentities% to age"
				)
				.supplier(EffAllowAging::new)
				.build()
		);
	}

	private boolean unlock;
	private Expression<LivingEntity> entities;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<LivingEntity>) expressions[0];
		unlock = matchedPattern > 2;
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (LivingEntity livingEntity : entities.getArray(event)) {
			if (!(livingEntity instanceof Breedable breedable))
				continue;

			breedable.setAgeLock(!unlock);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (unlock ? "allow" : "prevent") + " aging of " + entities.toString(event,debug);
	}

}
